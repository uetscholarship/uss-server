package net.bqc.uss.messenger.controller;

import com.restfb.DefaultJsonMapper;
import com.restfb.JsonMapper;
import com.restfb.types.send.Message;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessageItem;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.restfb.types.webhook.messaging.PostbackItem;
import com.restfb.types.webhook.messaging.QuickReplyItem;
import net.bqc.uss.messenger.dao.GradeSubscriberDaoImpl;
import net.bqc.uss.messenger.dao.UserDao;
import net.bqc.uss.messenger.model.User;
import net.bqc.uss.messenger.service.GradeSubscriptionService;
import net.bqc.uss.messenger.service.MyMessengerService;
import net.bqc.uss.messenger.service.NLPService;
import net.bqc.uss.messenger.service.NewsSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
public class WebhookController {

	private static final Logger logger = LoggerFactory.getLogger(NLPService.class);

    @Autowired
    private NLPService nlpService;

	@Autowired
	private GradeSubscriptionService gradeSubscriptionService;

	@Autowired
    private NewsSubscriptionService newsSubscriptionService;

	@Autowired
	private MyMessengerService myMessengerService;

	@Autowired
	private MessageSource messageSource;

	@Value("${webhook.token}")
	private String webhookToken;

	@Autowired
	private UserDao userDao;

	@Autowired
	private GradeSubscriberDaoImpl gradeSubscriberDao;

	private JsonMapper jsonMapper = new DefaultJsonMapper();

	@RequestMapping(value="/webhook", method=RequestMethod.GET)
	public String validate(@RequestParam(value="hub.challenge") String challenge,
			@RequestParam(value="hub.verify_token") String token) {

		return (webhookToken.equals(token)) ? challenge : "not valid";
	}

	@RequestMapping(value="/webhook", method=RequestMethod.POST)
	public ResponseEntity<String> receive(@RequestBody final String json) {
		logger.debug("Request: {}", json);
		String userId;

		try {
			WebhookObject data = jsonMapper.toJavaObject(json, WebhookObject.class);
			List<MessagingItem> messagingItems = data.getEntryList().get(0).getMessaging();
			for (MessagingItem messagingItem : messagingItems) {
				userId = messagingItem.getSender().getId();

				// send typing on, to let user know bot works
				myMessengerService.sendSenderAction(userId, MyMessengerService.SENDER_ACTION_TYPING_ON);

				PostbackItem postbackItem = messagingItem.getPostback();

				MessageItem messageItem = messagingItem.getMessage();
				QuickReplyItem qrItem = null;
				String text = null;

				if (messageItem != null) {
					qrItem = messageItem.getQuickReply();
					text = messageItem.getText();
				}

				if (qrItem != null || postbackItem != null) { // postback
					String payload = (qrItem != null) ? qrItem.getPayload() : postbackItem.getPayload();
					processPostback(payload, userId);
				}
				else if (text != null && text.matches("^#[Dd][Kk].*")) { // #DKxxxxxxxxxxxxxx
					// store information of fb users
					// START SAVING
					// fetch user info
					com.restfb.types.User fbUser = myMessengerService.getUserInformation(userId);
					User user = new User();
					user.setFbId(userId);
					user.setFirstName(fbUser.getFirstName());
					user.setLastName(fbUser.getLastName());
					user.setSubscribed(false);
					userDao.insert(user);
					// END SAVING

				    processSubscribeGradeTextMessage(userId, text);
				}
				else {
                    /**
                     * PROCESS NLP
                     */
					nlpService.processNlp(userId, messageItem.getNlp());
                }
			}
			return new ResponseEntity<>("success", HttpStatus.OK);
		}
		catch(Exception e) {
			logger.error(e.getMessage());

			return new ResponseEntity<>("success", HttpStatus.OK);
		}
	}

	private void processPostback(String payload, String userId) {
		if (MyMessengerService.QR_SUBSCRIBE_NEWS_PAYLOAD.equals(payload)) {
			newsSubscriptionService.processSubscribeNewsMessage(userId);
		}
		else if (MyMessengerService.QR_UNSUBSCRIBE_NEWS_PAYLOAD.equals(payload)) {
            newsSubscriptionService.processUnSubscribeNewsMessage(userId);
		}
		else if (MyMessengerService.MN_SUBSCRIBE_GRADE_PAYLOAD.equals(payload)) {
			gradeSubscriptionService.processReqSubscribeGradeMessage(userId);
		}
		else if (payload != null && payload.startsWith(MyMessengerService.BTN_UNSUBSCRIBE_GRADE_PAYLOAD)) {
            gradeSubscriptionService.processUnsubscribeGradeMessage(userId, payload);
		}
		// QR_ACCEPT_RESUBSCRIBE_GRADE_14027777
		else if (payload != null && payload.startsWith(MyMessengerService.QR_ACCEPT_RESUBSCRIBE_GRADE)) {
			processSubscribeGradePostback(userId, payload);
		}
		else if (MyMessengerService.MN_GET_GRADES_PAYLOAD.equals(payload)) {
            gradeSubscriptionService.processReqGetAllGradesMessage(userId);
		}
		else if (payload != null && payload.startsWith(MyMessengerService.BTN_GET_GRADES_PAYLOAD)) {
            processReqGetGradesMessage(userId, payload);
        }
		else if (MyMessengerService.MN_GRADE_SUBSCRIPTION_PAYLOAD.equals(payload)) {
			processMenuGradeSubscriptionMessage(userId);
		}
		else if (MyMessengerService.MN_NEWS_SUBSCRIPTION_PAYLOAD.equals(payload)) {
			processMenuNewsSubscriptionMessage(userId);
		}
		else if (MyMessengerService.BTN_GET_STARTED_PAYLOAD.equals(payload)) {
			processGetStartedMessage(userId);
		}
		else if (MyMessengerService.QR_DECLINE_EVERYTHING_PAYLOAD.equals(payload)) {
			processDeclineMessage(userId);
		}
	}

    /****************************************************************
     * ============================================================ *
     *  GRADE SUBSCRIPTION MANAGEMENT                               *
     * ============================================================ *
     ****************************************************************/
    private void processSubscribeGradePostback(String userId, String payload) {
        Message infoMessage;
        String[] payloadPieces = payload.split("_");
        String studentCode = payloadPieces[payloadPieces.length - 1];
        // need validate for student code, maybe in case white characters is not expected (not space) :(
        if (!studentCode.matches("\\d{8}")) {
            infoMessage = myMessengerService.buildGenericMessage(
                    getMessage("text.title.fail", null),
                    getMessage("grade.text.sub.fail", null),
                    null, null);
            myMessengerService.sendMessage(userId, infoMessage);
        }
        else {
            gradeSubscriptionService.processSubscribeGradeRequest(userId, studentCode);
        }
    }

	private void processReqGetGradesMessage(String userId, String payload) {
        String[] payloadPieces = payload.split("_");
        String studentCode = payloadPieces[payloadPieces.length - 1];
        if (!studentCode.matches("\\d{8}") || !gradeSubscriberDao.isSubscribed(userId, studentCode)) {
            Message errorMessage = MyMessengerService.buildGenericMessage(
                    getMessage("text.title.warning", null),
                    getMessage("text.err", null),
                    null, null);
            myMessengerService.sendMessage(userId, errorMessage);
        }
        else {
            gradeSubscriptionService.sendAllGrades(userId, studentCode);
        }
    }

	private void processSubscribeGradeTextMessage(String userId, String textMessage) {
        Message infoMessage;

        // get student code from textMessage
        String[] payloadPieces = textMessage.split(" ");
        String studentCode = payloadPieces[payloadPieces.length - 1];
        // need validate for student code, maybe in case white characters is not expected (not space) :(
        if (!studentCode.matches("\\d{8}")) {
			infoMessage = MyMessengerService.buildGenericMessage(
					getMessage("text.title.fail", null),
					getMessage("grade.text.sub.fail", null),
					null, null);
            myMessengerService.sendMessage(userId, infoMessage);
        }
        else {
            gradeSubscriptionService.processSubscribeGradeRequest(userId, studentCode);
        }
    }

    /****************************************************************
     * ============================================================ *
     *  NEWS SUBSCRIPTION MANAGEMENT                                *
     * ============================================================ *
     ****************************************************************/

	private void processMenuNewsSubscriptionMessage(String userId) {
		User user = userDao.findByFbId(userId);
		boolean isSubscribed = (user != null) && user.isSubscribed();
		newsSubscriptionService.sendNewsSubscriptionStatus(userId, isSubscribed);
	}

	private void processMenuGradeSubscriptionMessage(String userId) {
		List<String> studentCodes = gradeSubscriberDao.findStudentCodesBySubscriber(userId);
        gradeSubscriptionService.sendGradeSubscriptionStatus(userId, studentCodes);
	}

    /****************************************************************
     * ============================================================ *
     *  MISCELLANEOUS FUNCTIONS                                     *
     * ============================================================ *
     ****************************************************************/

    private void processDeclineMessage(String userId) {
        myMessengerService.sendTextMessage(
                userId,
                getMessage("text.thank_for_declining", null));
    }

    private void processGetStartedMessage(String userId) {
        Message welcomeMessage = MyMessengerService.buildGenericMessage(
                getMessage("text.welcome.title", null),
                getMessage("text.welcome.subtitle", null),
                null, null);
        myMessengerService.sendMessage(userId, welcomeMessage);
    }

    private String getMessage(String key, Object[] objects) {
		return messageSource.getMessage(key, objects, Locale.ENGLISH);
	}
}

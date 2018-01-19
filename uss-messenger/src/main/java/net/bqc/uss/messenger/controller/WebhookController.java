package net.bqc.uss.messenger.controller;

import java.util.List;
import java.util.Locale;

import com.restfb.types.send.Message;
import com.restfb.types.webhook.messaging.MessageItem;
import com.restfb.types.webhook.messaging.PostbackItem;
import net.bqc.uss.messenger.dao.GradeSubscriberDaoImpl;
import net.bqc.uss.messenger.dao.UserDao;
import net.bqc.uss.messenger.model.User;
import net.bqc.uss.messenger.service.MyMessengerService;
import net.bqc.uss.service.UetGradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restfb.DefaultJsonMapper;
import com.restfb.JsonMapper;
import com.restfb.types.webhook.WebhookObject;
import com.restfb.types.webhook.messaging.MessagingItem;
import com.restfb.types.webhook.messaging.QuickReplyItem;

@RestController
public class WebhookController {

	private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

	@Autowired
	private UetGradeService uetGradeService;

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
		String userId = null;
		try {
			WebhookObject data = jsonMapper.toJavaObject(json, WebhookObject.class);
			List<MessagingItem> messagingItems = data.getEntryList().get(0).getMessaging();
			for (MessagingItem messagingItem : messagingItems) {
				userId = messagingItem.getSender().getId();
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
				    processSubscribeGradeMessage(userId, text);
				}
				else {
                    processUnknownMessage(userId);
                }
			}
			return new ResponseEntity<>("success", HttpStatus.OK);
		}
		catch(Exception e) {
			e.printStackTrace();

			if (userId != null) {
                // notify error to users and ask him/her to retry
                Message errorMessage = myMessengerService.buildGenericMessage(
                        getMessage("text.title.fail", null),
                        getMessage("text.subtitle.fail", null),
                        null, null);
                myMessengerService.sendMessage(userId, errorMessage);
            }

			return new ResponseEntity<>("error", HttpStatus.OK);
		}
	}

	private void processPostback(String payload, String userId) {
		if (MyMessengerService.QR_SUBSCRIBE_NEWS_PAYLOAD.equals(payload)) {
			processSubscribeNewsMessage(userId);
		}
		else if (MyMessengerService.QR_UNSUBSCRIBE_NEWS_PAYLOAD.equals(payload)) {
			processUnSubscribeNewsMessage(userId);
		}
		else if (MyMessengerService.MN_SUBSCRIBE_GRADE_PAYLOAD.equals(payload)) {
			processReqSubscribeGradeMessage(userId);
		}
		else if (payload != null && payload.startsWith(MyMessengerService.BTN_UNSUBSCRIBE_GRADE_PAYLOAD)) {
			processUnsubscribeGradeMessage(userId, payload);
		}
		else if (MyMessengerService.MN_GET_GRADES_PAYLOAD.equals(payload)) {
			processReqGetAllGradesMessage(userId);
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
	}

	private void processGetStartedMessage(String userId) {
		Message welcomeMessage = myMessengerService.buildGenericMessage(
				getMessage("text.welcome.title", null),
				getMessage("text.welcome.subtitle", null),
				null, null);
		myMessengerService.sendMessage(userId, welcomeMessage);
	}

	private void processReqGetGradesMessage(String userId, String payload) {
        String[] payloadPieces = payload.split("_");
        String studentCode = payloadPieces[payloadPieces.length - 1];
        if (!studentCode.matches("\\d{8}") || !gradeSubscriberDao.isSubscribed(userId, studentCode)) {
            Message errorMessage = myMessengerService.buildGenericMessage(
                    getMessage("text.title.warning", null),
                    getMessage("text.err", null),
                    null, null);
            myMessengerService.sendMessage(userId, errorMessage);
        }
        else {
            myMessengerService.sendAllGrades(userId, studentCode);
        }
    }

    private void processReqGetAllGradesMessage(String userId) {
		List<String> studentCodes = gradeSubscriberDao.findStudentCodesBySubscriber(userId);
		if (studentCodes.size() == 1) { // display grades for single-subscriber
		    processReqGetGradesMessage(userId, String.format("%s_%s",
                    MyMessengerService.BTN_GET_GRADES_PAYLOAD, studentCodes.get(0)));
        }
		else { // do same as processing grade subscription message
            myMessengerService.sendGradeSubscriptionStatus(userId, studentCodes);
        }

	}

	private void processUnsubscribeGradeMessage(String userId, String payload) {
		// get student code from postback if student dont match pattern notify error else
		// check and remove from grade_subscribers
		// check if there's no subscriber of that student code, send unsubscribe to uetgrade-server
		// send text message to user, notify unsub successfully
		String[] payloadPieces = payload.split("_");
		String studentCode = payloadPieces[payloadPieces.length - 1];
		if (!studentCode.matches("\\d{8}") || !gradeSubscriberDao.isSubscribed(userId, studentCode)) {
            Message errorMessage = myMessengerService.buildGenericMessage(
                    getMessage("text.title.warning", null),
                    getMessage("text.err", null),
                    null, null);
            myMessengerService.sendMessage(userId, errorMessage);
		}
		else {
			gradeSubscriberDao.deleteSubscriber(userId, studentCode);
			List<String> subscribers = gradeSubscriberDao.findSubscribersByStudentCode(studentCode);
			if (subscribers.size() == 0) {
				logger.debug("Request uetgrade-server to unsubscribe for " + studentCode);
				boolean result = uetGradeService.unsubscribeGrade(studentCode);
				logger.debug("Result: " + result);
			}
			Message successMessage = myMessengerService.buildGenericMessage(
					getMessage("text.title.success", null),
					getMessage("grade.text.unsub.success", new Object[]{studentCode}),
					null, null);
			myMessengerService.sendMessage(userId, successMessage);
		}

	}

	private void processReqSubscribeGradeMessage(String userId) {
		// ask user to enter student code for subscription
        Message askMessage = myMessengerService.buildAskForStudentCodeSubGradesMessage();
        myMessengerService.sendMessage(userId, askMessage);
	}

	private void processSubscribeGradeMessage(String userId, String textMessage) {
        Message infoMessage;

        // get student code from textMessage
        String[] payloadPieces = textMessage.split(" ");
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
            // check if pair (userId, studentCode) existed in grade_subscribers
            List<String> studentCodes = gradeSubscriberDao.findStudentCodesBySubscriber(userId);
            if (studentCodes.contains(studentCode)) {
                infoMessage = myMessengerService.buildGenericMessage(
                        getMessage("text.title.fail", null),
                        getMessage("grade.text.has_already_sub", new Object[] { studentCode }),
                        null, null);
                myMessengerService.sendMessage(userId, infoMessage);
                // hint them to click on menu to get grade for subscribed student codes
                myMessengerService.sendTextMessage(userId,
                        getMessage("grade.text.has_already_sub.support", null));
            }
            else {
                // request subscribe to uetgrade-server for that student code
                // if uetgrade-server return true, notify success, and insert into grade_subscribers
                // 		else notify fail
				logger.debug("Request uetgrade-server to subscribe for " + studentCode);
				boolean result = uetGradeService.subscribeGrade(studentCode);
				logger.debug("Result: " + result);
                if (result) { // success
                    gradeSubscriberDao.insertSubscriber(userId, studentCode);

                    // notify success
                    infoMessage = myMessengerService.buildGenericMessage(
                            getMessage("text.title.success", null),
                            getMessage("grade.text.sub.success", new Object[] { studentCode }),
                            null, null);
                    myMessengerService.sendMessage(userId, infoMessage);

                    // send all grades on the first time
                    myMessengerService.sendAllGrades(userId, studentCode);
                }
                else { // fail
                    infoMessage = myMessengerService.buildGenericMessage(
                            getMessage("text.title.fail", null),
                            getMessage("grade.text.sub.fail", null),
                            null, null);
                    myMessengerService.sendMessage(userId, infoMessage);
                }
            }
        }
    }

	private void processMenuNewsSubscriptionMessage(String userId) {
		User user = userDao.findByFbId(userId);
		boolean isSubscribed = (user == null) ? false : user.isSubscribed();
		myMessengerService.sendNewsSubscriptionStatus(userId, isSubscribed);
	}

	private void processMenuGradeSubscriptionMessage(String userId) {
		List<String> studentCodes = gradeSubscriberDao.findStudentCodesBySubscriber(userId);
		myMessengerService.sendGradeSubscriptionStatus(userId, studentCodes);
	}

	private void processUnSubscribeNewsMessage(String userId) {
		// remove from database
		userDao.updateSubStatus(userId, false);

        Message successMessage = myMessengerService.buildGenericMessage(
                getMessage("text.title.success", null),
                getMessage("news.text.unsub.success", null),
                null, null);
        myMessengerService.sendMessage(userId, successMessage);
	}

	private void processSubscribeNewsMessage(String userId) {
		// fetch user info
		com.restfb.types.User fbUser = myMessengerService.getUserInformation(userId);
		
		// insert to database
		User user = new User();
		user.setFbId(userId);
		user.setFirstName(fbUser.getFirstName());
		user.setLastName(fbUser.getLastName());
		boolean newUser = userDao.insert(user);
		
		if (!newUser) userDao.updateSubStatus(userId, true);
		String representativeName = user.getFirstName() == null ? "Stranger" : user.getFirstName();

		// notify
        Message successMessage = myMessengerService.buildGenericMessage(
                getMessage("text.title.success", null),
                getMessage("news.text.sub.success", null),
                null, null);
        myMessengerService.sendMessage(userId, successMessage);
		myMessengerService.sendTextMessage(userId,
				getMessage("text.compliment", new Object[] { representativeName }));
	}
	
	private void processUnknownMessage(String userId) {
		myMessengerService.sendTextMessage(userId, getMessage("text.nothing", null));
	}

	private String getMessage(String key, Object[] objects) {
		return messageSource.getMessage(key, objects, Locale.ENGLISH);
	}
}

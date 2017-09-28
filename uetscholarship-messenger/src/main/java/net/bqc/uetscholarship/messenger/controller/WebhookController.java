package net.bqc.uetscholarship.messenger.controller;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
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

import net.bqc.uetscholarship.messenger.dao.UserDao;
import net.bqc.uetscholarship.messenger.model.User;
import net.bqc.uetscholarship.messenger.service.MessengerService;

@RestController
public class WebhookController {
	
	private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
	
	@Autowired
	private MessengerService messengerService;
	
	@Autowired
	private MessageSource messageSource;

	@Value("${webhook_token}")
	private String webhookToken;
	
	@Autowired
	private UserDao userDao;
	
	private JsonMapper jsonMapper = new DefaultJsonMapper();

	@RequestMapping(value="/webhook", method=RequestMethod.GET)
	public String validate(Model model,
			@RequestParam(value="hub.challenge") String challenge,
			@RequestParam(value="hub.verify_token") String token) {

		return (webhookToken.equals(token)) ? challenge : "not valid";
	}
	
	@RequestMapping(value="/webhook", method=RequestMethod.POST)
	public ResponseEntity<String> receive(Model model, @RequestBody final String json) {
		System.out.println("request=" + json);
		try {
			WebhookObject data = jsonMapper.toJavaObject(json, WebhookObject.class);
			List<MessagingItem> messagingItems = data.getEntryList().get(0).getMessaging();
			for (MessagingItem messagingItem : messagingItems) {
				String userId = messagingItem.getSender().getId();
				QuickReplyItem qrItem = messagingItem.getMessage().getQuickReply();
				
				if (qrItem != null) {
					String postBack = qrItem.getPayload();
					if (MessengerService.QR_SUBSCRIBE_PAYLOAD.equals(postBack)) {
						processSubscribeMessage(userId);
					}
					else if (MessengerService.QR_CANCEL_PAYLOAD.equals(postBack)) {
						processCancelMessage(userId);
					}
				}
				else {
					processUnknownMessage(userId);
				}
			}
			return new ResponseEntity<String>("success", HttpStatus.OK);
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private void processCancelMessage(String userId) {
		// remove from database
		userDao.updateSubStatus(userId, false);
		messengerService.sendTextMessage(userId,
				messageSource.getMessage("wh.subscribe.cancel", null, Locale.ENGLISH));
	}

	private void processSubscribeMessage(String userId) {
		// fetch user info
		com.restfb.types.User fbUser = messengerService.getUserInformation(userId);
		
		// insert to database
		User user = new User();
		user.setFbId(userId);
		user.setFirstName(fbUser.getFirstName());
		user.setLastName(fbUser.getLastName());
		boolean newUser = userDao.insert(user);
		
		if (!newUser) userDao.updateSubStatus(userId, true);
		
		// notify
		messengerService.sendTextMessage(userId,
				messageSource.getMessage("wh.subscribe.success", null, Locale.ENGLISH));
	}
	
	private void processUnknownMessage(String userId) {
		// send quick reply message
		User user = userDao.findByFbId(userId);
		boolean isSubscribed = (user == null) ? false : user.isSubscribed();
		messengerService.sendSubscriptionMessage(userId, isSubscribed);
	}
}

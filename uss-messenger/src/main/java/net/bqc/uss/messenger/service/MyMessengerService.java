package net.bqc.uss.messenger.service;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;
import com.restfb.types.send.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Service
public class MyMessengerService {

    private static final Logger logger = LoggerFactory.getLogger(MyMessengerService.class);

    public static final int MAX_PAYLOAD_ELEMENTS = 9;

	public static final String MN_NEWS_SUBSCRIPTION_PAYLOAD = "MN_NEWS_SUBSCRIPTION_PAYLOAD";
	public static final String QR_SUBSCRIBE_NEWS_PAYLOAD = "QR_SUBSCRIBE_NEWS_PAYLOAD";
	public static final String QR_UNSUBSCRIBE_NEWS_PAYLOAD = "QR_UNSUBSCRIBE_NEWS_PAYLOAD";

	public static final String MN_GRADE_SUBSCRIPTION_PAYLOAD = "MN_GRADE_SUBSCRIPTION_PAYLOAD";
	public static final String MN_GET_GRADES_PAYLOAD = "MN_GET_GRADES_PAYLOAD";
	public static final String MN_SUBSCRIBE_GRADE_PAYLOAD = "MN_SUBSCRIBE_GRADE_PAYLOAD";

	public static final String BTN_SUBSCRIBE_GRADE_PAYLOAD = "BTN_SUBSCRIBE_GRADE_PAYLOAD";
	public static final String BTN_UNSUBSCRIBE_GRADE_PAYLOAD = "BTN_UNSUBSCRIBE_GRADE_PAYLOAD";
	public static final String BTN_GET_GRADES_PAYLOAD = "BTN_GET_GRADES_PAYLOAD";

	public static final String BTN_GET_STARTED_PAYLOAD = "BTN_GET_STARTED_PAYLOAD";

	public static final String BTN_DECLINE_PAYLOAD = "BTN_DECLINE_PAYLOAD";

	public static final String SENDER_ACTION_TYPING_ON = "typing_on";
    public static final String QR_DECLINE_EVERYTHING_PAYLOAD = "QR_DECLINE_EVERYTHING_PAYLOAD";
	public static final String QR_ACCEPT_RESUBSCRIBE_GRADE = "QR_ACCEPT_RESUBSCRIBE_GRADE";


	@Value("${fb.page.token}")
	private String PAGE_TOKEN;
	
	@Autowired
	private MessageSource messageSource;
	
	private FacebookClient pageClient;

	@PostConstruct
	public void init() {
		pageClient = new DefaultFacebookClient(PAGE_TOKEN, Version.VERSION_2_9);
	}
	
	public User getUserInformation(String userId) {
		User user;
		try {
			user =  pageClient.fetchObject(userId, User.class);
		} catch (Exception e) {
			logger.error(e.getMessage());
			user = new User();
			user.setFirstName("NULL");
			user.setLastName("NULL");
		}
		return user;
	}

	public void sendMessage(String recipient, Message message) {
		pageClient.publish("me/messages",
				SendResponse.class,
				Parameter.with("recipient", new IdMessageRecipient(recipient)),
				Parameter.with("message", message));
	}

	public void sendSenderAction(String recipient, String senderAction) {
		pageClient.publish("me/messages",
				SendResponse.class,
				Parameter.with("recipient", new IdMessageRecipient(recipient)),
				Parameter.with("sender_action", senderAction));
	}
	
	public void sendTextMessage(String recipient, String message) {
		Message txtMessage = buildTextMessage(message);
		sendMessage(recipient, txtMessage);
	}

	public static Message buildConfirmMessage(String title, String subtitle, String yesPayload, String noPayload) {
		GenericTemplatePayload payload = new GenericTemplatePayload();
		TemplateAttachment attachment = new TemplateAttachment(payload);

		Bubble bubble = new Bubble(title);
		bubble.setSubtitle(subtitle);
		PostbackButton yesButton = new PostbackButton("Có", yesPayload);
		PostbackButton noButton = new PostbackButton("Không", noPayload);
		bubble.addButton(yesButton);
		bubble.addButton(noButton);
		payload.addBubble(bubble);

		Message message = new Message(attachment);
		return message;
	}
	
	public static Message buildGenericMessage(String title, String subtitle,
			String postbackTitle, String postbackValue) {
		
		Bubble bubble = new Bubble(title);
		bubble.setSubtitle(subtitle);
		
		if (postbackTitle != null && postbackValue != null) {
			PostbackButton postbackBtn = new PostbackButton(postbackTitle, postbackValue); 
			bubble.addButton(postbackBtn);
		}
		
		GenericTemplatePayload payload = new GenericTemplatePayload();
		payload.addBubble(bubble);
		TemplateAttachment attachment = new TemplateAttachment(payload);
		return new Message(attachment);
	}
	
	public static Message buildTextMessage(String text) {
		return new Message(text);
	}

	public String getMessage(String key, Object[] objects) {
		return messageSource.getMessage(key, objects, Locale.US);
	}
}

package net.bqc.uetscholarship.messenger.service;

import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;
import com.restfb.types.send.Bubble;
import com.restfb.types.send.GenericTemplatePayload;
import com.restfb.types.send.IdMessageRecipient;
import com.restfb.types.send.Message;
import com.restfb.types.send.PostbackButton;
import com.restfb.types.send.QuickReply;
import com.restfb.types.send.SendResponse;
import com.restfb.types.send.TemplateAttachment;
import com.restfb.types.send.WebButton;

@Service
public class MessengerService {
	
	public static final String QR_SUBSCRIBE_PAYLOAD = "SUBSCRIBED_TO_RECIEVE_NEWS";
	public static final String QR_CANCEL_PAYLOAD = "CANCEL_TO_RECIEVE_NEWS";
	
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
		return pageClient.fetchObject(userId, User.class);
	}
	
	public void sendMessage(String recipient, Message message) {
		pageClient.publish("me/messages",
				SendResponse.class,
				Parameter.with("recipient", new IdMessageRecipient(recipient)),
				Parameter.with("message", message));
	}
	
	public void sendTextMessage(String recipient, String message) {
		Message txtMessage = buildTextMessage(message);
		sendMessage(recipient, txtMessage);
	}
	
	public void sendSubscriptionMessage(String recipient, boolean isSubscribed) {
		Message message = new Message(messageSource.getMessage("wh.nothing", null, Locale.ENGLISH));
		QuickReply quickreply;
		
		if (isSubscribed) {
			quickreply = new QuickReply(messageSource.getMessage("messenger.cancel", null, Locale.ENGLISH),
					QR_CANCEL_PAYLOAD);
		}
		else {
			quickreply= new QuickReply(messageSource.getMessage("messenger.subscribe", null, Locale.ENGLISH),
					QR_SUBSCRIBE_PAYLOAD);
		}
		
		message.addQuickReply(quickreply);
		sendMessage(recipient, message);
	}
	
	public Message buildNewsMessage(String title, String link) {
		Bubble bubble = new Bubble(messageSource.getMessage("news.title", null, Locale.ENGLISH));
		bubble.setSubtitle(title);
		
		WebButton button = new WebButton(
				messageSource.getMessage("news.read", null, Locale.ENGLISH), link);
		
		bubble.addButton(button);
		GenericTemplatePayload payload = new GenericTemplatePayload();
		payload.addBubble(bubble);
		TemplateAttachment attachment = new TemplateAttachment(payload);
		return new Message(attachment);
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
}

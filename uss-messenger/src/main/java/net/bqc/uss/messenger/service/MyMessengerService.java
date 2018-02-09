package net.bqc.uss.messenger.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.restfb.types.send.*;
import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;

@Service
public class MyMessengerService {

    private static final Logger logger = LoggerFactory.getLogger(MyMessengerService.class);

    public static final int MAX_PAYLOAD_ELEMENTS = 10;

	public static final String MN_NEWS_SUBSCRIPTION_PAYLOAD = "MN_NEWS_SUBSCRIPTION_PAYLOAD";
	public static final String QR_SUBSCRIBE_NEWS_PAYLOAD = "QR_SUBSCRIBE_NEWS_PAYLOAD";
	public static final String QR_UNSUBSCRIBE_NEWS_PAYLOAD = "QR_UNSUBSCRIBE_NEWS_PAYLOAD";

	public static final String MN_GRADE_SUBSCRIPTION_PAYLOAD = "MN_GRADE_SUBSCRIPTION_PAYLOAD";
	public static final String MN_GET_GRADES_PAYLOAD = "MN_GET_GRADES_PAYLOAD";
	public static final String MN_SUBSCRIBE_GRADE_PAYLOAD = "MN_SUBSCRIBE_GRADE_PAYLOAD";

	public static final String BTN_UNSUBSCRIBE_GRADE_PAYLOAD = "BTN_UNSUBSCRIBE_GRADE_PAYLOAD";
	public static final String BTN_GET_GRADES_PAYLOAD = "BTN_GET_GRADES_PAYLOAD";

	public static final String BTN_GET_STARTED_PAYLOAD = "BTN_GET_STARTED_PAYLOAD";

	public static final String SENDER_ACTION_TYPING_ON = "typing_on";


	@Value("${fb.page.token}")
	private String PAGE_TOKEN;
	
	@Autowired
	private MessageSource messageSource;
	
	private FacebookClient pageClient;

    @Autowired
    private UetGradeService uetGradeService;
	
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
	
	public void sendNewsSubscriptionStatus(String recipient, boolean isSubscribed) {
		Message message;
		QuickReply quickreply;
		
		if (isSubscribed) {
		    message = new Message(getMessage("news.text.subscribed", null));
			quickreply = new QuickReply(getMessage("news.qr.unsub", null),
					QR_UNSUBSCRIBE_NEWS_PAYLOAD);
		}
		else {
            message = new Message(getMessage("news.text.no_subscribed", null));
			quickreply= new QuickReply(getMessage("news.qr.sub", null),
					QR_SUBSCRIBE_NEWS_PAYLOAD);
		}
		
		message.addQuickReply(quickreply);
		sendMessage(recipient, message);
	}

	public void sendGradeSubscriptionStatus(String recipient, List<String> subscribedStudentCodes) {
		if (subscribedStudentCodes.size() == 0) {
            sendTextMessage(recipient, getMessage("grade.text.status.empty", null));
            Message askMessage = buildAskForStudentCodeSubGradesMessage();
			sendMessage(recipient, askMessage);
		}
		else {
			sendMessage(recipient, buildGradeSubscriptionInfoMessage(subscribedStudentCodes));
		}
	}

    public void sendAllGrades(String userId, String studentCode) {
        logger.debug("Request to uetgrade-server get all courses for " + studentCode + ": ");
        Student student = uetGradeService.getStudentWithAllCourses(studentCode);
        logger.debug("Result: " + student);
        if (student == null) {
            sendTextMessage(userId,
                    getMessage("grade.text.std.current_processing", new Object[] { studentCode }));
        }
        else if (student.getCourses().size() == 0) {
            sendTextMessage(userId,
                    getMessage("grade.text.std.no_course", new Object[] { studentCode }));
        }
        else {
            // notify number of courses have grades
            long gradedCoursesCount = student.getCourses().stream()
                    .filter(course -> course.getGradeUrl() != null)
                    .count();

            Message successMessage = buildGenericMessage(
                    String.format("[%s]", student.getCode()),
                    getMessage("grade.text.std.has_course",
                            new Object[] { student.getName(), gradedCoursesCount, student.getCourses().size()}),
                    null, null);
            sendMessage(userId, successMessage);


            // send course list
            sendCoursesList(userId, student);
        }
    }

	public Message buildAskForStudentCodeSubGradesMessage() {
        Message message = buildGenericMessage(
                getMessage("grade.ask_student_code.title", null),
                getMessage("grade.ask_student_code.subtitle", null),
                null, null);
        return message;
    }

	public Message buildGradeSubscriptionInfoMessage(List<String> studentCodes) {
		GenericTemplatePayload payload = new GenericTemplatePayload();
		TemplateAttachment attachment = new TemplateAttachment(payload);

		studentCodes.forEach(studentCode -> {
			Bubble bubble = new Bubble(getMessage("grade.std.title", new Object[] {studentCode }));  // MSSV: 1402xxxx
			bubble.setSubtitle(getMessage("grade.std.content", null));
			PostbackButton getGradesPostbackButton = new PostbackButton(getMessage("grade.btn.get_grades", null),
                    BTN_GET_GRADES_PAYLOAD + "_" + studentCode); // Payload will be: BTN_GET_GRADES_PAYLOAD_1402xxxx
			PostbackButton unsubscribePostbackButton = new PostbackButton(getMessage("grade.btn.unsub", null),
					BTN_UNSUBSCRIBE_GRADE_PAYLOAD + "_" + studentCode); // Payload will be: BTN_UNSUBSCRIBE_GRADE_PAYLOAD_1402xxxx
            bubble.addButton(getGradesPostbackButton);
			bubble.addButton(unsubscribePostbackButton);
			payload.addBubble(bubble);
		});

		Message message = new Message(attachment);
		return message;
	}
	
	public Message buildNewsMessage(String title, String link) {
		Bubble bubble = new Bubble(getMessage("news.title", null));
		bubble.setSubtitle(title);
		
		WebButton button = new WebButton(getMessage("news.read", null), link);
		
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

	public String getMessage(String key, Object[] objects) {
		return messageSource.getMessage(key, objects, Locale.ENGLISH);
	}

	public void sendCoursesList(String recipient, Student student) {
		Message courseListMessage = buildCoursesInfoMessage(student.getCourses());
		sendMessage(recipient, courseListMessage);
	}

    public Message buildCoursesInfoMessage(Set<Course> courses) {
        GenericTemplatePayload payload = new GenericTemplatePayload();
        TemplateAttachment attachment = new TemplateAttachment(payload);
        courses.forEach(course -> {
            Bubble bubble = new Bubble(String.format("[%s] %s", course.getCode(), course.getName())); // maximum characters for title is 80
            if (course.getGradeUrl() != null) {
                bubble.setSubtitle(getMessage("grade.course.subtitle", null));
                WebButton button = new WebButton(getMessage("grade.course.read", null), course.getGradeUrl());
                bubble.addButton(button);
            }
            else {
                bubble.setSubtitle(getMessage("grade.course.has_no_grade", null));
            }
            payload.addBubble(bubble);
        });

        Message message = new Message(attachment);
        return message;
    }
}

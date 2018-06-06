package net.bqc.uss.messenger.service;

import com.restfb.types.send.*;
import net.bqc.uss.messenger.dao.GradeSubscriberDaoImpl;
import net.bqc.uss.service.UetGradeService;
import net.bqc.uss.uetgrade_server.entity.Course;
import net.bqc.uss.uetgrade_server.entity.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static net.bqc.uss.messenger.service.MyMessengerService.*;

@Component
public class GradeSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(GradeSubscriptionService.class);

    @Autowired
    private UetGradeService uetGradeService;

    @Autowired
    private MyMessengerService myMessengerService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private GradeSubscriberDaoImpl gradeSubscriberDao;

    public void processReqGetAllGradesMessage(String userId) {
        List<String> studentCodes = gradeSubscriberDao.findStudentCodesBySubscriber(userId);
        if (studentCodes.size() == 1) { // display grades for single-subscriber
            if (!gradeSubscriberDao.isSubscribed(userId, studentCodes.get(0))) {
                Message errorMessage = MyMessengerService.buildGenericMessage(
                        getMessage("text.title.warning", null),
                        getMessage("text.err", null),
                        null, null);
                myMessengerService.sendMessage(userId, errorMessage);
            }
            else {
                sendAllGrades(userId, studentCodes.get(0));
            }

        }
        else { // do same as processing grade subscription message
            sendGradeSubscriptionStatus(userId, studentCodes);
        }
    }

    public void processReqSubscribeGradeMessage(String userId) {
        // ask user to enter student code for subscription
        Message askMessage = buildAskForStudentCodeSubGradesMessage();
        myMessengerService.sendMessage(userId, askMessage);
    }

    public void processUnsubscribeGradeMessage(String userId, String payload) {
        // get student code from postback if student dont match pattern notify error else
        // check and remove from grade_subscribers
        // check if there's no subscriber of that student code, send unsubscribe to uetgrade-server
        // send text message to user, notify unsub successfully
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
            gradeSubscriberDao.deleteSubscriber(userId, studentCode);
            List<String> subscribers = gradeSubscriberDao.findSubscribersByStudentCode(studentCode);
            if (subscribers.size() == 0) {
                logger.debug("Request uetgrade-server to unsubscribe for " + studentCode);
                boolean result = uetGradeService.unsubscribeGrade(studentCode);
                logger.debug("Result: " + result);
            }
            Message successMessage = MyMessengerService.buildGenericMessage(
                    getMessage("text.title.success", null),
                    getMessage("grade.text.unsub.success", new Object[]{studentCode}),
                    null, null);
            myMessengerService.sendMessage(userId, successMessage);
        }

    }

    public void processSubscribeGradeRequest(String userId, String studentCode) {
        Message infoMessage;

        List<String> studentCodes = gradeSubscriberDao.findStudentCodesBySubscriber(userId);
        // check if pair (userId, studentCode) existed in grade_subscribers
        if (studentCodes.contains(studentCode)) {
            infoMessage = MyMessengerService.buildGenericMessage(
                    getMessage("text.title.fail", null),
                    getMessage("grade.text.has_already_sub", new Object[] { studentCode }),
                    null, null);
            myMessengerService.sendMessage(userId, infoMessage);
            // hint them to click on menu to get grade for subscribed student codes
            myMessengerService.sendTextMessage(userId,
                    getMessage("grade.text.has_already_sub.support", null));
        }
        // check maximum number of student codes allowed to subscribe
        else if (studentCodes.size() >= MyMessengerService.MAX_PAYLOAD_ELEMENTS) {
            infoMessage = MyMessengerService.buildGenericMessage(
                    getMessage("text.title.fail", null),
                    getMessage("grade.text.exceed_allowed_student_codes", new Object[] { MyMessengerService.MAX_PAYLOAD_ELEMENTS }),
                    null, null);
            myMessengerService.sendMessage(userId, infoMessage);
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
                this.sendAllGrades(userId, studentCode);
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

    public void sendAllGrades(String userId, String studentCode) {
        logger.debug("Request to uetgrade-server get all courses for " + studentCode + ": ");
        Student student = uetGradeService.getStudentWithAllCourses(studentCode);
        logger.debug("Result: " + student);
        if (student == null) {
            myMessengerService.sendTextMessage(userId,
                    getMessage("grade.text.std.current_processing", new Object[] { studentCode }));
        }
        else if (student.getCourses().size() == 0) {
            myMessengerService.sendTextMessage(userId,
                    getMessage("grade.text.std.no_course", new Object[] { studentCode }));
        }
        else {
            // notify number of courses have grades
            long gradedCoursesCount = student.getCourses().stream()
                    .filter(course -> course.getGradeUrl() != null)
                    .count();

            Message successMessage = MyMessengerService.buildGenericMessage(
                    String.format("[%s]", student.getCode()),
                    getMessage("grade.text.std.has_course",
                            new Object[] { student.getName(), gradedCoursesCount, student.getCourses().size()}),
                    null, null);
            myMessengerService.sendMessage(userId, successMessage);


            // send course list
            sendCoursesList(userId, student);
        }
    }

    public void sendCoursesList(String recipient, Student student) {
        Message courseListMessage = buildCoursesInfoMessage(student.getCourses());
        myMessengerService.sendMessage(recipient, courseListMessage);
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


    public void sendGradeSubscriptionStatus(String recipient, List<String> subscribedStudentCodes) {
        if (subscribedStudentCodes.size() == 0) {
            myMessengerService.sendTextMessage(recipient, getMessage("grade.text.status.empty", null));
            Message askMessage = buildAskForStudentCodeSubGradesMessage();
            myMessengerService.sendMessage(recipient, askMessage);
        }
        else {
            myMessengerService.sendMessage(recipient, buildGradeSubscriptionInfoMessage(subscribedStudentCodes));
        }
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

    public Message buildAskForStudentCodeSubGradesMessage() {
        Message message = MyMessengerService.buildGenericMessage(
                getMessage("grade.ask_student_code.title", null),
                getMessage("grade.ask_student_code.subtitle", null),
                null, null);
        return message;
    }

    private String getMessage(String key, Object[] objects) {
        return messageSource.getMessage(key, objects, Locale.ENGLISH);
    }

    public void confirmToSubscribe(String userId, String studentCode) {
        Message confirmMessage = MyMessengerService.buildConfirmMessage(
                "Xác nhận", String.format("Bạn thật sự muốn đăng ký nhận điểm cho mssv %s?", studentCode),
                String.format("%_%", BTN_SUBSCRIBE_GRADE_PAYLOAD, studentCode),  BTN_DECLINE_PAYLOAD);

        myMessengerService.sendMessage(userId, confirmMessage);
    }

    public void confirmToUnsubscribe(String userId, String studentCode) {
        Message confirmMessage = MyMessengerService.buildConfirmMessage(
                "Xác nhận", String.format("Bạn thật sự muốn hủy đăng ký cho mssv %s?", studentCode),
                String.format("%_%", BTN_UNSUBSCRIBE_GRADE_PAYLOAD, studentCode),  BTN_DECLINE_PAYLOAD);

        myMessengerService.sendMessage(userId, confirmMessage);
    }
}

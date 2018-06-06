package net.bqc.uss.messenger.service;

import com.restfb.types.webhook.messaging.NlpResult;
import com.restfb.types.webhook.messaging.nlp.NlpCustomWitAi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NLPService {

    private static final Logger logger = LoggerFactory.getLogger(NLPService.class);

    public static final String ENTITY_INTENT = "intent";
    public static final String ENTITY_STUDENT_CODE = "student_code";

    public static final String INTENT_GET_GRADE = "get_grade";
    public static final String INTENT_DEFAULT = "default";
    public static final String INTENT_GREETINGS = "greetings";
    public static final String INTENT_THANKFUL = "thankful";
    public static final String INTENT_SUBSCRIBE_GRADE = "subscribe";
    public static final String INTENT_UNSUBSCRIBE_GRADE = "unsubscribe";

    @Autowired
    private MyMessengerService myMessengerService;

    @Autowired
    private GradeSubscriptionService gradeSubscriptionService;

    public void processNlp(String userId, NlpResult nlpResult) {
        List<NlpCustomWitAi> entities = nlpResult.getEntities(NlpCustomWitAi.class);

        if (entities.isEmpty()) { // can not recognize any intents in message
            processUnknownMessage(userId);
        }
        else {
            this.processNlpEntities(userId, entities);
        }

    }

    private void processNlpEntities(String userId, List<NlpCustomWitAi> entities) {
        String intent = null;
        String studentCode = null;

        for (NlpCustomWitAi entity : entities) {

            String witKey = entity.getWitAiKey();
            String value = entity.getValue();
            double score = entity.getConfidence();
            logger.debug("[NLP] Received Intent: [{} = {},{}]", witKey, value, score);

            if (ENTITY_STUDENT_CODE.equals(witKey) && studentCode == null) { // only consider the first appeared student code
                if (value.matches("\\d{8}")) // ensure it is a valid student code
                    studentCode = value;
            }
            else if (ENTITY_INTENT.equals(witKey)) {
                if (INTENT_GET_GRADE.equals(value) && score >= 0.8) {
                    gradeSubscriptionService.processReqGetAllGradesMessage(userId);
                    return;
                }
                if (INTENT_THANKFUL.equals(value) && score >= 0.7) {
                    myMessengerService.sendTextMessage(userId, "Hihi, mong bạn đạt điểm cao nhé ^^");
                    return;
                }
                if (INTENT_GREETINGS.equals(value) && score >= 0.7) {
                    myMessengerService.sendTextMessage(userId, "Hi, chào bạn :D");
                    return;
                }
                if (INTENT_SUBSCRIBE_GRADE.equals(value) && score >= 0.8) {
                    intent = INTENT_SUBSCRIBE_GRADE;
                }
                if (INTENT_UNSUBSCRIBE_GRADE.equals(value) && score >= 0.8) {
                    intent = INTENT_UNSUBSCRIBE_GRADE;
                }
            }
        }

        if (studentCode != null) {
            if (intent == null || INTENT_SUBSCRIBE_GRADE.equals(intent)) {
                gradeSubscriptionService.confirmToSubscribe(userId, studentCode);
                return;
            }
            if (INTENT_UNSUBSCRIBE_GRADE.equals(intent)) {
                gradeSubscriptionService.confirmToUnsubscribe(userId, studentCode);
                return;
            }
        }

        // send stupid message if not any if-statement hits
        processUnknownMessage(userId);
    }

    private void processUnknownMessage(String userId) {
        myMessengerService.sendTextMessage(userId, "BQC đẹp trai và bạn cũng thế :)");
    }
}

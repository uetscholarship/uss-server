package net.bqc.uss.messenger.service;

import com.restfb.types.webhook.messaging.NlpResult;
import com.restfb.types.webhook.messaging.nlp.NlpCustomWitAi;
import net.bqc.uss.messenger.controller.WebhookController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NLPService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    public static final String INTENT_KEY = "intent";
    public static final String INTENT_GET_GRADE = "get_grade";
    public static final String INTENT_DEFAULT = "default";
    public static final String INTENT_THANKFUL = "thankful";

    @Autowired
    private MyMessengerService myMessengerService;

    @Autowired
    private GradeSubscriptionService gradeSubscriptionService;

    public void processNlp(String userId, NlpResult nlpResult) {
        List<NlpCustomWitAi> entities = nlpResult.getEntities(NlpCustomWitAi.class);
//		nlpResult.getEntities().get(0).

        // can not recognize any intents in message
        if (entities.isEmpty()) {
            processUnknownMessage(userId);
        }
        else {
            entities.forEach(e -> this.processNlpEntities(userId, e));
        }

    }

    private void processNlpEntities(String userId, NlpCustomWitAi entity) {
        String witKey = entity.getWitAiKey();
        String value = entity.getValue();
        double score = entity.getConfidence();
        logger.debug("[NLP] Received Intent: [{} = {},{}]", witKey, value, score);

        // get grade intent
        if (NLPService.INTENT_KEY.equals(witKey)) {
            if (NLPService.INTENT_GET_GRADE.equals(value) && score >= 0.8) {
                gradeSubscriptionService.processReqGetAllGradesMessage(userId);
                return;
            }
            if (NLPService.INTENT_THANKFUL.equals(value) && score >= 0.7) {
                myMessengerService.sendTextMessage(userId, "Hihi, mong bạn đạt điểm cao ^^");
                return;
            }

            processUnknownMessage(userId);

        }
        else {
            processUnknownMessage(userId);
        }
    }

    private void processUnknownMessage(String userId) {
        myMessengerService.sendTextMessage(userId, "BQC đẹp trai và bạn cũng thế :)");
    }
}

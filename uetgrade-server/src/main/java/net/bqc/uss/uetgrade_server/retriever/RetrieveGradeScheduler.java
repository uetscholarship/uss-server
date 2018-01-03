package net.bqc.uss.uetgrade_server.retriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetrieveGradeScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveGradeScheduler.class);

    @Scheduled(cron = "0 */30 6-18 * * MON-FRI", zone = "GMT+7")
//	@Scheduled(cron = "*/30 * * * * *", zone = "GMT+7")
    public void retrieve() {

    }
}

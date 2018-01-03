package net.bqc.uss.uetnews_server.retriever;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetrieveNewsScheduler {
	
	private static final Logger logger = LoggerFactory.getLogger(RetrieveNewsScheduler.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
	private String latestLink;
	
	@Autowired
	private RetrieveNewsTask retrieveNewsTask;

	// every 30 minutes from 6h to 18h, Monday to Friday
	@Scheduled(cron = "0 */30 6-18 * * MON-FRI", zone = "GMT+7")
//	@Scheduled(cron = "*/30 * * * * *", zone = "GMT+7")
	public void retrieve() {
		this.latestLink = retrieveNewsTask.call(this.latestLink);
		logger.info("[{}] Retrieved latest news: {}", dateFormat.format(new Date()), this.latestLink);
	}
}

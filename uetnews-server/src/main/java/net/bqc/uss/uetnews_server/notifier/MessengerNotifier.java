package net.bqc.uss.uetnews_server.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.shirwa.simplistic_rss.RssItem;
import net.bqc.uss.service.NotifierService;

@Service
public class MessengerNotifier implements INotifier {

	private static final Logger logger = LoggerFactory.getLogger(MessengerNotifier.class);
	
	@Autowired
	private NotifierService messengerNotifierProxy;
	
	@Override
	public boolean notify(RssItem item) {
		try {
			boolean rsp = messengerNotifierProxy.notify(item);
			logger.info("Messenger response for {}: {}", item.getLink(), rsp);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

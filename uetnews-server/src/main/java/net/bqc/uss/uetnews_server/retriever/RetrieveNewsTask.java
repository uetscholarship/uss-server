package net.bqc.uss.uetnews_server.retriever;

import com.shirwa.simplistic_rss.RssItem;
import com.shirwa.simplistic_rss.RssReader;
import net.bqc.uss.uetnews_server.notifier.FirebaseNotifier;
import net.bqc.uss.uetnews_server.notifier.MessengerNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RetrieveNewsTask {

	@Value("${uet.feed.url}")
	private String FEED_URL;
	
	@Autowired
	private FirebaseNotifier firebaseNotifier;
	
	@Autowired
	private MessengerNotifier messengerNotifier;
	
	public String call(String latestLink) {
		try {
			RssReader reader = new RssReader(FEED_URL);
			List<RssItem> latestItems = new ArrayList<>();
			for (RssItem item : reader.getItems()) {
				if (item.getLink().equals(latestLink)) {
					break;
				}
				latestItems.add(item);
			}
			
			// only notify user when system has non-null latest post
			if (latestLink != null) {
				notify(latestItems);
			}
			
			// return latest post for caller
			return (latestItems.size() == 0) ? latestLink : latestItems.get(0).getLink(); 
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void notify(List<RssItem> latestItems) {
		latestItems.forEach(item -> {
			firebaseNotifier.notify(item);
			messengerNotifier.notify(item);
		});
	}

}

package net.bqc.uetscholarship.server.retriever;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.shirwa.simplistic_rss.RssItem;
import com.shirwa.simplistic_rss.RssReader;

import net.bqc.uetscholarship.server.notifier.FirebaseNotifier;

@Component
public class RetrieveNewsTask {

	private static final String FEED_URL = "https://uet.vnu.edu.vn/category/tin-tuc/tin-sinh-vien/feed";
	
	@Autowired
	private FirebaseNotifier notifier;
	
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
			if (latestLink != null || true) {
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
			notifier.notify(item);
		});
	}

}
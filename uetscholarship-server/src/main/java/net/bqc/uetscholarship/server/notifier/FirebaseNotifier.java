package net.bqc.uetscholarship.server.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.shirwa.simplistic_rss.RssItem;

import net.bqc.uetscholarship.server.entity.FcmMessage;
import net.bqc.uetscholarship.server.entity.FcmNotification;
import net.bqc.uetscholarship.server.util.RestTemplateUtil;

@Service
public class FirebaseNotifier implements INotifier {
	
	private static final Logger logger = LoggerFactory.getLogger(FirebaseNotifier.class);

	@Value("${fcm_api_url}")
	private String FCM_API_URL;
	
	@Value("${fcm_topic_path}")
	private String TOPIC_PATH;

	@Value("${fcm_api_key}")
	private String FCM_API_KEY;

	@Override
	public boolean notify(RssItem item) {
		try {
			FcmNotification notification = new FcmNotification("UET News", item.getTitle());
			notification.putData("link", item.getLink());
			FcmMessage message = new FcmMessage(notification, TOPIC_PATH);

			// send message
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "key=" + FCM_API_KEY);
			HttpEntity<Object> entity = new HttpEntity<>(message, headers);
			String rsp = RestTemplateUtil.sendPost(FCM_API_URL, entity);
			logger.info("Firebase response for {}: {}", item.getLink(), rsp);
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

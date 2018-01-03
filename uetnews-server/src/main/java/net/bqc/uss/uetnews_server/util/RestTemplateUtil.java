package net.bqc.uss.uetnews_server.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestTemplateUtil {

	private static RestTemplate rstTempl = new RestTemplate();

	public static String sendPost(String url, HttpEntity<?> entity) {
		ResponseEntity<String> rsp = rstTempl.exchange(url, HttpMethod.POST, entity, String.class);
		return rsp.getBody();
	}
}

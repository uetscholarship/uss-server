package net.bqc.uetscholarship.service;

import javax.jws.WebService;

import com.shirwa.simplistic_rss.RssItem;

@WebService(name = "Notifier", targetNamespace = "http://jaxws.messenger.uetscholarship.bqc.net/")
public interface NotifierService {
	boolean notify(RssItem item);
}

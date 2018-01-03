package net.bqc.uss.service;

import javax.jws.WebService;

import com.shirwa.simplistic_rss.RssItem;

@WebService(name = "Notifier", targetNamespace = "http://jaxws.messenger.uss.bqc.net/")
public interface NotifierService {
	boolean notify(RssItem item);
}

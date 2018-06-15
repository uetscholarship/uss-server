package net.bqc.uss.service;

import com.shirwa.simplistic_rss.RssItem;

import javax.jws.WebService;

@WebService(name = "Notifier", targetNamespace = "http://jaxws.messenger.uss.bqc.net/")
public interface NotifierService {
	boolean notify(RssItem item);
}

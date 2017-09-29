package net.bqc.uetscholarship.service;

import com.shirwa.simplistic_rss.RssItem;

public interface NotifierService {
	boolean notify(RssItem item);
}

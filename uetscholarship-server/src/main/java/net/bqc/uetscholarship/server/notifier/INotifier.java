package net.bqc.uetscholarship.server.notifier;

import com.shirwa.simplistic_rss.RssItem;

public interface INotifier {
	boolean notify(RssItem item);
}

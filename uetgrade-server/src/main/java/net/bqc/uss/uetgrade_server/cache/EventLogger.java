package net.bqc.uss.uetgrade_server.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLogger implements CacheEventListener<Object, Object> {

    public static final Logger logger = LoggerFactory.getLogger(EventLogger.class);

    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        logger.debug("[{}]: [{} | {} -> {}]",
                cacheEvent.getType(), cacheEvent.getKey(),
                cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}

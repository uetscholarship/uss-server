package net.bqc.uss.uetgrade_server.cache;

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class MyJCacheCustomizer implements JCacheManagerCustomizer {

    @Override
    public void customize(CacheManager cacheManager) {
        cacheManager.createCache("gradeCache", new MutableConfiguration<>()
                // TODO: change expired time of element in cache
                .setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(SECONDS, 10)))
                .setStoreByValue(false)
                .setStatisticsEnabled(true));
    }
}

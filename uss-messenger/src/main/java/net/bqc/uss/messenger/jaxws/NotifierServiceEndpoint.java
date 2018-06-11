package net.bqc.uss.messenger.jaxws;

import com.restfb.types.send.Message;
import com.shirwa.simplistic_rss.RssItem;
import net.bqc.uss.messenger.dao.UserDao;
import net.bqc.uss.messenger.model.User;
import net.bqc.uss.messenger.service.NewsSubscriptionService;
import net.bqc.uss.messenger.task.NotifyTask;
import net.bqc.uss.service.NotifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.Collections;
import java.util.List;

@WebService(serviceName = "NotifierService", portName = "NotifierPort",
		targetNamespace = "http://jaxws.messenger.uss.bqc.net/",
		endpointInterface = "net.bqc.uss.service.NotifierService")
public class NotifierServiceEndpoint extends SpringBeanAutowiringSupport implements NotifierService {

	private static final Logger logger = LoggerFactory.getLogger(NotifierServiceEndpoint.class);

	@Autowired
    private NewsSubscriptionService newsSubscriptionService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TaskExecutor taskExecutor;

    @WebMethod
    public boolean notify(RssItem item) {
        try {
			logger.info("Receive news notification request: {}", item.getLink());
			
	        // build new message
            Message message = newsSubscriptionService.buildNewsMessage(item.getTitle(), item.getLink());
            List<Message> messages = Collections.singletonList(message);

            // send to all subscribed users
            List<User> subscribedUsers = userDao.findAllSubscribedUsers();
            subscribedUsers.forEach(user -> {
                NotifyTask notifyTask = (NotifyTask) applicationContext.getBean("notifyTask");
                notifyTask.setFbId(user.getFbId());
                notifyTask.setMessages(messages);
                taskExecutor.execute(notifyTask);
            });

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

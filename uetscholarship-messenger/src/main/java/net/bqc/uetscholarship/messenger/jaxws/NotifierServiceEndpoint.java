package net.bqc.uetscholarship.messenger.jaxws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.restfb.types.send.Message;
import com.shirwa.simplistic_rss.RssItem;

import net.bqc.uetscholarship.messenger.dao.UserDao;
import net.bqc.uetscholarship.messenger.model.User;
import net.bqc.uetscholarship.messenger.service.MessengerService;
import net.bqc.uetscholarship.service.NotifierService;

@WebService(serviceName = "NotifierService", portName = "NotifierPort",
		targetNamespace = "http://jaxws.messenger.uetscholarship.bqc.net/",
		endpointInterface = "net.bqc.uetscholarship.service.NotifierService")
public class NotifierServiceEndpoint extends SpringBeanAutowiringSupport implements NotifierService {

	private static final Logger logger = LoggerFactory.getLogger(NotifierServiceEndpoint.class);
	
    @Autowired
    private MessengerService messengerService;

    @Autowired
    private UserDao userDao;

    @WebMethod
    public boolean notify(RssItem item) {
        try {
			logger.info("Receive news notification request: {}", item.getLink());
			
	        // build new message
            Message message = messengerService.buildNewsMessage(
                    item.getTitle(), item.getLink());

            // send to all subscribed users
            List<User> subscribedUsers = userDao.findAllSubscribedUsers();
            subscribedUsers.forEach(user -> {
                messengerService.sendMessage(user.getFbId(), message);
            });

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

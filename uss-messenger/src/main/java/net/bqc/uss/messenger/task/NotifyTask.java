package net.bqc.uss.messenger.task;

import com.restfb.exception.FacebookException;
import com.restfb.types.send.Message;
import net.bqc.uss.messenger.service.MyMessengerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("notifyTask")
@Scope("prototype")
public class NotifyTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NotifyTask.class);

    @Autowired
    private MyMessengerService myMessengerService;

    private String fbId;
    private List<Message> messages;

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            logger.debug("Executing notifyTask [{}] -> [{}]",
                    Thread.currentThread().getName(),
                    this.fbId);
            messages.forEach(message -> myMessengerService.sendMessage(fbId, message));
            logger.debug("Done notifyTask [{}]", Thread.currentThread().getName());
        }
        catch (FacebookException e) {
            logger.debug("Facebook Exception: {}" + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

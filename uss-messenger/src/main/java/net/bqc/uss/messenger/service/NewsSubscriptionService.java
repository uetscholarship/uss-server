package net.bqc.uss.messenger.service;

import com.restfb.types.send.*;
import net.bqc.uss.messenger.dao.UserDao;
import net.bqc.uss.messenger.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static net.bqc.uss.messenger.service.MyMessengerService.QR_SUBSCRIBE_NEWS_PAYLOAD;
import static net.bqc.uss.messenger.service.MyMessengerService.QR_UNSUBSCRIBE_NEWS_PAYLOAD;

@Component
public class NewsSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(NewsSubscriptionService.class);

    @Autowired
    private MyMessengerService myMessengerService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserDao userDao;

    public void processUnSubscribeNewsMessage(String userId) {
        // remove from database
        userDao.updateSubStatus(userId, false);

        Message successMessage = MyMessengerService.buildGenericMessage(
                getMessage("text.title.success", null),
                getMessage("news.text.unsub.success", null),
                null, null);
        myMessengerService.sendMessage(userId, successMessage);
    }

    public void processSubscribeNewsMessage(String userId) {
        // fetch user info
        com.restfb.types.User fbUser = myMessengerService.getUserInformation(userId);

        // insert to database
        User user = new User();
        user.setFbId(userId);
        user.setFirstName(fbUser.getFirstName());
        user.setLastName(fbUser.getLastName());
        boolean newUser = userDao.insert(user);

        if (!newUser) userDao.updateSubStatus(userId, true);
        String representativeName = user.getFirstName() == null ? "Stranger" : user.getFirstName();

        // notify
        Message successMessage = MyMessengerService.buildGenericMessage(
                getMessage("text.title.success", null),
                getMessage("news.text.sub.success", null),
                null, null);
        myMessengerService.sendMessage(userId, successMessage);
        myMessengerService.sendTextMessage(userId,
                getMessage("text.compliment", new Object[] { representativeName }));
    }

    public void getNewsSubscriptionStatus(String userId) {
        User user = userDao.findByFbId(userId);
        boolean isSubscribed = (user != null) && user.isSubscribed();
        sendNewsSubscriptionStatus(userId, isSubscribed);
    }

    private void sendNewsSubscriptionStatus(String recipient, boolean isSubscribed) {
        Message message;
        QuickReply quickreply;

        if (isSubscribed) {
            message = new Message(getMessage("news.text.subscribed", null));
            quickreply = new QuickReply(getMessage("news.qr.unsub", null),
                    QR_UNSUBSCRIBE_NEWS_PAYLOAD);
        }
        else {
            message = new Message(getMessage("news.text.no_subscribed", null));
            quickreply= new QuickReply(getMessage("news.qr.sub", null),
                    QR_SUBSCRIBE_NEWS_PAYLOAD);
        }

        message.addQuickReply(quickreply);
        myMessengerService.sendMessage(recipient, message);
    }

    public Message buildNewsMessage(String title, String link) {
        Bubble bubble = new Bubble(getMessage("news.title", null));
        bubble.setSubtitle(title);

        WebButton button = new WebButton(getMessage("news.read", null), link);

        bubble.addButton(button);
        GenericTemplatePayload payload = new GenericTemplatePayload();
        payload.addBubble(bubble);
        TemplateAttachment attachment = new TemplateAttachment(payload);
        return new Message(attachment);
    }

    private String getMessage(String key, Object[] objects) {
        return messageSource.getMessage(key, objects, Locale.ENGLISH);
    }
}

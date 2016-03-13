package app.flaneurs.com.flaneurs.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by mprice on 3/12/16.
 */
@ParseClassName("InboxItem")
public class InboxItem extends ParseObject {
    public static final String KEY_INBOX_USER = "KEY_INBOX_USER";
    public static final String KEY_INBOX_DATE = "KEY_INBOX_DATE";
    public static final String KEY_INBOX_POST = "KEY_INBOX_POST";
    public static final String KEY_INBOX_NEW = "KEY_INBOX_NEW";
    public static final String KEY_INBOX_ID = "KEY_INBOX_ID";


    public String getId() {
        return getString(KEY_INBOX_ID);
    }

    public User getUser() {
        return (User)getParseUser(KEY_INBOX_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_INBOX_USER, user);
    }

    public Date getPickUpTime() {
        return getDate(KEY_INBOX_DATE);
    }

    public void setPickUpTime(Date pickUpTime) {
        put(KEY_INBOX_DATE, pickUpTime);
    }


    public Post getPost() {
        return (Post)get(KEY_INBOX_POST);
    }

    public void setPost(Post post) {
        put(KEY_INBOX_POST, post);
        put(KEY_INBOX_ID, post.getObjectId());
    }

    public boolean getNew() {
        return getBoolean(KEY_INBOX_NEW);
    }

    public void setNew(boolean isNew) {
        put(KEY_INBOX_NEW, isNew);
    }

}

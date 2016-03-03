package app.flaneurs.com.flaneurs.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by kpu on 2/29/16.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {
    private static final String KEY_COMMENT_AUTHOR = "KEY_COMMENT_AUTHOR";
    private static final String KEY_COMMENT_DATE = "KEY_COMMENT_DATE";
    private static final String KEY_COMMENT_TEXT = "KEY_COMMENT_TEXT";

    public Comment() {

    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_COMMENT_AUTHOR);
    }

    public void setAuthor(ParseUser author) {
        put(KEY_COMMENT_AUTHOR, author);
    }

    public Date getCreatedTime() {
        return getDate(KEY_COMMENT_DATE);
    }

    public void setCreatedTime(Date createdTime) {
        put(KEY_COMMENT_DATE, createdTime);
    }

    public String getCommentText() {
        return getString(KEY_COMMENT_TEXT);
    }

    public void setCommentText(String text) {
        put(KEY_COMMENT_TEXT, text);
    }
}

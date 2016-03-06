package app.flaneurs.com.flaneurs.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

/**
 * Created by kpu on 2/29/16.
 */
@ParseClassName("Post")
public class Post extends ParseObject{
    public static final String KEY_POST_AUTHOR = "KEY_POST_AUTHOR";
    private static final String KEY_POST_LOCATION = "KEY_POST_LOCATION";
    public static final String KEY_POST_DATE = "KEY_POST_DATE";
    private static final String KEY_POST_TYPE = "KEY_POST_TYPE";
    private static final String KEY_POST_MEDIAURL = "KEY_POST_MEDIAURL";
    private static final String KEY_POST_CAPTION = "KEY_POST_CAPTION";
    private static final String KEY_POST_VIEWCOUNT = "KEY_POST_VIEWCOUNT";
    private static final String KEY_POST_UPVOTECOUNT = "KEY_POST_UPVOTECOUNT";
    private static final String KEY_POST_DOWNVOTECOUNT = "KEY_POST_DOWNVOTECOUNT";
    private static final String KEY_POST_COMMENTS = "KEY_POST_COMMENTS";
    private static final String KEY_POST_IMAGE = "KEY_POST_IMAGE";

    public Post() {

    }

    public User getAuthor() {
        return (User)getParseUser(KEY_POST_AUTHOR);
    }

    public void setAuthor(ParseUser author) {
        put(KEY_POST_AUTHOR, author);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(KEY_POST_LOCATION);
    }

    public void setLocation(ParseGeoPoint location) {
        put(KEY_POST_LOCATION, location);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_POST_IMAGE);
    }

    public void setImage(ParseFile file) {
        put(KEY_POST_IMAGE, file);
    }

    public Date getCreatedTime() {
        return getDate(KEY_POST_DATE);
    }

    public void setCreatedTime(Date createdTime) {
        put(KEY_POST_DATE, createdTime);
    }

    public String getPostType() {
        return getString(KEY_POST_TYPE);
    }

    public void setPostType(String type) {
        put(KEY_POST_TYPE, type);
    }

    public String getMediaUrl() {
        return getString(KEY_POST_MEDIAURL);
    }

    public void setMediaUrl(String url) {
        put(KEY_POST_MEDIAURL, url);
    }

    public String getCaption() {
        return getString(KEY_POST_CAPTION);
    }

    public void setCaption(String caption) {
        put(KEY_POST_CAPTION, caption);
    }

    public int getViewCount() {
        return getInt(KEY_POST_VIEWCOUNT);
    }

    public void setViewCount(int viewCount) {
        put(KEY_POST_VIEWCOUNT, viewCount);
    }

    public int getUpVoteCount() {
        return getInt(KEY_POST_UPVOTECOUNT);
    }

    public void setUpVoteCount(int upVoteCount) {
        put(KEY_POST_UPVOTECOUNT, upVoteCount);
    }

    public int getDownVoteCount() {
        return getInt(KEY_POST_DOWNVOTECOUNT);
    }

    public void setDownVoteCount(int downVoteCount) {
        put(KEY_POST_DOWNVOTECOUNT, downVoteCount);
    }

    public List<Comment> getComments() {
        return getList(KEY_POST_COMMENTS);
    }

    public void setComments(List<Comment> comments) {
        put(KEY_POST_COMMENTS, comments);
    }
}

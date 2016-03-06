package app.flaneurs.com.flaneurs.models;

import com.parse.ParseClassName;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mprice on 3/5/16.
 */
@ParseClassName("_User") public class User extends ParseUser implements Serializable {
    private static final String KEY_USER_PROFILE_URL = "KEY_USER_PROFILE_URL";
    public static final String KEY_USER_INBOX = "KEY_USER_INBOX";

    public String getProfileUrl() {
        return getString(KEY_USER_PROFILE_URL);
    }

    public void setProfileUrl(String profileUrl) {
        put(KEY_USER_PROFILE_URL, profileUrl);
    }

    public List<Post> getInboxPosts() {
        List<Post> posts = (List<Post>) get(KEY_USER_INBOX);
        if (posts == null) {
            posts = new ArrayList<Post>();
            Post post = new Post();
            post.setCreatedTime(new Date());
            posts.add(post);
        }
        return posts;
    }

    public static User currentUser(){
        return (User)ParseUser.getCurrentUser();
    }
}
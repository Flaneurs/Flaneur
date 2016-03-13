package app.flaneurs.com.flaneurs.models;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mprice on 3/5/16.
 */
@ParseClassName("_User")
public class User extends ParseUser implements Serializable {
    private static final String KEY_USER_PROFILE_URL = "KEY_USER_PROFILE_URL";
    public static final String KEY_USER_INBOX = "KEY_USER_INBOX";
    public static final String KEY_USER_DROPS = "KEY_USER_DROPS";
    public static final String KEY_USER_UPVOTES = "KEY_USER_UPVOTES";


    private List<InboxItem> inboxItems;

    public String getProfileUrl() {
        return getString(KEY_USER_PROFILE_URL);
    }

    public void setProfileUrl(String profileUrl) {
        put(KEY_USER_PROFILE_URL, profileUrl);
    }

    public int getDrops() {
        return getInt(KEY_USER_DROPS);
    }

    public void incrementDrops() {
        int drops = getDrops();
        drops++;
        put(KEY_USER_DROPS, drops);
    }

    public int getUpVotes() {
        return getInt(KEY_USER_UPVOTES);
    }

    public void incrementUpVotes() {
        int upVotes = getUpVotes();
        upVotes++;
        put(KEY_USER_UPVOTES, upVotes);
    }

    public void fetchInboxPosts(final FindCallback<InboxItem> callback) {
        ParseQuery<InboxItem> query = ParseQuery.getQuery("InboxItem");

        // Restrict to cases where the author is the current user.
        query.whereEqualTo("KEY_INBOX_USER", this);
        query.include("KEY_INBOX_POST");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
        // Run the query
        query.findInBackground(new FindCallback<InboxItem>() {

            @Override
            public void done(List<InboxItem> objects, ParseException e) {
                callback.done(objects, e);
                if (e == null) {
                    inboxItems = objects;
                }
            }
        });
    }

    public List<InboxItem> getInbox() {
        return inboxItems;
    }

    public static User currentUser() {
        return (User) ParseUser.getCurrentUser();
    }
}
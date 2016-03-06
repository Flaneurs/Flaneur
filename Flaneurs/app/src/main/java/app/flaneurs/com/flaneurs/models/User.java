package app.flaneurs.com.flaneurs.models;

import com.parse.ParseClassName;
import com.parse.ParseUser;

import java.io.Serializable;

/**
 * Created by mprice on 3/5/16.
 */
@ParseClassName("_User") public class User extends ParseUser implements Serializable {
    private static final String KEY_USER_PROFILE_URL = "KEY_USER_PROFILE_URL";

    public String getProfileUrl() {
        return getString(KEY_USER_PROFILE_URL);
    }

    public void setProfileUrl(String profileUrl) {
        put(KEY_USER_PROFILE_URL, profileUrl);
    }

    public static User currentUser(){
        return (User)ParseUser.getCurrentUser();
    }
}
package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.User;

public class LoginActivity extends AppCompatActivity {

    private String mName;
    private String mPictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (User.currentUser() != null) {
            finishLogin();
        }
    }

    public void onLoginButtonClicked(View v) {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, Arrays.asList("user_status"), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("Flanuers", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("Flanuers", "User signed up and logged in through Facebook!");
                    getUserDetailsFromFB();
                }
            }
        });
    }


    public void onFakeLoginButtonClicked(View v) {
        Intent i = new Intent(this, DiscoverActivity.class);
        startActivity(i);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        finishLogin();
    }


    private void getUserDetailsFromFB() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {

                            mName = response.getJSONObject().getString("name");
                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");
                            mPictureUrl = data.getString("url");
                            saveNewUser();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void saveNewUser() {
        User parseUser = User.currentUser();
        parseUser.setUsername(mName);
        parseUser.setProfileUrl(mPictureUrl);


        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
            }
        });
    }

    private void finishLogin() {
        Intent i = new Intent(this, DiscoverActivity.class);
        startActivity(i);
        finish();
    }
}

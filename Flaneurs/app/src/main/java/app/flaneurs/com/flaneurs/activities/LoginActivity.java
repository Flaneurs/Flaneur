package app.flaneurs.com.flaneurs.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
import app.flaneurs.com.flaneurs.utils.RevealLayoutDiamond;
import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private String mName;
    private String mPictureUrl;

    @Bind(R.id.reveal_layout)
    RevealLayoutDiamond mRevealLayout;

    @Bind(R.id.reveal_view)
    View mRevealView;

    @Bind(R.id.ivLogo)
    ImageView ivLogo;

    @Bind(R.id.btnLogin)
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (User.currentUser() != null) {
            // TODO: disable for video
            //finishLogin();
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
                //TODO: Remove once we have all authed once and have better pictures
                getUserDetailsFromFB();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        btnLogin.setClickable(false);
        btnLogin.setVisibility(View.INVISIBLE);


        btnLogin.postDelayed(new Runnable() {
            @Override
            public void run () {
                int[] location = new int[2];
                ivLogo.getLocationOnScreen(location);
                location[0] += ivLogo.getWidth() / 2;
                location[1] += (ivLogo.getHeight() / 5) * 3;

                // create Intent to take a picture and return control to the calling application
                final Intent intent = new Intent(LoginActivity.this, DiscoverActivity.class);

                mRevealView.setVisibility(View.VISIBLE);
                mRevealLayout.setVisibility(View.VISIBLE);

                mRevealLayout.show(location[0], location[1]); // Expand from center of FAB. Actually, it just plays reveal animation.
                btnLogin.postDelayed(new

                                             Runnable() {
                                                 @Override
                                                 public void run() {
                                                     // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
                                                     // So as long as the result is not null, it's safe to use the intent.
                                                     if (intent.resolveActivity(getPackageManager()) != null) {
                                                         // Start the image capture intent to take photo

                                                         finishLogin();
                                                     }
                                                     /**
                                                      * Without using R.anim.hold, the screen will flash because of transition
                                                      * of Activities.
                                                      */
                                                     overridePendingTransition(0, R.anim.hold);
                                                 }
                                             }

                        , 600); // 600 is default duration of reveal animation in RevealLayout
                btnLogin.postDelayed(new

                                             Runnable() {
                                                 @Override
                                                 public void run() {
                                                     btnLogin.setClickable(true);
                                                     btnLogin.setVisibility(View.VISIBLE);
                                                     mRevealLayout.setVisibility(View.INVISIBLE);
                                                     mRevealView.setVisibility(View.INVISIBLE);
                                                 }
                                             }

                        , 960); // Or some numbers larger than 600.
            }
        }, 700);

    }

    private void getUserDetailsFromFB() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture.width(720).height(720)");
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
        startActivity(i,
                ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this).toBundle());    }
}

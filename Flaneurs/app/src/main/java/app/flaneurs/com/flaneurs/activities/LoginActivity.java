package app.flaneurs.com.flaneurs.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
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

    @Bind(R.id.tvSignUp)
    TextView tvSignUp;

    @Bind(R.id.btnLoginEmail)
    Button btnLoginEmail;

    @Bind(R.id.editText1)
    EditText editText1;

    @Bind(R.id.editText2)
    EditText editText2;

    @Bind(R.id.rootLayout)
    RelativeLayout rootLayout;

    @Bind(R.id.imageViewText)
    ImageView imageViewText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        final SpannableStringBuilder sb = new SpannableStringBuilder("Don't have an account? Sign up");

        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 22, 30, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold


        tvSignUp.setText(sb);

        // Cheat
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.include(Post.KEY_POST_AUTHOR);
        // query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (objects != null && objects.size() > 0)
                    ParseObject.pinAllInBackground(objects);
            }
        });

        ParseQuery<User> query1 = ParseQuery.getQuery("_User");
        query1.findInBackground(new FindCallback<User>() {
            @Override
            public void done(List<User> objects, ParseException e) {
                if (objects != null && objects.size() > 0)
                    Log.e("Pinning all users", "cpunt: " + objects.size());
                    ParseObject.pinAllInBackground(objects);
            }
        });

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
        btnLoginEmail.setVisibility(View.INVISIBLE);
        tvSignUp.setVisibility(View.INVISIBLE);
        editText1.setVisibility(View.INVISIBLE);
editText2.setVisibility(View.INVISIBLE);
        imageViewText.setVisibility(View.INVISIBLE);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveViewToScreenCenter(ivLogo);
            }
        }, 600/* 1sec delay */);
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


    int thingCenter = 0;

    private void moveViewToScreenCenter( View view )
    {
        RelativeLayout root = (RelativeLayout) findViewById(R.id.rootLayout );
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int statusBarOffset = dm.heightPixels - root.getMeasuredHeight();

        int originalPos[] = new int[2];
        view.getLocationOnScreen( originalPos );

        int xDest = dm.widthPixels/2;
        xDest -= (view.getMeasuredWidth()/2);
        int yDest = dm.heightPixels/2 - (view.getMeasuredHeight()/2) - statusBarOffset;
        thingCenter = yDest - originalPos[1];
        TranslateAnimation anim = new TranslateAnimation( 0, xDest - originalPos[0] , 0, yDest - originalPos[1] );
        anim.setDuration(800);

        anim.setInterpolator(new DecelerateInterpolator());
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
doCoolAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }


    void doCoolAnimation() {
        btnLogin.postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] location = new int[2];
                ivLogo.getLocationOnScreen(location);
                location[0] += ivLogo.getWidth() / 2;
                location[1] += thingCenter + (ivLogo.getHeight() / 5) * 4;

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
                                                     getWindow().setEnterTransition(new Explode());
                                                     overridePendingTransition(R.anim.explode, R.anim.hold);
                                                 }
                                             }

                        , 600); // 600 is default duration of reveal animation in RevealLayout
                btnLogin.postDelayed(new

                                             Runnable() {
                                                 @Override
                                                 public void run() {
                                                     btnLogin.setClickable(true);
                                                     //btnLogin.setVisibility(View.VISIBLE);
                                                     //mRevealLayout.setVisibility(View.INVISIBLE);
                                                     //mRevealView.setVisibility(View.INVISIBLE);
                                                 }
                                             }

                        , 960); // Or some numbers larger than 600.
            }
        }, 700);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
       // if (keyCode == KeyEvent.KEYCODE_MENU) {
            btnLogin.setClickable(true);
            btnLogin.setVisibility(View.VISIBLE);
            btnLoginEmail.setVisibility(View.VISIBLE);
            tvSignUp.setVisibility(View.VISIBLE);
            editText1.setVisibility(View.VISIBLE);
            editText2.setVisibility(View.VISIBLE);
            imageViewText.setVisibility(View.VISIBLE);
            mRevealLayout.setVisibility(View.INVISIBLE);
            mRevealView.setVisibility(View.INVISIBLE);
        ivLogo.clearAnimation();
            return true;
       // }
       // return super.onKeyUp(keyCode, event);
    }



}

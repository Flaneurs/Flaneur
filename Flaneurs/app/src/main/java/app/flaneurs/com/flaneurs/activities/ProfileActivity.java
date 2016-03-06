package app.flaneurs.com.flaneurs.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.MapStreamPagerAdapter;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.fragments.StreamFragment;
import app.flaneurs.com.flaneurs.models.User;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {


    @Bind(R.id.ivProfileImage)
    ImageView ivProfileImage;

    @Bind(R.id.tvUsername)
    TextView tvUsername;

    @Bind(R.id.vpViewPager)
    ViewPager viewPager;

    @Bind(R.id.psTabs)
    PagerSlidingTabStrip slidingTabStrip;

    public static final String USER_ID = "USER_ID";
    public static final String PROFILE_TYPE = "PROFILE_TYPE";

    public enum ProfileType {
        CURRENT_USER, OTHER_USER
    }

    private MapFragment mMapFragment;
    private StreamFragment mStreamFragment;

    private ProfileType mProfileType;
    private String mUserId;

    public ProfileType getProfileType() {
        return mProfileType;
    }

    public void setProfileType(ProfileType mProfileType) {
        this.mProfileType = mProfileType;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        extractProfileConfigurationData();
        retrieveProfileDataAndSetupView();
    }

    private void retrieveUserObject(final IOnUserObjectRetrieved callback) {
        ProfileType profileType = getProfileType();
        if (profileType == ProfileType.CURRENT_USER) {
            callback.onSuccess((User) User.getCurrentUser());
        } else if (profileType == ProfileType.OTHER_USER) {
            ParseQuery<User> query = ParseQuery.getQuery("_User");
            query.getInBackground(getUserId(), new GetCallback<User>() {
                @Override
                public void done(User object, ParseException e) {
                    if (object == null) {
                        callback.onError("There was no user id found with " + getUserId());
                    } else {
                        callback.onSuccess(object);
                    }
                }
            });
        }
    }

    private void retrieveProfileDataAndSetupView() {
        retrieveUserObject(new IOnUserObjectRetrieved() {
            @Override
            public void onSuccess(User user) {
                setupProfileView(user);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProfileView(User user) {
        mMapFragment = MapFragment.newInstance(false, null, null);
        StreamFragment.StreamConfiguration configuration = new StreamFragment.StreamConfiguration();
        configuration.setStreamType(StreamFragment.StreamType.User);
        configuration.setUser(user);
        mStreamFragment = StreamFragment.createInstance(configuration);
        viewPager.setAdapter(new MapStreamPagerAdapter(getSupportFragmentManager(), mMapFragment, mStreamFragment));
        slidingTabStrip.setViewPager(viewPager);

        Glide.with(this).load(user.getProfileUrl()).into(ivProfileImage);
        tvUsername.setText(user.getUsername());
    }

    private void extractProfileConfigurationData() {
        ProfileType profileType = (ProfileType) getIntent().getSerializableExtra(PROFILE_TYPE);
        setProfileType(profileType);
        String userId = getIntent().getStringExtra(USER_ID);
        setUserId(userId);
    }

    public interface IOnUserObjectRetrieved {
        void onSuccess(User user);
        void onError(String error);
    }
}

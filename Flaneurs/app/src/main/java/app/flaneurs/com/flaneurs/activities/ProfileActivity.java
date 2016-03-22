package app.flaneurs.com.flaneurs.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.MapStreamPagerAdapter;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.fragments.StreamFragment;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;

public class ProfileActivity extends AppCompatActivity {
    @Bind(R.id.ivProfileImage)
    com.github.siyamed.shapeimageview.DiamondImageView ivProfileImage;

    @Bind(R.id.tvDrops)
    TextView tvDrops;

    @Bind(R.id.tvUpvotes)
    TextView tvUpvotes;

    @Bind(R.id.tvDropsDesc)
    TextView tvDropsDesc;

    @Bind(R.id.tvUpvotesDesc)
    TextView tvUpvotesDesc;

    @Bind(R.id.vpViewPager)
    ViewPager viewPager;

    @Bind(R.id.psTabs)
    PagerSlidingTabStrip slidingTabStrip;

    @Bind(R.id.ivCoverPhoto)
    ImageView ivCoverPhoto;

    @Bind(R.id.tvProfileName)
    TextView tvProfileName;

    public static final String USER_ID = "USER_ID";

    private MapFragment mMapFragment;
    private StreamFragment mStreamFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        String userId = getIntent().getStringExtra(USER_ID);

        ParseQuery<Post> query1 = ParseQuery.getQuery("Post");
        query1.orderByDescending(Post.KEY_POST_DATE);

        ParseQuery<User> query2 = ParseQuery.getQuery("_User");
        query2.whereEqualTo("objectId", userId);

        query1.whereMatchesQuery(Post.KEY_POST_AUTHOR, query2);
        query1.include(Post.KEY_POST_AUTHOR);

        //query1.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query1.fromLocalDatastore();
        query1.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                setupProfileView(objects.get(0).getAuthor(), objects);
            }
        });

    }

    private void setupProfileView(User user, List<Post> posts) {
        ArrayList<ParseProxyObject> postsProxy = new ArrayList<>();
        // Build ArrayList of ParseProxyObjects to pass into MapFragment
        for (Post post : posts) {
            postsProxy.add(new ParseProxyObject(post));
        }

        mMapFragment = MapFragment.newInstance(false, false, null, postsProxy);
        StreamFragment.StreamConfiguration configuration = new StreamFragment.StreamConfiguration();
        configuration.setStreamType(StreamFragment.StreamType.User);
        configuration.setUser(user);
        mStreamFragment = StreamFragment.createInstance(configuration);
        viewPager.setAdapter(new MapStreamPagerAdapter(getSupportFragmentManager(), mMapFragment, mStreamFragment));
        slidingTabStrip.setViewPager(viewPager);
        Glide.with(this).load(user.getProfileUrl()).asBitmap().into(ivProfileImage);
        Glide.with(getApplicationContext()).load(user.getCoverPhotoUrl()).bitmapTransform(new BlurTransformation(this, 3), new ColorFilterTransformation(this, Color.argb(150, 0, 0, 0))).into(ivCoverPhoto);
        tvDrops.setText("" + user.getDrops());
        tvUpvotes.setText("" + user.getUpVotes());
        tvProfileName.setText(user.getUsername());
    }
}

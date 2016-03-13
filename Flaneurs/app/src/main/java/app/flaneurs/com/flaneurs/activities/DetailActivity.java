package app.flaneurs.com.flaneurs.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.CommentAdapter;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.models.Comment;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @Bind(R.id.rvComments)
    RecyclerView rvComments;


    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout ctCollapsingToolbar;

    CommentAdapter adapter;


    private Post mPost;
    public final static String POST_ID = "POST_ID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        ArrayList<Comment> list = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        String objectId = extras.getString(POST_ID);

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // First try to find from the cache and only then go to network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK); // or CACHE_ONLY
        // Execute the query to find the object with ID
        query.getInBackground(objectId, new GetCallback<Post>() {
            public void done(Post item, ParseException e) {
                if (e == null) {
                    mPost = item;
                    configureViewWithPost(item);
                    adapter = new CommentAdapter(DetailActivity.this, mPost);

                    rvComments.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
                    rvComments.setAdapter(adapter);
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpVoteButtonClicked(view);
            }
        });

    }

    private void configureViewWithPost(Post item) {
        loadImages(item.getImage(), ivPicturePreview);
        String locationString = Utils.getPrettyAddress(this, item.getLocation().getLatitude(), item.getLocation().getLongitude());
        ctCollapsingToolbar.setTitle(locationString);
        String caption = item.getCaption();
        ParseGeoPoint location = item.getLocation();
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        getSupportFragmentManager().beginTransaction().replace(R.id.flMap, MapFragment.newInstance(false, point, null)).commit();
    }

    private void loadImages(ParseFile thumbnail, final ImageView img) {
        if (thumbnail != null) {
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        img.setImageBitmap(bmp);
                    } else {
                    }
                }
            });
        } else {
            //img.setImageResource(R.drawable.menu);
        }
    }

    public void onUpVoteButtonClicked(View view) {
        try {
            User author = (User) mPost.getAuthor().fetchIfNeeded();
            author.incrementUpVotes();
            author.saveEventually();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mPost.incrementUpVote();
        mPost.saveEventually();
    }

}

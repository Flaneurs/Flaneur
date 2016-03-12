package app.flaneurs.com.flaneurs.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.manager.ParseManager;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import butterknife.Bind;
import butterknife.ButterKnife;

public class FlanDetailActivity extends AppCompatActivity {

    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Bind(R.id.tvCaption)
    TextView tvCaption;

    @Bind(R.id.tvUpvotes)
    TextView tvUpvotes;

    @Bind(R.id.tvDownVotes)
    TextView tvDownVotes;

    private Post mPost;
    private String mObjectIdentifier;

    public final static String POST_ID = "POST_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flan_detail);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        mObjectIdentifier = extras.getString(POST_ID);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        View v = super.onCreateView(name, context, attrs);
        ParseManager.getInstance().getPostById(mObjectIdentifier, new ParseManager.IOnPostsReceivedCallback() {
            @Override
            public void onSuccess(Post post) {
                mPost = post;
                configureViewWithPost(post);
            }

            @Override
            public void onSuccess(List<Post> posts) {

            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FlanDetailActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    private void configureViewWithPost(Post item) {
        loadImages(item.getImage(), ivPicturePreview);

        String caption = item.getCaption();
        if (caption != null) {
            tvCaption.setText(caption);
        }

        tvUpvotes.setText(mPost.getUpVoteCount() + " Upvotes");
        tvDownVotes.setText(mPost.getDownVoteCount() + " Downvotes");

        ArrayList<ParseProxyObject> postsProxy = new ArrayList<>();
        postsProxy.add(new ParseProxyObject(item));
        MapFragment.MapConfiguration configuration = new MapFragment.MapConfiguration();
        configuration.setMapType(MapFragment.MapType.Post);
        configuration.setPostId(mPost.getObjectId());
        configuration.setShouldTrackLocation(false);
        MapFragment mapFragment = MapFragment.newInstance(configuration);
        getSupportFragmentManager().beginTransaction().replace(R.id.flMap, mapFragment).commit();
        mapFragment.loadMap();
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


    public void onDownVoteButtonClicked(View view) {
        mPost.incrementDownVote();

        tvDownVotes.setText(mPost.getDownVoteCount() + " Downvotes");
        mPost.saveEventually();
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
        tvUpvotes.setText(mPost.getUpVoteCount() + " Upvotes");

        mPost.saveEventually();
    }
}

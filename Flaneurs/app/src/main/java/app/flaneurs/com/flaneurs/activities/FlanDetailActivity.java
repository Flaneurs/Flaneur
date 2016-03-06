package app.flaneurs.com.flaneurs.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
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

    public final static String POST_ID = "POST_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flan_detail);
        ButterKnife.bind(this);

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
                }
            }
        });
    }

    private void configureViewWithPost(Post item) {
        loadImages(item.getImage(), ivPicturePreview);

        String caption = item.getCaption();
        if (caption != null) {
            tvCaption.setText(caption);
        }
        Log.e("fuck", "POLICE: " + item.getUpVoteCount());
        tvUpvotes.setText(mPost.getUpVoteCount() + " Upvotes");
        tvDownVotes.setText(mPost.getDownVoteCount() + " Downvotes");
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
        mPost.incrementUpVote();
        tvUpvotes.setText(mPost.getUpVoteCount() + " Upvotes");
        mPost.saveEventually();
    }
}

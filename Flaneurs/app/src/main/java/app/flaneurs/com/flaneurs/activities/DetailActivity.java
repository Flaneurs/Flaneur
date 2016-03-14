package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
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

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.CommentAdapter;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.models.InboxItem;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.ControllableAppBarLayout;
import butterknife.Bind;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements CommentAdapter.ICommentInteractionListener {

    @Bind(R.id.rvComments)
    RecyclerView rvComments;

    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout ctCollapsingToolbar;

    @Bind(R.id.app_bar_layout)
    ControllableAppBarLayout ablLayout;

    @Bind(R.id.clRootLayout)
    CoordinatorLayout clRootLayout;

    CommentAdapter adapter;

    private Post mPost;
    private InboxItem mInboxItem;
    private boolean mIsLiked;
    private FloatingActionButton mFab;

    public final static String POST_ID = "POST_ID";
    public final static String INBOX_ID = "INBOX_ID";
    public final static String IS_NEW = "IS_NEW";
    public final static String IS_LIKED = "IS_LIKED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();

        String postId = extras.getString(POST_ID);
        String inboxId = extras.getString(INBOX_ID);

        final boolean isNew = extras.getBoolean(IS_NEW);
        mIsLiked = extras.getBoolean(IS_LIKED);
        mFab = (FloatingActionButton) findViewById(R.id.fab);

        if (postId != null) {
            configureForDiscoverItem(postId);

        } else if (inboxId != null) {
            configureForInboxItem(inboxId, isNew);
        }
    }

    private void configureForInboxItem(String inboxId, boolean isNew) {
        ablLayout.setExpanded(true, true);

        ParseQuery<InboxItem> query = ParseQuery.getQuery(InboxItem.class);
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        query.include(InboxItem.KEY_INBOX_POST);
        query.getInBackground(inboxId, new GetCallback<InboxItem>() {
            public void done(InboxItem item, ParseException e) {
                if (e == null) {
                    mInboxItem = item;
                    mPost = mInboxItem.getPost();
                    configureViewWithPost(mPost, true);
                }
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpVoteButtonClicked(view);
            }
        });

        if (mIsLiked) {
            mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_action_heart_full));
        } else {
            mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_action_heart_empty));
        }
    }

    private void configureForDiscoverItem(String postId) {
        ablLayout.setExpanded(false, false);
        mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_action_locked));
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.getInBackground(postId, new GetCallback<Post>() {
            public void done(Post item, ParseException e) {
                if (e == null) {
                    mPost = item;
                    configureViewWithPost(item, false);
                }
            }
        });
    }

    private void revealPost() {
        // TODO: Scroll up!
    }

    private void configureViewWithPost(Post item, boolean isRevealed) {
        adapter = new CommentAdapter(DetailActivity.this, mPost, DetailActivity.this, isRevealed);

        rvComments.setLayoutManager(new LinearLayoutManager(DetailActivity.this));
        rvComments.setAdapter(adapter);

        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() == 0) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        rvComments.setItemAnimator(animator);


        loadImages(item.getImage(), ivPicturePreview);
        ctCollapsingToolbar.setTitle(item.getAddress());
        ParseGeoPoint location = item.getLocation();
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        getSupportFragmentManager().beginTransaction().replace(R.id.flMap, MapFragment.newInstance(false, true, point, null)).commit();
    }

    private void loadImages(ParseFile thumbnail, final ImageView img) {
        if (thumbnail != null) {
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                      if (mInboxItem == null) {
                          // Blur image
                          bmp = Bitmap.createScaledBitmap(bmp, 10, 10, false);
                      }
                        img.setImageBitmap(bmp);
                    }
                }
            });
        }
    }

    public void onUpVoteButtonClicked(View view) {
        if (mInboxItem == null) {
            return;
        }

        mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_action_heart_full));
        adapter.onUpvote();
        if (mIsLiked) {
            return;
        }
        try {
            User author = (User) mPost.getAuthor().fetchIfNeeded();
            author.incrementUpVotes();
            author.saveEventually();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mPost.incrementUpVote();
        mPost.saveEventually();
        mInboxItem.setUpvoted(true);
        mInboxItem.saveEventually();
        adapter.onUpvote();
    }

    @Override
    public void openProfileView(User user) {
        Intent i = new Intent(this, ProfileActivity.class);
        i.putExtra(ProfileActivity.USER_ID, user.getObjectId());
        startActivity(i);
    }
}

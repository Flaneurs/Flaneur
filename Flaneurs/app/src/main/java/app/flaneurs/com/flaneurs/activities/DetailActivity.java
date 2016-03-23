package app.flaneurs.com.flaneurs.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.CommentAdapter;
import app.flaneurs.com.flaneurs.models.InboxItem;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.ControllableAppBarLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;

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

    @Bind(R.id.ivLike)
    ImageView ivLike;

    @Bind(R.id.vBgLike)
    View vBgLike;

    private Transition.TransitionListener mEnterTransitionListener;

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
        mFab.setVisibility(View.INVISIBLE);

        if (postId != null) {
            configureForDiscoverItem(postId);

        } else if (inboxId != null) {
            configureForInboxItem(inboxId, isNew);
        }

        mEnterTransitionListener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                enterReveal();
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        };
        getWindow().getEnterTransition().addListener(mEnterTransitionListener);
    }

    private void configureForInboxItem(String inboxId, boolean isNew) {
        ablLayout.setExpanded(true, true);

        ParseQuery<InboxItem> query = ParseQuery.getQuery(InboxItem.class);
     //   query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.fromLocalDatastore();
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
                animatePhotoLike();
            }
        });

        if (mIsLiked) {
            mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_favorite_white_48dp));
        } else {
            mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_favorite_border_white_48dp));
        }
    }

    private void configureForDiscoverItem(String postId) {
        ablLayout.setExpanded(false, false);
        mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_action_locked));
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        //query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
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
                return (viewHolder.getItemViewType() == 0);
            }
        };
        rvComments.setItemAnimator(animator);

        if (isRevealed) {
            Glide.with(this).load(item.getImage()).into(ivPicturePreview);
        } else {
            Glide.with(this).load(item.getImage())
                    .bitmapTransform(new BlurTransformation(this, 25, 5))
                    .into(ivPicturePreview);
        }

        ctCollapsingToolbar.setTitle(item.getAddress());
    }

    public void onUpVoteButtonClicked(View view) {
        if (mInboxItem == null) {
            return;
        }

        mFab.setImageDrawable(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_favorite_white_48dp));
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

    void enterReveal() {
        // previously invisible view
        final View myView = mFab;

        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight()) / 2;

        // create the animator for this view (the start radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }

    void exitReveal() {
        // previously visible view
        final View myView = mFab;

        // get the center for the clipping circle
        int cx = myView.getMeasuredWidth() / 2;
        int cy = myView.getMeasuredHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = myView.getWidth() / 2;

        // create the animation (the final radius is zero)
        Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
                supportFinishAfterTransition();
            }
        });

        // start the animation
        anim.start();
    }

    @Override
    public void onBackPressed() {
        exitReveal();
    }

        private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private void animatePhotoLike() {

            vBgLike.setVisibility(View.VISIBLE);
           ivLike.setVisibility(View.VISIBLE);

           vBgLike.setScaleY(0.1f);
           vBgLike.setScaleX(0.1f);
           vBgLike.setAlpha(1f);
           ivLike.setScaleY(0.1f);
           ivLike.setScaleX(0.1f);

            AnimatorSet animatorSet = new AnimatorSet();
           // likeAnimations.put(holder, animatorSet);

            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(vBgLike, "scaleY", 0.1f, 1f);
            bgScaleYAnim.setDuration(200);
            bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(vBgLike, "scaleX", 0.1f, 1f);
            bgScaleXAnim.setDuration(200);
            bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(vBgLike, "alpha", 1f, 0f);
            bgAlphaAnim.setDuration(200);
            bgAlphaAnim.setStartDelay(150);
            bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 0.1f, 1f);
            imgScaleUpYAnim.setDuration(300);
            imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 0.1f, 1f);
            imgScaleUpXAnim.setDuration(300);
            imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(ivLike, "scaleY", 1f, 0f);
            imgScaleDownYAnim.setDuration(300);
            imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(ivLike, "scaleX", 1f, 0f);
            imgScaleDownXAnim.setDuration(300);
            imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
            animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                   // resetLikeAnimationState(holder);
                }
            });
            animatorSet.start();
        }
    }


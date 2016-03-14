package app.flaneurs.com.flaneurs.adapters;

/**
 * Created by mprice on 3/9/16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ocpsoft.pretty.time.PrettyTime;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Comment;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Comment> mComments;

    private Post mPost;
    private ICommentInteractionListener mListener;
    private Context mContext;
    private static int HEADER_POSITION = 0;
    private boolean mIsRevealed;

    private final int HEADER = 0, COMMENT = 1, FOOTER = 2;

    public CommentAdapter(Context context, Post post, ICommentInteractionListener listener, boolean isRevealed) {
        mIsRevealed = isRevealed;
        mPost = post;
        mListener = listener;
        mContext = context;
        mComments = mPost.getComments();

        if (mComments == null) {
            mComments = new ArrayList<>();
            mPost.fetchComments(new FindCallback<Comment>() {
                @Override
                public void done(List<Comment> objects, ParseException e) {
                    if (e == null) {
                        mComments = objects;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == HEADER_POSITION) {
            return HEADER;
        } else if (position <= mComments.size()) {
            return COMMENT;
        } else {
            return FOOTER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case HEADER:
                View v1 = inflator.inflate(R.layout.detail_view_item_header, parent, false);
                viewHolder = new HeaderViewHolder(v1);
                break;
            default:
            case COMMENT:
                View v2 = inflator.inflate(R.layout.detail_view_item_comment, parent, false);
                viewHolder = new CommentViewHolder(v2);
                break;
            case FOOTER:
                View v3 = inflator.inflate(R.layout.detail_view_item_footer, parent, false);
                viewHolder = new FooterViewHolder(v3, new FooterViewHolder.ICommentAddListener() {
                    @Override
                    public void onAddComment(String commentString) {
                        Comment comment = new Comment();

                        User currentUser = User.currentUser();

                        comment.setAuthor(currentUser);
                        comment.setCommentText(commentString);
                        comment.setCreatedTime(new Date());
                        comment.setPost(mPost);
                        comment.saveInBackground();
                        mComments.add(comment);
                        notifyItemInserted(mComments.size());
                    }
                });
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position, List<Object> payloads) {
        switch (holder.getItemViewType()) {
            case HEADER:
                if (payloads != null && !payloads.isEmpty()) {
                    updateHeaderView((HeaderViewHolder) holder, mPost);
                }
                configureHeaderView((HeaderViewHolder) holder, mPost);
                break;
            case COMMENT:
                final Comment comment = mComments.get(position - 1);
                configureCommentView((CommentViewHolder) holder, comment);
                break;
            case FOOTER:
                configureFooterView((FooterViewHolder) holder, User.currentUser());
                break;
        }
    }

    private void updateHeaderView(HeaderViewHolder view, Post post) {
        view.tvUpvotes.setText(post.getUpVoteCount() + " upvotes");
    }

    private void configureHeaderView(HeaderViewHolder view, final Post post) {
        User author = post.getAuthor();
        if (author != null) {
            try {
                author.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String username = author.getUsername();
            view.tvUsername.setText(username);
            view.ivProfileImage.setImageResource(0);
            if (author.getProfileUrl() != null) {
                Glide.with(mContext).load(author.getProfileUrl()).into(view.ivProfileImage);
            }
            view.ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.openProfileView(post.getAuthor());
                }
            });
        }
        PrettyTime pt = new PrettyTime();
        view.tvCreationTime.setText(pt.format(post.getCreatedTime()));
        view.tvUpvotes.setText(post.getUpVoteCount() + " upvotes");
        view.tvViewCount.setText(post.getViewCount() + " views");
        view.tvStreamDistanceAway.setText("");
        view.tvLocation.setText(post.getAddress());

        if (mIsRevealed) {
            view.tvCaption.setText(post.getCaption());
        } else {
            view.tvCaption.setText("Go find this drop to see its contents!");
        }
    }

    private void configureCommentView(CommentViewHolder view, final Comment comment) {
        try {
            User user = (User) comment.getAuthor().fetchIfNeeded();
            Glide.with(mContext).load(user.getProfileUrl()).into(view.ivProfileImage);
            view.tvUsername.setText(user.getUsername());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        view.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openProfileView(comment.getAuthor());
            }
        });

        view.tvCreationTime.setText(Utils.getPrettyTime(comment.getCreatedTime()));
        view.tvComment.setText(comment.getCommentText());
    }

    private void configureFooterView(FooterViewHolder view, User user) {
        view.ivProfileImage.setImageResource(0);
        if (user.getProfileUrl() != null) {
            Glide.with(mContext).load(user.getProfileUrl()).into(view.ivProfileImage);
        }
    }


    @Override
    public int getItemCount() {
        return mComments.size() + 2;
    }

    public void onUpvote() {
        notifyItemChanged(0, new Boolean(true));
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivProfileImage)
        ImageView ivProfileImage;

        @Bind(R.id.tvUsername)
        TextView tvUsername;

        @Bind(R.id.tvCommentCreationTime)
        TextView tvCreationTime;

        @Bind(R.id.tvComment)
        TextView tvComment;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            ivProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }


    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvCaption)
        TextView tvCaption;

        @Bind(R.id.ivProfileImage)
        ImageView ivProfileImage;

        @Bind(R.id.tvUsername)
        TextView tvUsername;

        @Bind(R.id.tvStreamCreationTime)
        TextView tvCreationTime;

        @Bind(R.id.tvStreamLocation)
        TextView tvLocation;

        @Bind(R.id.tvStreamUpvotes)
        TextView tvUpvotes;

        @Bind(R.id.tvStreamViewCount)
        TextView tvViewCount;

        @Bind(R.id.tvStreamDistanceAway)
        TextView tvStreamDistanceAway;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.etComment)
        EditText etComment;

        @Bind(R.id.ivProfileImage)
        ImageView ivProfileImage;

        private ICommentAddListener mListener;

        public FooterViewHolder(View itemView, ICommentAddListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mListener = listener;

            etComment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        mListener.onAddComment(etComment.getText().toString());

                        InputMethodManager imm = (InputMethodManager) etComment.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                        etComment.setText("");
                        return true;
                    }
                    return false;
                }
            });

        }

        public interface ICommentAddListener {
            void onAddComment(String comment);
        }
    }

    public interface ICommentInteractionListener {
        void openProfileView(User user);
    }
}

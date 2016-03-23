package app.flaneurs.com.flaneurs.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.InboxItem;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by kamranpirwani on 3/5/16.
 */
public class InboxArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<InboxItem> mInboxItems;
    private IInboxInteractionListener mListener;
    private Context mContext;

    private final int NEW_HEADER = 0, NEW_ITEM = 1, OLD_HEADER = 2, OLD_ITEM = 3;

    public int mNewItemCount = 0;

    InboxViewHolder.IMyViewHolderClicks mClickListener = new InboxViewHolder.IMyViewHolderClicks() {
        @Override
        public void onInboxClicked(InboxViewHolder caller, int position) {
            InboxItem flan = mInboxItems.get(position);
            mListener.openInboxDetailView(flan, caller);
        }
    };

    public InboxArrayAdapter(Context context, List<InboxItem> flans, IInboxInteractionListener listener) {
        mInboxItems = flans;
        mListener = listener;
        mContext = context;

        // TODO: Make more efficient
        if (mInboxItems != null) {
            for (InboxItem item : mInboxItems) {
                if (item.getNew()) {
                    Log.e("test", "Adding new test");
                    mNewItemCount++;
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case NEW_HEADER:
            case OLD_HEADER:
                View v1 = inflator.inflate(R.layout.flan_inbox_header_item, parent, false);
                viewHolder = new HeaderViewHolder(v1);
                break;
            default:
            case NEW_ITEM:
                View v2 = inflator.inflate(R.layout.flan_inbox_item, parent, false);
                viewHolder = new InboxViewHolder(v2, mClickListener, true);
                break;
            case OLD_ITEM:
                View v3 = inflator.inflate(R.layout.flan_inbox_item, parent, false);
                viewHolder = new InboxViewHolder(v3, mClickListener, false);
                break;

        }

        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == NEW_HEADER) {
            return NEW_HEADER;
        } else if (position <= mNewItemCount) {
            return NEW_ITEM;
        } else if (position == mNewItemCount + 1) {
            return OLD_HEADER;
        } else {
            return OLD_ITEM;
        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case NEW_HEADER:
            case OLD_HEADER:
                configureHeaderView((HeaderViewHolder) holder, position);
                break;
            default:
            case NEW_ITEM:
                final InboxItem inboxItem = mInboxItems.get(position - 1);
                final Post post = inboxItem.getPost();
                final User author = post.getAuthor();

                configureInboxView((InboxViewHolder) holder, post, author, inboxItem, true);
                break;
            case OLD_ITEM:
                final InboxItem inboxItem1 = mInboxItems.get(position - 2);
                final Post post1 = inboxItem1.getPost();
                final User author1 = post1.getAuthor();
                
                configureInboxView((InboxViewHolder) holder, post1, author1, inboxItem1, false);
                break;

        }
    }

    private void configureInboxView(InboxViewHolder holder, Post post, User author, InboxItem inboxItem, boolean isNew) {
        if (isNew) {
            holder.ivImageThumb.setVisibility(View.GONE);
            holder.tvCaption.setVisibility(View.GONE);

        } else {
            holder.ivImageThumb.setVisibility(View.VISIBLE);
            holder.tvCaption.setVisibility(View.VISIBLE);



        }

        String imageUrl = post.getImage();
        Glide.with(mContext)
                .load(imageUrl)
                .into(holder.ivImageThumb);

        String username = author.getUsername();
        holder.tvUsername.setText(username);
        Glide.with(mContext).load(author.getProfileUrl()).into(holder.ivInboxImage);

        holder.tvCreationTime.setText(Utils.getPrettyTime(post.getCreatedTime()));
        holder.tvLocation.setText(post.getAddress());
        holder.tvCaption.setText(post.getCaption());
        if (inboxItem.getNew() == true) {
            holder.ivNew.setVisibility(View.VISIBLE);
        } else {
            holder.ivNew.setVisibility(View.GONE);
        }
    }

    private void configureHeaderView(HeaderViewHolder holder, int position) {
        if (position > 0) {
            holder.tvHeader.setText("Older Pickups");
        } else {
            holder.tvHeader.setText("New");
        }
    }

    public void remove(int position) {
        InboxItem item = mInboxItems.remove(position - 2);
        item.setHidden(true);
        item.saveEventually();
        FlaneurApplication.getInstance().pickupService.onHide(item);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        if (mInboxItems == null) {
            return 0;
        }

        return mInboxItems.size() + 2;
    }

    public static class InboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.ivInboxImage)
        public ImageView ivInboxImage;

        @Bind(R.id.ivNew)
        public ImageView ivNew;

        @Bind(R.id.tvInboxUsername)
        public TextView tvUsername;

        @Bind(R.id.tvInboxCreationTime)
        public TextView tvCreationTime;

        @Bind(R.id.tvInboxLocation)
        public TextView tvLocation;

        @Bind(R.id.ivImageThumb)
        public ImageView ivImageThumb;

        @Bind(R.id.tvCaption)
        public TextView tvCaption;

        IMyViewHolderClicks mListener;
        boolean mIsNew;

        public InboxViewHolder(View itemView, IMyViewHolderClicks listener, boolean isNew) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            mListener = listener;
            mIsNew = isNew;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (mIsNew) {
                position -= 1;
            } else {
                position -= 2;

            }
            mListener.onInboxClicked(this, position);
            ivNew.setVisibility(View.GONE);
        }

        public interface IMyViewHolderClicks {
            void onInboxClicked(InboxViewHolder caller, int position);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvHeader)
        TextView tvHeader;


        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface IInboxInteractionListener {
        void openInboxDetailView(InboxItem flan, InboxViewHolder view);
    }
}

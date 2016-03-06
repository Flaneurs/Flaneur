package app.flaneurs.com.flaneurs.adapters;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ocpsoft.pretty.time.PrettyTime;
import com.parse.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by mprice on 2/29/16.
 */
public class FlanArrayAdapter extends RecyclerView.Adapter<FlanArrayAdapter.FlanViewHolder> {


    private List<Post> mFlans; // TODO: make model object
    private IFlanInteractionListener mListener;
    private Context mContext;

    public FlanArrayAdapter(Context context, List<Post> flans, IFlanInteractionListener listener) {
        mFlans = flans;
        mListener = listener;
        mContext = context;
    }

    @Override
    public FlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.flan_stream_item, parent, false);
        FlanViewHolder viewHolder = new FlanViewHolder(view, new FlanViewHolder.IMyViewHolderClicks() {
            @Override
            public void onFlanClicked(View caller, int position) {
                Post flan = mFlans.get(position);
                mListener.openDetailView(flan);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FlanViewHolder holder, final int position) {
        final Post flan = mFlans.get(position);

        User author = flan.getAuthor();
        if (author != null) {
            try {
                author.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String username = author.getUsername();
            holder.tvUsername.setText(username);
            if (author.getProfileUrl() != null) {
                Glide.with(mContext).load(author.getProfileUrl()).into(holder.ivProfileImage);
            }

        }
        holder.ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Post flan = mFlans.get(position);
                mListener.openProfileView(flan);
            }
        });
        PrettyTime pt = new PrettyTime();
        holder.tvCreationTime.setText(pt.format(flan.getCreatedTime()));
        holder.tvDownvotes.setText(flan.getDownVoteCount() + " downvotes");
        holder.tvUpvotes.setText(flan.getUpVoteCount() + " upvotes");
        holder.tvViewCount.setText(flan.getViewCount() + " views");

        String address = this.getPrettyAddress(flan.getLocation().getLatitude(), flan.getLocation().getLongitude());
        if (address != null) {
            holder.tvLocation.setText(address);
        }
    }

    @Override
    public int getItemCount() {
        return mFlans.size();
    }

    public static class FlanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        @Bind(R.id.tvStreamDownvotes)
        TextView tvDownvotes;

        @Bind(R.id.tvStreamViewCount)
        TextView tvViewCount;


        IMyViewHolderClicks mListener;

        public FlanViewHolder(View itemView, IMyViewHolderClicks listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            mListener.onFlanClicked(v, getAdapterPosition());
        }

        public interface IMyViewHolderClicks {
            void onFlanClicked(View caller, int position);
        }
    }

    public interface IFlanInteractionListener {
        void openDetailView(Post flan);
        void openProfileView(Post flan);
    }

    public String getPrettyAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(mContext, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null) {
            Address bestMatch = (addresses.isEmpty() ? null : addresses.get(0));
            return bestMatch.getAddressLine(0).toString();
        }
        return "";
    }
}

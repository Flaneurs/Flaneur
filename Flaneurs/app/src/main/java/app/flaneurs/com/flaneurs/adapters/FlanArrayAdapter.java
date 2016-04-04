package app.flaneurs.com.flaneurs.adapters;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ocpsoft.pretty.time.PrettyTime;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.LocationProvider;
import app.flaneurs.com.flaneurs.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by mprice on 2/29/16.
 */
public class FlanArrayAdapter extends RecyclerView.Adapter<FlanArrayAdapter.FlanViewHolder> implements LocationProvider.ILocationListener {
    private List<Post> mFlans;
    private IFlanInteractionListener mListener;
    private Context mContext;
    private Location mCurrentLocation;

    public FlanArrayAdapter(Context context, List<Post> flans, IFlanInteractionListener listener) {
        mFlans = flans;
        mListener = listener;
        mContext = context;

        FlaneurApplication.getInstance().locationProvider.addListener(this);
        mCurrentLocation = FlaneurApplication.getInstance().locationProvider.getCurrentLocation();
    }

    @Override
    public FlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.flan_stream_item, parent, false);
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
    public void onBindViewHolder(final FlanViewHolder holder, final int position) {
        final Post flan = mFlans.get(position);

        final User author = flan.getAuthor();
        holder.ivProfileImage.setImageResource(0);
        if (author.isDataAvailable()) {
            String username = author.getUsername();
            holder.tvUsername.setText(username);
            Glide.with(mContext).load(author.getProfileUrl()).asBitmap().into(holder.ivProfileImage);
        } else {
            author.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    String username = author.getUsername();
                    holder.tvUsername.setText(username);
                    Glide.with(mContext).load(author.getProfileUrl()).asBitmap().into(holder.ivProfileImage);
                }
            });
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
        holder.tvUpvotes.setText(flan.getUpVoteCount() + "");
        holder.tvViewCount.setText(flan.getViewCount() + "");

        String pretty = Utils.getPrettyDistance(mCurrentLocation, flan.getLocation());
        String locationString = flan.getAddress() + " (" + pretty +  ")";
//        holder.tvStreamDistanceAway.setText(pretty);
        SpannableString str = new SpannableString(locationString);
        str.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.divider)), flan.getAddress().length() + 1, locationString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvLocation.setText(str);
    }

    @Override
    public int getItemCount() {
        return mFlans.size();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mCurrentLocation == null || location.distanceTo(mCurrentLocation) > 10) {
            mCurrentLocation = location;
            notifyDataSetChanged();
        }
    }

    public static class FlanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.rlBackground)
        RelativeLayout rlBackground;

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
}

package app.flaneurs.com.flaneurs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ocpsoft.pretty.time.PrettyTime;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.List;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by mprice on 2/29/16.
 */
public class FlanArrayAdapter extends RecyclerView.Adapter<FlanArrayAdapter.FlanViewHolder> {


    private List<Post> mFlans; // TODO: make model object
    private IFlanInteractionListener mListener;

    public FlanArrayAdapter(List<Post> flans, IFlanInteractionListener listener) {
        mFlans = flans;
        mListener = listener;
    }

    @Override
    public FlanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.flan_stream_item, parent, false);
        return new FlanViewHolder(view, new FlanViewHolder.IMyViewHolderClicks() {
            @Override
            public void onFlanClicked(View caller, int position) {
                Post flan = mFlans.get(position);
                mListener.openDetailView(flan);
            }
        });
    }

    @Override
    public void onBindViewHolder(FlanViewHolder holder, int position) {
        final Post flan = mFlans.get(position);

        ParseUser author = flan.getAuthor();
        if (author != null) {
            try {
                author.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String username = author.getUsername();
            holder.tvUsername.setText(username);
        }
        PrettyTime pt = new PrettyTime();
        holder.tvCreationTime.setText(pt.format(flan.getCreatedTime()));
        holder.tvDownvotes.setText(flan.getDownVoteCount() + " downvotes");
        holder.tvUpvotes.setText(flan.getUpVoteCount() + " upvotes");
        holder.tvViewCount.setText(flan.getViewCount() + " views");
        ParseGeoPoint locationPoint = flan.getLocation();
        if (locationPoint != null) {
            String location = locationPoint.toString();
            holder.tvLocation.setText(location);
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
    }
}

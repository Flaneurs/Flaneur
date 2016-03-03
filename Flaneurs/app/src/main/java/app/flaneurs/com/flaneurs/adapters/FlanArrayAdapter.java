package app.flaneurs.com.flaneurs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.flaneurs.com.flaneurs.R;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by mprice on 2/29/16.
 */
public class FlanArrayAdapter extends RecyclerView.Adapter<FlanArrayAdapter.FlanViewHolder> {


    private List<String> mFlans; // TODO: make model object
    private IFlanInteractionListener mListener;

    public FlanArrayAdapter(List<String> flans, IFlanInteractionListener listener) {
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
                String model = mFlans.get(position);
                mListener.openDetailView(model);
            }
        });
    }

    @Override
    public void onBindViewHolder(FlanViewHolder holder, int position) {
        final String flan = mFlans.get(position);

        holder.tvUsername.setText(flan);
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
        void openDetailView(String flan);
    }


}

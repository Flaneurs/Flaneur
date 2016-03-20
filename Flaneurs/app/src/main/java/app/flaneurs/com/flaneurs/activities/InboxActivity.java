package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.InboxArrayAdapter;
import app.flaneurs.com.flaneurs.models.InboxItem;
import app.flaneurs.com.flaneurs.utils.DividerItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;

public class InboxActivity extends AppCompatActivity implements InboxArrayAdapter.IInboxInteractionListener {

    @Bind(R.id.rvInboxItems)
    RecyclerView rvInboxItems;

    protected List<InboxItem> mInboxItems;
    protected InboxArrayAdapter mAdapter;
    protected LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        ButterKnife.bind(this);


        mInboxItems = FlaneurApplication.getInstance().pickupService.getInbox();

        mAdapter = new InboxArrayAdapter(this, mInboxItems, this);
        mLayoutManager = new LinearLayoutManager(this);
        rvInboxItems.setLayoutManager(mLayoutManager);
        rvInboxItems.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        rvInboxItems.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                mAdapter.remove(viewHolder.getAdapterPosition());
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rvInboxItems);
    }

    @Override
    public void openInboxDetailView(InboxItem item, InboxArrayAdapter.InboxViewHolder view) {
        boolean isNew = item.getNew();
        boolean isLiked = item.getUpvoted();
        item.setNew(false);
        item.saveEventually();

        FlaneurApplication.getInstance().pickupService.decrementNew();

        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra(DetailActivity.INBOX_ID, item.getObjectId());
        i.putExtra(DetailActivity.IS_NEW, isNew);
        i.putExtra(DetailActivity.IS_LIKED, isLiked);

        Pair<View, String> p3 = Pair.create((View) view.ivImageThumb, "image");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, p3);

        startActivity(i, options.toBundle());
    }
}

package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.InboxArrayAdapter;
import app.flaneurs.com.flaneurs.models.InboxItem;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.DividerItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

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

        User currentUser = User.currentUser();

        mInboxItems = FlaneurApplication.getInstance().pickupService.getInbox();

        if (mInboxItems == null) {
            mInboxItems = new ArrayList<>();
            currentUser.fetchInboxPosts(new FindCallback<InboxItem>() {
                @Override
                public void done(List<InboxItem> objects, ParseException e) {
                    if (e == null) {
                        mInboxItems = objects;
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        mAdapter = new InboxArrayAdapter(this, mInboxItems, this);

        mLayoutManager = new LinearLayoutManager(this);
        rvInboxItems.setLayoutManager(mLayoutManager);

        rvInboxItems.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(mAdapter);
        alphaAdapter.setFirstOnly(false);

        rvInboxItems.setAdapter(alphaAdapter);
    }

    @Override
    public void openInboxDetailView(InboxItem item) {
        boolean isNew = item.getNew();
        item.setNew(false);
        item.saveEventually();

        Intent i = new Intent(this, DetailActivity.class);
        i.putExtra(DetailActivity.POST_ID, item.getPost().getObjectId());
        i.putExtra(DetailActivity.IS_NEW, isNew);
        startActivity(i);
    }
}

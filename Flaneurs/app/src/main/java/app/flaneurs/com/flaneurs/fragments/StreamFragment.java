package app.flaneurs.com.flaneurs.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.utils.DividerItemDecoration;
import app.flaneurs.com.flaneurs.activities.FlanDetailActivity;
import app.flaneurs.com.flaneurs.adapters.FlanArrayAdapter;
import app.flaneurs.com.flaneurs.models.Post;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class StreamFragment extends Fragment implements FlanArrayAdapter.IFlanInteractionListener{

    @Bind(R.id.lvFlans)
    RecyclerView lvFlans;

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    protected ArrayList<Post> mFlans;
    protected FlanArrayAdapter adapter;
    protected LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlans = new ArrayList<>();
        setupDatasource();
        adapter = new FlanArrayAdapter(mFlans, this);
    }

    private void setupDatasource() {
        Post post = new Post();
        post.setCaption("Flan1");
        post.setCreatedTime(new Date());
        post.setDownVoteCount(23);
        post.setUpVoteCount(20);
        mFlans.add(post);
        post = new Post();
        post.setCaption("Flan2");
        post.setCreatedTime(new Date());
        post.setDownVoteCount(13);
        post.setUpVoteCount(2);
        mFlans.add(post);
        post = new Post();
        post.setCaption("Flan3");
        post.setCreatedTime(new Date());
        post.setDownVoteCount(4);
        post.setUpVoteCount(17);
        mFlans.add(post);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stream, container, false);
        ButterKnife.bind(this, v);
        setupRecyclerView();
        return v;
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        lvFlans.setLayoutManager(layoutManager);

        lvFlans.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        alphaAdapter.setFirstOnly(false);

        lvFlans.setAdapter(alphaAdapter);
    }

    @Override
    public void openDetailView(Post flan) {
        Intent i = new Intent(getActivity(), FlanDetailActivity.class);
        startActivity(i);
    }
}

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
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.utils.DividerItemDecoration;
import app.flaneurs.com.flaneurs.activities.FlanDetailActivity;
import app.flaneurs.com.flaneurs.adapters.FlanArrayAdapter;
import app.flaneurs.com.flaneurs.models.Post;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class StreamFragment extends Fragment implements FlanArrayAdapter.IFlanInteractionListener{

    @Bind(R.id.rvFlans)
    RecyclerView rvFlans;

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @Bind(R.id.pbStreamLoading)
    ProgressBar pbStreamLoading;

    protected ArrayList<Post> mFlans;
    protected FlanArrayAdapter adapter;
    protected LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlans = new ArrayList<>();
        adapter = new FlanArrayAdapter(getContext(), mFlans, this);
    }

    private void setupFakeDatasource() {
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

    private void grabDataFromParse() {
        showLoadingDataState();
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.orderByDescending("KEY_POST_DATE");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                mFlans.addAll(objects);
                adapter.notifyDataSetChanged();
                hideLoadingDataState();
            }
        });
    }

    private void showLoadingDataState() {
        setRecycleViewHidden(true);
        setProgressBarHidden(false);
    }

    private void hideLoadingDataState() {
        setRecycleViewHidden(false);
        setProgressBarHidden(true);
    }

    private void setRecycleViewHidden(boolean hidden) {
        if (hidden) {
            rvFlans.setVisibility(View.GONE);
        } else {
            rvFlans.setVisibility(View.VISIBLE);
        }
    }

    private void setProgressBarHidden(boolean hidden) {
        if (hidden) {
            pbStreamLoading.setVisibility(View.GONE);
        } else {
            pbStreamLoading.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stream, container, false);
        ButterKnife.bind(this, v);
        setupRecyclerView();
        grabDataFromParse();
        return v;
    }

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(getContext());
        rvFlans.setLayoutManager(layoutManager);

        rvFlans.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        alphaAdapter.setFirstOnly(false);

        rvFlans.setAdapter(alphaAdapter);
    }

    @Override
    public void openDetailView(Post flan) {
        Intent i = new Intent(getActivity(), FlanDetailActivity.class);
        startActivity(i);
    }
}

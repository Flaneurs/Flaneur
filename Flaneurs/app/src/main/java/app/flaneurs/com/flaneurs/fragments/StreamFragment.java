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
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.activities.ProfileActivity;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.DividerItemDecoration;
import app.flaneurs.com.flaneurs.activities.FlanDetailActivity;
import app.flaneurs.com.flaneurs.adapters.FlanArrayAdapter;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.utils.DividerItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class StreamFragment extends Fragment implements FlanArrayAdapter.IFlanInteractionListener {

    @Bind(R.id.rvFlans)
    RecyclerView rvFlans;

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    @Bind(R.id.pbStreamLoading)
    ProgressBar pbStreamLoading;

    public static String STREAM_CONFIGURATION_KEY = "STREAM_CONFIGURATION_KEY";

    private StreamConfiguration mStreamConfiguration;

    public StreamConfiguration getStreamConfiguration() {
        return mStreamConfiguration;
    }

    public static class StreamConfiguration implements Serializable {
        private StreamType mStreamType;
        private ParseUser mUser;

        public StreamType getStreamType() {
            return mStreamType;
        }

        public ParseUser getUser() {
            return mUser;
        }

        public void setStreamType(StreamType mStreamType) {
            this.mStreamType = mStreamType;
        }

        public void setUser(ParseUser mUser) {
            this.mUser = mUser;
        }
    }

    public enum StreamType {
        AllPosts, User
    }

    public static StreamFragment createInstance(StreamConfiguration configuration) {
        StreamFragment streamFragment = new StreamFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(STREAM_CONFIGURATION_KEY, configuration);
        streamFragment.setArguments(bundle);
        return streamFragment;
    }

    protected ArrayList<Post> mFlans;
    protected FlanArrayAdapter adapter;
    protected LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlans = new ArrayList<>();
        adapter = new FlanArrayAdapter(getContext(), mFlans, this);
    }

    private void grabDataFromParse(boolean hideProgressBar) {
        showLoadingDataState(hideProgressBar);
        StreamConfiguration streamConfiguration = getStreamConfiguration();
        if (streamConfiguration.getStreamType() == StreamType.AllPosts) {
            grabAllPostsFromParse();
        } else if (streamConfiguration.getStreamType() == StreamType.User) {
            grabAllPostsForUser();
        }
    }

    private void grabAllPostsFromParse() {
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.orderByDescending("KEY_POST_DATE");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                onParseResultsReceived(objects);
            }
        });
    }

    private void grabAllPostsForUser() {
        ParseUser user = getStreamConfiguration().getUser();
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.orderByDescending(Post.KEY_POST_DATE);
        query.whereEqualTo(Post.KEY_POST_AUTHOR, user);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                onParseResultsReceived(objects);
            }
        });
    }

    private void onParseResultsReceived(List<Post> posts) {
        swipeContainer.setRefreshing(false);
        mFlans.clear();
        mFlans.addAll(posts);
        adapter.notifyDataSetChanged();
        hideLoadingDataState();
    }

    private void showLoadingDataState(boolean hideProgressBar) {
        setRecycleViewHidden(true);
        setProgressBarHidden(hideProgressBar);
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
        retrieveStreamConfiguration();
        setupRecyclerView();
        setupPullToRefresh();
        grabDataFromParse(false);
        return v;
    }

    private void retrieveStreamConfiguration() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mStreamConfiguration = (StreamConfiguration) bundle.getSerializable(STREAM_CONFIGURATION_KEY);
        }
    }

    private void setupPullToRefresh() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                grabDataFromParse(true);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
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
        i.putExtra(FlanDetailActivity.POST_ID, flan.getObjectId());
        startActivity(i);
    }

    @Override
    public void openProfileView(Post flan) {
        User user = (User) flan.getAuthor();
        if (user == null) {
            Toast.makeText(getActivity(), "The user you are looking for was not a valid User", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(getActivity(), ProfileActivity.class);
        i.putExtra(ProfileActivity.PROFILE_TYPE, ProfileActivity.ProfileType.OTHER_USER);
        i.putExtra(ProfileActivity.USER_ID, user.getObjectId());
        startActivity(i);
    }
}

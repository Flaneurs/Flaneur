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

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.activities.FlanDetailActivity;
import app.flaneurs.com.flaneurs.adapters.FlanArrayAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class StreamFragment extends Fragment implements FlanArrayAdapter.IFlanInteractionListener{

    @Bind(R.id.lvFlans)
    RecyclerView lvFlans;

    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeContainer;

    protected ArrayList<String> mFlans;
    protected FlanArrayAdapter adapter;
    protected LinearLayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlans = new ArrayList<>();
        mFlans.add("Flan1");
        mFlans.add("Flan2");
        mFlans.add("Flan3");
        mFlans.add("Flan4");
        mFlans.add("Flan5");
        mFlans.add("Flan6");
        adapter = new FlanArrayAdapter(mFlans, this);
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

        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        alphaAdapter.setFirstOnly(false);

        lvFlans.setAdapter(alphaAdapter);
    }

    @Override
    public void openDetailView(String flan) {
        Intent i = new Intent(getActivity(), FlanDetailActivity.class);

        startActivity(i);
    }
}

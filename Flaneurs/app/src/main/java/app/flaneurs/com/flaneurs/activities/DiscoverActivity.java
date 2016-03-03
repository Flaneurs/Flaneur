package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.fragments.StreamFragment;
import butterknife.Bind;
import butterknife.ButterKnife;

public class DiscoverActivity extends AppCompatActivity {

    @Bind(R.id.vpViewPager)
    ViewPager viewPager;

    @Bind(R.id.psTabs)
    PagerSlidingTabStrip slidingTabStrip;

    private MapFragment mMapFragment;
    private StreamFragment mStreamFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        ButterKnife.bind(this);

        mMapFragment = new MapFragment();
        mStreamFragment = new StreamFragment();
        viewPager.setAdapter(new DiscoverPagerAdapter(getSupportFragmentManager(), mMapFragment, mStreamFragment));

        slidingTabStrip.setViewPager(viewPager);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(DiscoverActivity.this, ComposeActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_discover, menu);
        return true;
    }

   public void onProfileViewOnClick(MenuItem mi) {
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

   public void onInboxViewOnClick(MenuItem mi) {
        Intent i = new Intent(this, InboxActivity.class);
        startActivity(i);
    }

    public class DiscoverPagerAdapter extends FragmentPagerAdapter {
        private String[] pageTitles = {"Map", "List"};

        Fragment mFragment1;
        Fragment mFragment2;

        public DiscoverPagerAdapter(FragmentManager fm, Fragment fragment1, Fragment fragment2) {
            super(fm);

            mFragment1 = fragment1;
            mFragment2 = fragment2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mFragment1;
                case 1:
                    return mFragment2;
            }
            return null;
        }

        @Override
        public int getCount() {
            return pageTitles.length;
        }
    }
}

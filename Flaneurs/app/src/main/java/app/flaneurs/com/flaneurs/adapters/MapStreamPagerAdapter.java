package app.flaneurs.com.flaneurs.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by mprice on 3/5/16.
 */
public class MapStreamPagerAdapter extends FragmentPagerAdapter {
    private String[] pageTitles = {"Map", "List"};

    Fragment mFragment1;
    Fragment mFragment2;

    public MapStreamPagerAdapter(FragmentManager fm, Fragment fragment1, Fragment fragment2) {
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
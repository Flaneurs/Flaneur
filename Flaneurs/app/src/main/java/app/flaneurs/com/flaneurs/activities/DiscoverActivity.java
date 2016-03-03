package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.io.File;

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

    public final String APP_TAG = "flaneurs";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";

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
                onLaunchCamera();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                Intent i = new Intent(DiscoverActivity.this, ComposeActivity.class);
                i.putExtra("BitmapImage", takenPhotoUri.getPath());
                startActivity(i);

            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoFileUri(photoFileName)); // set the image file name

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the Uri for a photo stored on disk given the fileName
    public Uri getPhotoFileUri(String fileName) {
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
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

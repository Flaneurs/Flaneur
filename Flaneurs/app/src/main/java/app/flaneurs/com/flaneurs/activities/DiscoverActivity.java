package app.flaneurs.com.flaneurs.activities;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.facebook.appevents.AppEventsLogger;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.adapters.MapStreamPagerAdapter;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.fragments.StreamFragment;
import app.flaneurs.com.flaneurs.manager.ParseManager;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.utils.LocationProvider;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import butterknife.Bind;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class DiscoverActivity extends AppCompatActivity implements LocationProvider.ILocationListener {

    @Bind(R.id.vpViewPager)
    ViewPager viewPager;

    @Bind(R.id.psTabs)
    PagerSlidingTabStrip slidingTabStrip;

    private LocationProvider mLocationProvider;
    private MapFragment mMapFragment;
    private StreamFragment mStreamFragment;

    private Location mLocation;

    public final String APP_TAG = "flaneurs";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);
        ButterKnife.bind(this);
        DiscoverActivityPermissionsDispatcher.getMyLocationWithCheck(this);
        loadViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DiscoverActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        mLocationProvider = FlaneurApplication.getInstance().locationProvider;
        mLocationProvider.addListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mLocation == null) {
            updateConfigurationsWithLocation(location);
            reloadData();
        }
        mLocation = location;
    }

    private void loadViews() {
        MapFragment.MapConfiguration configuration = new MapFragment.MapConfiguration();
        configuration.setMapType(MapFragment.MapType.RecentPosts);
        mMapFragment = MapFragment.newInstance(configuration);

        StreamFragment.StreamConfiguration streamConfiguration = new StreamFragment.StreamConfiguration();
        streamConfiguration.setStreamType(StreamFragment.StreamType.AllPosts);
        mStreamFragment = StreamFragment.createInstance(streamConfiguration);
        viewPager.setAdapter(new MapStreamPagerAdapter(getSupportFragmentManager(), mMapFragment, mStreamFragment));

        slidingTabStrip.setViewPager(viewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLaunchCamera();
            }
        });
    }

    private void updateConfigurationsWithLocation(Location location) {
        MapFragment.MapConfiguration mapConfig = mMapFragment.getMapConfiguration();
        mapConfig.setCurrentLocation(location);
        mMapFragment.setMapConfiguration(mapConfig);

        StreamFragment.StreamConfiguration streamConfig = mStreamFragment.getStreamConfiguration();
        streamConfig.setLocation(location);
        mStreamFragment.setStreamConfiguration(streamConfig);
    }

    private void reloadData() {
        mMapFragment.loadMap();
        mStreamFragment.loadStream();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getPhotoFileUri(photoFileName);
                Intent i = new Intent(DiscoverActivity.this, ComposeActivity.class);

                if (mLocation != null) {
                    i.putExtra(ComposeActivity.COMPOSE_LAT_ID, mLocation.getLatitude());
                    i.putExtra(ComposeActivity.COMPOSE_LONG_ID, mLocation.getLongitude());
                }
                i.putExtra(ComposeActivity.COMPOSE_IMAGE_ID, takenPhotoUri.getPath());
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
        i.putExtra(ProfileActivity.PROFILE_TYPE, ProfileActivity.ProfileType.CURRENT_USER);
        startActivity(i);
    }

   public void onInboxViewOnClick(MenuItem mi) {
        Intent i = new Intent(this, InboxActivity.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
}

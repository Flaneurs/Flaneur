package app.flaneurs.com.flaneurs.fragments;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.utils.LocationProvider;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MapFragment extends SupportMapFragment implements LocationProvider.ILocationListener {

    public static final String TAG = MapFragment.class.getSimpleName();
    public static final String ARG_SHOULD_TRACK_LOCATION = "ARG_SHOULD_TRACK_LOCATION";
    public static final String ARG_SHOULD_LOCK_MAP = "ARG_SHOULD_LOCK_MAP";
    public static final String ARG_LAT_LNG = "ARG_LAT_LNG";
    public static final String ARG_POSTS = "ARG_POSTS";

    private GoogleMap map;
    private LocationProvider mLocationProvider;

    private Location mLocation;

    private boolean shouldTrackLocation;
    private boolean shouldLockMap;
    private ArrayList<ParseProxyObject> posts;
    private LatLng point;

    private Map<String, ParseProxyObject> markerPostMap;

    public static MapFragment newInstance(boolean shouldTrackLocation, boolean shouldLockMap, LatLng latLng, ArrayList<ParseProxyObject> parseProxyObjects) {
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOULD_TRACK_LOCATION, shouldTrackLocation);
        args.putBoolean(ARG_SHOULD_LOCK_MAP, shouldLockMap);
        args.putParcelable(ARG_LAT_LNG, latLng);
        args.putSerializable(ARG_POSTS, parseProxyObjects);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            shouldTrackLocation = arguments.getBoolean(ARG_SHOULD_TRACK_LOCATION);
            shouldLockMap = arguments.getBoolean(ARG_SHOULD_LOCK_MAP);
            point = arguments.getParcelable(ARG_LAT_LNG);
            posts = (ArrayList<ParseProxyObject>)arguments.getSerializable(ARG_POSTS);
        } else {
            shouldTrackLocation = false;
            point = null;
            posts = null;
        }

        markerPostMap = new HashMap<>();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                loadMap(googleMap);
            }
        });
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            Log.d(TAG, "Map Fragment was loaded properly.");

            int topPadding = (shouldTrackLocation || shouldLockMap) ? 0 : 50;
            int bottomPadding = (shouldTrackLocation) ? 130 : 0;
            map.setPadding(0, topPadding, 0, bottomPadding);
            if (shouldLockMap) {
                map.getUiSettings().setScrollGesturesEnabled(false);
            } else {
                map.setInfoWindowAdapter(new CustomWindowAdapter(getActivity().getLayoutInflater()));
            }

            if (point != null) {
                markLatLng(point);
            } else if (posts != null && posts.size() > 0) {
                final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                for (ParseProxyObject post : posts) {
                    double[] latLng = post.getParseGeoPointArray(Post.KEY_POST_LOCATION);
                    LatLng point = new LatLng(latLng[0], latLng[1]);
                    Marker marker = addMarkerAtLatLng(point);
                    bounds.include(point);

                    markerPostMap.put(marker.getId(), post);
                }
                LatLngBounds newBounds = bounds.build();

                if (!shouldTrackLocation) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(newBounds.getCenter(), 5.0f));
                    map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
                        }
                    });
                }
            }
            if (shouldTrackLocation) {
                MapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
            }
        } else {
            Toast.makeText(getContext(), "Error - Map was null!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Map was null.");
        }
    }

    private Marker addMarkerAtLatLng(LatLng latLng) {
        BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        return map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(defaultMarker));
    }

    public void markLatLng(LatLng latLng) {
        addMarkerAtLatLng(latLng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        map.moveCamera(cameraUpdate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (map != null) {
            map.setMyLocationEnabled(true);
            mLocationProvider = FlaneurApplication.getInstance().locationProvider;
            mLocationProvider.addListener(this);
            mLocationProvider.connect();
        }
    }

    public void onLocationChanged(Location location) {
        if (mLocation == null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate1 = CameraUpdateFactory.newLatLngZoom(latLng, 10);
            CameraUpdate cameraUpdate2 = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.moveCamera(cameraUpdate1);
            map.animateCamera(cameraUpdate2);
        }
        mLocation = location;
    }

    public Location getCurrentLocation() {
        return (shouldTrackLocation) ? mLocation : null;
    }

    class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater mInflater;

        public CustomWindowAdapter(LayoutInflater i){
            mInflater = i;
        }

        // This defines the contents within the info window based on the marker
        @Override
        public View getInfoContents(Marker marker) {
            // Get the associated post
            ParseProxyObject post = markerPostMap.get(marker.getId());

            // Getting view from the layout file
            View v = mInflater.inflate(R.layout.custom_info_window, null);

            // Populate fields
            TextView title = (TextView) v.findViewById(R.id.tv_info_window_title);
            title.setText(post.getString(Post.KEY_POST_CAPTION));

            TextView description = (TextView) v.findViewById(R.id.tv_info_window_description);
            String s = String.format("Upvotes: %d", post.getInt(Post.KEY_POST_UPVOTECOUNT));
            description.setText(s);

            // Return info window contents
            return v;
        }

        // This changes the frame of the info window; returning null uses the default frame.
        // This is just the border and arrow surrounding the contents specified above
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }
}

package app.flaneurs.com.flaneurs.fragments;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
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
    private PostMarkerGetter mPostMarkerGetter;

    private Location mLocation;

    private boolean shouldTrackLocation;
    private boolean shouldLockMap;
    private ArrayList<ParseProxyObject> posts;
    private LatLng point;

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
            posts = (ArrayList<ParseProxyObject>) arguments.getSerializable(ARG_POSTS);
        } else {
            shouldTrackLocation = false;
            point = null;
            posts = null;
        }

        mPostMarkerGetter = new PostMarkerGetter();

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
                map.getUiSettings().setAllGesturesEnabled(false);
            } else {
                map.setInfoWindowAdapter(new CustomWindowAdapter(getActivity().getLayoutInflater()));
            }

            if (point != null) {
                markLatLng(point);
            } else if (posts != null && posts.size() > 0) {
                final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                for (ParseProxyObject post : posts) {
                    LatLng markerPoint = addMarkerForPost(post, false);
                    bounds.include(markerPoint);
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

    public LatLng addMarkerForPost(ParseProxyObject post, boolean withAnimation) {
        double[] latLng = post.getParseGeoPointArray(Post.KEY_POST_LOCATION);
        LatLng point = new LatLng(latLng[0], latLng[1]);
        Marker marker = addMarkerAtLatLng(point);
        mPostMarkerGetter.putPostMarker(post, marker);

        if (withAnimation) {
            dropPinEffect(marker);
        }

        return point;
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

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        if (map != null) {
            map.setMyLocationEnabled(true);
            mLocationProvider = FlaneurApplication.getInstance().locationProvider;
            mLocationProvider.addListener(this);
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


    public void onPostPickup(String postId) {
        //TODO a thing
    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator = new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15 ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;

                // Calculate t for bounce based on elapsed time
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);

                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 8 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now
                    handler.postDelayed(this, 15);
                } else {
                    // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    class CustomWindowAdapter implements GoogleMap.InfoWindowAdapter {
        LayoutInflater mInflater;

        public CustomWindowAdapter(LayoutInflater i) {
            mInflater = i;
        }

        // This defines the contents within the info window based on the marker
        @Override
        public View getInfoContents(Marker marker) {
            // Get the associated post
            ParseProxyObject post = mPostMarkerGetter.getPostForMarkerId(marker.getId());

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

    class PostMarkerGetter {
        private Map<String, ParseProxyObject> markerPostMap;
        private Map<String, Marker> postMarkerMap;

        public PostMarkerGetter() {
            markerPostMap = new HashMap<>();
            postMarkerMap = new HashMap<>();
        }

        public void putPostMarker(ParseProxyObject post, Marker marker) {
            markerPostMap.put(marker.getId(), post);
            postMarkerMap.put(post.getObjectId(), marker);
        }

        public Marker getMarkerForPostId(String postId) {
            return postMarkerMap.get(postId);
        }

        public  ParseProxyObject getPostForMarkerId(String markerId) {
            return markerPostMap.get(markerId);
        }
    }
}

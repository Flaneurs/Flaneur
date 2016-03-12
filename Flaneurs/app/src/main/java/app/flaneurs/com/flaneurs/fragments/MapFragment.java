package app.flaneurs.com.flaneurs.fragments;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.manager.ParseManager;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.utils.LocationProvider;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MapFragment extends SupportMapFragment implements LocationProvider.ILocationListener {

    public static final String TAG = MapFragment.class.getSimpleName();
    public static String MAP_CONFIGURATION_KEY = "MAP_CONFIGURATION_KEY";

    private GoogleMap map;
    private LocationProvider mLocationProvider;

    private MapConfiguration mMapConfiguration;

    public MapConfiguration getMapConfiguration() {
        return mMapConfiguration;
    }
    public void setMapConfiguration(MapConfiguration mMapConfiguration) {
        this.mMapConfiguration = mMapConfiguration;
    }

    public static class MapConfiguration implements Serializable {
        private MapType mMapType;
        private boolean mShouldTrackLocation;
        private LatLng mPoint;
        private String mPostId;

        public Location getCurrentLocation() {
            return mCurrentLocation;
        }

        public void setCurrentLocation(Location mCurrentLocation) {
            this.mCurrentLocation = mCurrentLocation;
        }

        private Location mCurrentLocation;

        public LatLng getPoint() {
            return mPoint;
        }

        public void setPoint(LatLng mPoint) {
            this.mPoint = mPoint;
        }

        public String getPostId() {
            return mPostId;
        }

        public void setPostId(String mPostId) {
            this.mPostId = mPostId;
        }

        public boolean isTrackingLocation() {
            return mShouldTrackLocation;
        }

        public void setShouldTrackLocation(boolean shouldTrackLocation) {
            this.mShouldTrackLocation = shouldTrackLocation;
        }

        public MapType getMapType() {
            return mMapType;
        }

        public void setMapType(MapType mMapType) {
            this.mMapType = mMapType;
        }

    }

    public enum MapType {
        Location, RecentPosts, Post
    }

    public static MapFragment newInstance(MapConfiguration mapConfiguration) {
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putSerializable(MAP_CONFIGURATION_KEY, mapConfiguration);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            MapConfiguration configuration = ((MapConfiguration) arguments.getSerializable(MAP_CONFIGURATION_KEY));
            setMapConfiguration(configuration);
        }
    }

    public void loadMap() {
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                loadMap(googleMap);
            }
        });
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        final MapConfiguration configuration = getMapConfiguration();
        if (map != null) {
            Log.d(TAG, "Map Fragment was loaded properly.");
            if (configuration.getMapType() == MapType.Location) {
                markLatLng(configuration.getPoint());
                configureMapForLocationTracking();
            } else if (configuration.getMapType() == MapType.RecentPosts) {
                ParseManager.getInstance().getPostsRelativeToLocation(configuration.getCurrentLocation(), 5, new ParseManager.IOnPostsReceivedCallback() {
                    @Override
                    public void onSuccess(Post post) {

                    }

                    @Override
                    public void onSuccess(List<Post> posts) {
                        if (posts.size() == 1) {
                            Post post = posts.get(0);
                            ParseGeoPoint geoPoint = post.getParseGeoPoint(Post.KEY_POST_LOCATION);
                            LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            markLatLng(point);
                        } else {
                            for (Post post : posts) {
                                ParseGeoPoint geoPoint = post.getParseGeoPoint(Post.KEY_POST_LOCATION);
                                LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                                addMarkerAtLatLng(point);
                            }
                        }
                        configureMapForLocationTracking();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getActivity(), "Error fetching posts by location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (configuration.getMapType() == MapType.Post) {
                ParseManager.getInstance().getPostById(configuration.getPostId(), new ParseManager.IOnPostsReceivedCallback() {
                    @Override
                    public void onSuccess(Post post) {
                        ParseGeoPoint geoPoint = post.getParseGeoPoint(Post.KEY_POST_LOCATION);
                        LatLng point = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                        markLatLng(point);
                        configureMapForLocationTracking();
                    }

                    @Override
                    public void onSuccess(List<Post> posts) {

                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getActivity(), "Error fetching posts by id", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(getContext(), "Error - Map was null!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Map was null.");
        }
    }

    private void configureMapForLocationTracking() {
        if (getMapConfiguration().isTrackingLocation() == true) {
            MapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
        }
    }

    private void addMarkerAtLatLng(LatLng latLng) {
        BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        map.addMarker(new MarkerOptions()
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
        }
    }

    public void onLocationChanged(Location location) {
        if (getMapConfiguration().getCurrentLocation() == null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            map.animateCamera(cameraUpdate);
        }
        getMapConfiguration().setCurrentLocation(location);
    }
}

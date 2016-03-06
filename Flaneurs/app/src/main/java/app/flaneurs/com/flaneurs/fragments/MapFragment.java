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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.utils.LocationProvider;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MapFragment extends SupportMapFragment implements LocationProvider.ILocationListener{

    public static final String TAG = MapFragment.class.getSimpleName();
    public static final String ARG_SHOULD_TRACK_LOCATION = "ARG_SHOULD_TRACK_LOCATION";
    public static final String ARG_LAT_LNG = "ARG_LAT_LNG";
    public static final String ARG_POST = "ARG_POST";

    private GoogleMap map;
    private LocationProvider mLocationProvider;

    private Location mLocation;

    private boolean shouldTrackLocation;
    private ParseProxyObject post;
    private LatLng point;

    public static MapFragment newInstance(boolean shouldTrackLocation, LatLng latLng, ParseProxyObject parseProxyObject) {
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOULD_TRACK_LOCATION, shouldTrackLocation);
        args.putParcelable(ARG_LAT_LNG, latLng);
        args.putSerializable(ARG_POST, parseProxyObject);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            shouldTrackLocation = arguments.getBoolean(ARG_SHOULD_TRACK_LOCATION);
            point = arguments.getParcelable(ARG_LAT_LNG);
            post = (ParseProxyObject)arguments.getSerializable(ARG_POST);
        } else {
            shouldTrackLocation = false;
            post = null;
        }

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
            if (point != null) {
                markLatLng(point);
            } else if (post != null) {
                double[] latLng = post.getParseGeoPointArray(Post.KEY_POST_LOCATION);
                LatLng point = new LatLng(latLng[0], latLng[1]);
                addMarkerAtLatLng(point);
                if (!shouldTrackLocation) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(point, 17);
                    map.animateCamera(cameraUpdate);
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

    private void addMarkerAtLatLng(LatLng latLng) {
        BitmapDescriptor defaultMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(defaultMarker));
    }

    public void markLatLng(LatLng latLng) {
        addMarkerAtLatLng(latLng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        map.animateCamera(cameraUpdate);
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
        mLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        map.animateCamera(cameraUpdate);
    }

    public Location getCurrentLocation() {
        return (shouldTrackLocation) ? mLocation : null;
    }
}

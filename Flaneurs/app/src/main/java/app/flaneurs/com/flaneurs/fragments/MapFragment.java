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
import com.google.android.gms.maps.model.LatLng;

import app.flaneurs.com.flaneurs.utils.LocationProvider;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MapFragment extends SupportMapFragment implements LocationProvider.ILocationListener{

    public static final String TAG = MapFragment.class.getSimpleName();

    private GoogleMap map;
    private LocationProvider mLocationProvider;

    private boolean shouldTrackLocation;

    public static MapFragment newInstance(boolean shouldTrackLocation) {
        MapFragment mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putBoolean("shouldTrackLocation", shouldTrackLocation);
        mapFragment.setArguments(args);
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shouldTrackLocation = (getArguments() != null) ? getArguments().getBoolean("shouldTrackLocation") : false;
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
            if (shouldTrackLocation) {
                MapFragmentPermissionsDispatcher.getMyLocationWithCheck(this);
            }
        } else {
            Toast.makeText(getContext(), "Error - Map was null!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Map was null.");
        }
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
            mLocationProvider = new LocationProvider(getActivity(), this);
            mLocationProvider.connect();
        }
    }

    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        map.animateCamera(cameraUpdate);
    }
}

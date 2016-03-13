package app.flaneurs.com.flaneurs.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.ocpsoft.pretty.time.PrettyTime;
import com.parse.ParseGeoPoint;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by kamranpirwani on 3/5/16.
 */
public class Utils {
    public static String getPrettyAddress(Context mContext, double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(mContext, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null) {
            Address bestMatch = (addresses.isEmpty() ? null : addresses.get(0));
            if (bestMatch != null) {
                String address = bestMatch.getAddressLine(0);
                return address;
            }
        }
        return "This post is too old to have an address";
    }

    public static String getPrettyDistance(Location current, ParseGeoPoint destinationGeoPoint) {
        if (current == null || destinationGeoPoint == null) {
            return "Unknown";
        }

        Location destination = new Location("");
        destination.setLatitude(destinationGeoPoint.getLatitude());
        destination.setLongitude(destinationGeoPoint.getLongitude());
        float distanceInMeters = current.distanceTo(destination);
        String pretty = distanceInMeters + "m away";
        return pretty;
    }

    public static String getPrettyTime(Date date) {
        PrettyTime pt = new PrettyTime();
       return pt.format(date);
    }

}

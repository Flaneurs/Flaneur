package app.flaneurs.com.flaneurs.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.NotificationCompat;

import com.ocpsoft.pretty.time.PrettyTime;
import com.parse.ParseGeoPoint;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.activities.InboxActivity;

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
        return "Unknown";
    }

    public static String getPrettyDistance(Location current, ParseGeoPoint destinationGeoPoint) {
        if (current == null || destinationGeoPoint == null) {
            return "Unknown";
        }

        Location destination = new Location("");
        destination.setLatitude(destinationGeoPoint.getLatitude());
        destination.setLongitude(destinationGeoPoint.getLongitude());
        int distanceInMeters = Math.round(current.distanceTo(destination));
        double miles = Utils.convertMetersToMiles(distanceInMeters);
        String pretty = String.format("%f mi away", miles);
        return pretty;
    }

    public static String getPrettyTime(Date date) {
        PrettyTime pt = new PrettyTime();
        return pt.format(date);
    }

    public static void fireLocalNotification(Context context, String address) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("WalkAbout")
                        .setContentText("New pickup at " + address)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);


        Intent resultIntent = new Intent(context, InboxActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public static double convertMetersToMiles(int meters) {
        double miles = 0.000621371 * meters;
        return miles;
    }
}

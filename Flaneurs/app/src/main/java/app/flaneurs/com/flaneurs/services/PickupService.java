package app.flaneurs.com.flaneurs.services;

import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.flaneurs.com.flaneurs.FlaneurApplication;
import app.flaneurs.com.flaneurs.models.InboxItem;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.LocationProvider;

/**
 * Created by mprice on 3/12/16.
 */
public class PickupService implements LocationProvider.ILocationListener {

    public static String PICKUP_ADDRESS = "PICKUP_ADDRESS";
    public static String PICKUP_EVENT = "PICKUP_EVENT";

    private Location mCurrentLocation;
    private List<Post> cachedPosts;
    private List<Post> currentSessionInboxPosts;
    private List<InboxItem> currentSessionInbox;

    private final static int PICKUP_RADIUS_IN_METERS = 5;
    private final static int QUERY_RADIUS_IN_METERS = 10;

    private final static double METER_PER_MILE = 1609.344;

    public PickupService() {
        FlaneurApplication.getInstance().locationProvider.addListener(this);
        mCurrentLocation = FlaneurApplication.getInstance().locationProvider.getCurrentLocation();

        currentSessionInboxPosts = new ArrayList<>();
        cachedPosts = new ArrayList<>();
    }

    @Override
    public void onLocationChanged(Location location) {
        Location oldLocation = mCurrentLocation;
        mCurrentLocation = location;

        if (oldLocation == null || oldLocation.distanceTo(mCurrentLocation) > QUERY_RADIUS_IN_METERS) {
            Log.e("PickupService", "Querying for posts");
            queryForPosts();
        } else if (mCurrentLocation != null) {
            Log.e("PickupService", "Attempting Pickup");
            attemptPickup();
        }
    }

    private void attemptPickup() {
        ParseGeoPoint currentLocation = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        for (Post post : cachedPosts) {

            double distanceInMiles = post.getLocation().distanceInMilesTo(currentLocation);
            if (distanceInMiles * METER_PER_MILE < PICKUP_RADIUS_IN_METERS) {
                if (currentSessionInboxPosts.contains(post)) {
                    Log.e("PickupService", "Already pickedup Post at: " + post.getAddress());
                    continue;
                }
                addPostToInbox(post);
            }
        }
    }

    private void queryForPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        ParseGeoPoint current = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        query.whereNear(Post.KEY_POST_LOCATION, current);
        query.whereNotEqualTo(Post.KEY_POST_AUTHOR, User.currentUser());

        currentSessionInboxPosts = new ArrayList<>();

        ParseQuery<InboxItem> inboxQuery = ParseQuery.getQuery("InboxItem");
        inboxQuery.whereEqualTo("KEY_INBOX_USER", User.currentUser());

        query.whereDoesNotMatchKeyInQuery("objectId", "KEY_INBOX_ID", inboxQuery);

        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e != null) {
                    return;
                }

                Log.e("PickupService", "Queried and got posts:  " + objects.size());
                if (objects.isEmpty()) {
                    return;
                }

                cachedPosts = objects;
                attemptPickup();
            }
        });
    }

    private void addPostToInbox(Post post) {
        InboxItem inboxItem = new InboxItem();

        inboxItem.setPost(post);
        inboxItem.setUser(User.currentUser());
        inboxItem.setPickUpTime(new Date());
        inboxItem.setNew(true);
        inboxItem.setHidden(false);
        inboxItem.saveInBackground();

        post.incrementViewCount();
        post.saveEventually();
        post.getAuthor().fetchIfNeededInBackground();
        currentSessionInboxPosts.add(post);

        if (currentSessionInbox != null) {
            currentSessionInbox.add(inboxItem);
        }
        sendNotification(post);
    }

    private void sendNotification(Post post) {
        Intent intent = new Intent(PICKUP_EVENT);
        intent.putExtra(PICKUP_ADDRESS, post.getAddress());
        LocalBroadcastManager.getInstance(FlaneurApplication.getInstance().getApplicationContext()).sendBroadcast(intent);
    }

    public List<InboxItem> getInbox() {
        return currentSessionInbox;
    }

    public void onColdLaunch() {
        ParseQuery<InboxItem> query = ParseQuery.getQuery("InboxItem");
        query.whereEqualTo(InboxItem.KEY_INBOX_USER, User.currentUser());
        query.whereEqualTo(InboxItem.KEY_INBOX_HIDDEN, false);
        query.include(InboxItem.KEY_INBOX_POST);
        query.include(InboxItem.KEY_INBOX_POST + "." + Post.KEY_POST_AUTHOR);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.orderByDescending(InboxItem.KEY_INBOX_DATE + "," + InboxItem.KEY_INBOX_NEW);
        query.findInBackground(new FindCallback<InboxItem>() {
            @Override
            public void done(List<InboxItem> objects, ParseException e) {
                Log.e("PickupService", "Updating cached inbox");
                currentSessionInbox = objects;
            }
        });
    }

    public void onHide(InboxItem item) {
        currentSessionInbox.remove(item);
    }
}

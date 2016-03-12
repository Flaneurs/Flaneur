package app.flaneurs.com.flaneurs.manager;

import android.location.Location;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import app.flaneurs.com.flaneurs.models.Post;

/**
 * Created by kamranpirwani on 3/9/16.
 */
public class ParseManager {

    private static ParseManager instance = null;
    public static final int ALL_POSTS = -1;

    public static ParseManager getInstance() {
        if (instance == null) {
            instance = new ParseManager();
        }
        return instance;
    }

    public void getPostsRelativeToLocation(Location currentLocation, final int numberOfPosts, final IOnPostsReceivedCallback postsReceivedCallback) {
        ParseQuery query = getParseQueryForRecentPostsByLocation(currentLocation);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        if (query.hasCachedResult() == true) {
            query.findInBackground(new FindCallback() {
                @Override
                public void done(List objects, ParseException e) {
                    if (e == null) {
                        postsReceivedCallback.onSuccess(filteredList(objects, numberOfPosts));
                    } else {
                        postsReceivedCallback.onFailure(e.getLocalizedMessage());
                    }
                }

                @Override
                public void done(Object o, Throwable throwable) {
                    if (throwable == null) {
                        List<Post> posts = new ArrayList<Post>();
                        posts.add((Post) o);
                        postsReceivedCallback.onSuccess(posts);
                    } else {
                        postsReceivedCallback.onFailure(throwable.getMessage());
                    }
                }
            });
        } else {
            fetchPostsRelativeToLocation(currentLocation, new IOnPostsFetchCallback() {
                @Override
                public void onSuccess(List<Post> posts) {
                    postsReceivedCallback.onSuccess(posts.subList(0, numberOfPosts));
                }

                @Override
                public void onFailure(String error) {
                    postsReceivedCallback.onFailure(error);
                }
            });
        }
    }

    private List<Post> filteredList(List<Post> list, int endIndex) {
        if (endIndex == ALL_POSTS) {
            return list;
        } else {
            boolean validNumberOfPosts = list.size() > endIndex;
            List<Post> subList;
            if (validNumberOfPosts) {
                subList = list.subList(0, endIndex);
            } else {
                subList = list.subList(0, list.size() - 1);
            }
            return subList;
        }
    }

    private void fetchPostsRelativeToLocation(Location currentLocation, final IOnPostsFetchCallback fetchedCallback) {
        ParseQuery query = getParseQueryForRecentPostsByLocation(currentLocation);
        query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    fetchedCallback.onSuccess(objects);
                } else {
                    Log.e("ParseManager", "Error fetching parse posts by location: " + e.getMessage());
                    fetchedCallback.onFailure("Error fetching parse posts by location: " + e.getMessage());
                }
            }
        });
    }

    public void getPostById(String objectIdentifier, final IOnPostsReceivedCallback callback) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // First try to find from the cache and only then go to network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK); // or CACHE_ONLY
        // Execute the query to find the object with ID
        query.getInBackground(objectIdentifier, new GetCallback<Post>() {
            public void done(Post item, ParseException e) {
                if (e == null) {
                    callback.onSuccess(item);
                } else {
                    callback.onFailure(e.getMessage());
                }
            }
        });
    }

    private ParseQuery<Post> getParseQueryForRecentPostsByLocation(Location currentLocation) {
        ParseGeoPoint currentPoint = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        ParseQuery<Post> query = ParseQuery.getQuery("Post");
        query.whereNear(Post.KEY_POST_LOCATION, currentPoint);
        return query;
    }

    public interface IOnPostsReceivedCallback {
        void onSuccess(Post post);

        void onSuccess(List<Post> posts);

        void onFailure(String error);
    }

    private interface IOnPostsFetchCallback {
        void onSuccess(List<Post> posts);

        void onFailure(String error);
    }
}

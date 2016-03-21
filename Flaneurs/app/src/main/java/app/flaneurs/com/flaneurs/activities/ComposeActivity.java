package app.flaneurs.com.flaneurs.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.fragments.MapFragment;
import app.flaneurs.com.flaneurs.models.Post;
import app.flaneurs.com.flaneurs.models.User;
import app.flaneurs.com.flaneurs.utils.ParseProxyObject;
import app.flaneurs.com.flaneurs.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = ComposeActivity.class.getSimpleName();
    public static final int REQUEST_PLACE_PICKER = 37;

    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Bind(R.id.etCaption)
    EditText etCaption;

    @Bind(R.id.btnLocation)
    Button btnLocation;

    @Bind(R.id.postButton)
    Button postButton;

    Bitmap mPicture;

    private double mLat;
    private double mLong;

    private MapFragment mMapFragment;

    public final static String COMPOSE_LAT_ID = "COMPOSE_LAT_ID";
    public final static String COMPOSE_LONG_ID = "COMPOSE_LONG_ID";
    public final static String COMPOSE_IMAGE_ID = "COMPOSE_IMAGE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra(COMPOSE_IMAGE_ID);
        mLat = intent.getDoubleExtra(COMPOSE_LAT_ID, 0);
        mLong = intent.getDoubleExtra(COMPOSE_LONG_ID, 0);

        mPicture = BitmapFactory.decodeFile(imageUri);
        ivPicturePreview.setImageBitmap(mPicture);

        btnLocation.setText(Utils.getPrettyAddress(this, mLat, mLong));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int sidePadding = (int) (310.0/768 * metrics.widthPixels);

        postButton.setPadding(sidePadding, 0, sidePadding, 0);

        mMapFragment = MapFragment.newInstance(false, false, new LatLng(mLat, mLong), null);
        getSupportFragmentManager().beginTransaction().replace(R.id.flMap, mMapFragment).commit();
    }

    public void onLocationClicked(View v) {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "Unable to launch place picker.");
            Toast.makeText(this, "Error - Unable to launch place picker!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onPostButtonClicked(View v) {
        final Post newPost = new Post();

        User currentUser = User.currentUser();
        newPost.setAuthor(currentUser);

        ParseGeoPoint latLong = new ParseGeoPoint(mLat, mLong);
        newPost.setLocation(latLong);

        String caption = etCaption.getText().toString();
        newPost.setCaption(caption);
        newPost.setCreatedTime(new Date());
        newPost.setPostType("image");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mPicture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        // get byte array here
        byte[] bytearray = stream.toByteArray();

        ParseFile file = new ParseFile("picture.jpg", bytearray);
        newPost.setImage(file);

        String address = btnLocation.getText().toString();
        newPost.setAddress(address);

        newPost.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
            }
        });

        currentUser.incrementDrops();
        currentUser.saveEventually();

        Intent data = new Intent();
        data.putExtra(Post.KEY_POST, new ParseProxyObject(newPost));

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and latLng.
            final Place place = PlacePicker.getPlace(data, this);
            final CharSequence name = place.getName();
            final LatLng location = place.getLatLng();
            mLat = location.latitude;
            mLong = location.longitude;

            btnLocation.setText(name);
            mMapFragment.markLatLng(location);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

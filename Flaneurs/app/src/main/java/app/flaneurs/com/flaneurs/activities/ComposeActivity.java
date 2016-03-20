package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Bind(R.id.etCaption)
    EditText etCaption;

    @Bind(R.id.tvLocation)
    TextView tvLocation;

    @Bind(R.id.postButton)
    Button postButton;

    Bitmap mPicture;

    private double mLat;
    private double mLong;

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

        tvLocation.setText(Utils.getPrettyAddress(this, mLat, mLong));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int sidePadding = (int) (310.0/768 * metrics.widthPixels);

        postButton.setPadding(sidePadding, 0, sidePadding, 0);

        getSupportActionBar().hide();
        getSupportFragmentManager().beginTransaction().replace(R.id.flMap, MapFragment.newInstance(false, false, new LatLng(mLat, mLong), null)).commit();
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

        String address = tvLocation.getText().toString();
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
}

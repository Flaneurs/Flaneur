package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Random;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.models.Post;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeActivity extends AppCompatActivity {

    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Bind(R.id.etCaption)
    EditText etCaption;

    Bitmap mPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("BitmapImage");
        mPicture = BitmapFactory.decodeFile(imageUri);
        ivPicturePreview.setImageBitmap(mPicture);
    }

    public void onPostButtonClicked(View v) {

        final Post newPost = new Post();

        // TODO: get lat and long from map
        Random r = new Random();
        double randomLat = 37 + (1) * r.nextDouble();
        double randomLong = 122 + (1) * r.nextDouble();

        ParseGeoPoint latLong = new ParseGeoPoint(randomLat, randomLong);
        String caption = etCaption.getText().toString();

        newPost.setAuthor(ParseUser.getCurrentUser());
        newPost.setLocation(latLong);
        newPost.setCaption(caption);
        newPost.setCreatedTime(new Date());
        newPost.setPostType("image");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mPicture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        // get byte array here
        byte[] bytearray = stream.toByteArray();

        ParseFile file = new ParseFile("picture.jpg", bytearray);
        newPost.setImage(file);

        newPost.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                Log.v("DEBUG", "Saved!");
            }
        });

        finish();
    }
}

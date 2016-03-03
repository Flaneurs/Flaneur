package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import app.flaneurs.com.flaneurs.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeActivity extends AppCompatActivity {

    @Bind(R.id.ivPicturePreview)
    ImageView ivPicturePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String imageUri = intent.getStringExtra("BitmapImage");
        Bitmap bitmap = BitmapFactory.decodeFile(imageUri);
        ivPicturePreview.setImageBitmap(bitmap);
    }
}

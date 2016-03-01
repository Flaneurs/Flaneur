package app.flaneurs.com.flaneurs.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import app.flaneurs.com.flaneurs.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginButtonClicked(View v) {
        Intent i = new Intent(this, DiscoverActivity.class);
        startActivity(i);
    }

}

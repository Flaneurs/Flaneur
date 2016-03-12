package app.flaneurs.com.flaneurs.activities;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import app.flaneurs.com.flaneurs.R;
import app.flaneurs.com.flaneurs.fragments.StreamFragment;
import app.flaneurs.com.flaneurs.models.User;

public class InboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        if (savedInstanceState == null) {
            StreamFragment fragment = createStreamFragment();
            android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, fragment).commit();
        }
    }

    private StreamFragment createStreamFragment() {
        StreamFragment.StreamConfiguration configuration = new StreamFragment.StreamConfiguration();
        configuration.setStreamType(StreamFragment.StreamType.Inbox);
        configuration.setUser(User.getCurrentUser());
        StreamFragment fragment = StreamFragment.createInstance(configuration);
        fragment.loadStream();
        return fragment;
    }
}

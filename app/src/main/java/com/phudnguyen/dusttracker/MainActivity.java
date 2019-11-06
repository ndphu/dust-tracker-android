package com.phudnguyen.dusttracker;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.phudnguyen.dusttracker.fragment.GroupDetailsFragment;
import com.phudnguyen.dusttracker.fragment.LoginFragment;
import com.phudnguyen.dusttracker.fragment.JoinGroupFragment;
import com.phudnguyen.dusttracker.http.HttpHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

    }

    @Override
    protected void onResume() {
        super.onResume();
        String savedToken = getSharedPreferences("appPrefs", MODE_PRIVATE)
                .getString("JWT", null);
        if (savedToken == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_placeholder, new LoginFragment(), LoginFragment.class.getSimpleName())
                    .commit();
        } else {
            HttpHelper.JWT.set(savedToken);
            String groupId = getSharedPreferences("appPrefs", MODE_PRIVATE).getString("currentGroupId", null);
            if (groupId != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, GroupDetailsFragment.newInstance(groupId), GroupDetailsFragment.class.getSimpleName())
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_placeholder, new JoinGroupFragment(), JoinGroupFragment.class.getSimpleName())
                        .commit();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}

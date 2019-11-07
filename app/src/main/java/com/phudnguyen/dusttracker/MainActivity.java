package com.phudnguyen.dusttracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.phudnguyen.dusttracker.fragment.GroupDetailsFragment;
import com.phudnguyen.dusttracker.fragment.JoinGroupFragment;
import com.phudnguyen.dusttracker.fragment.LoginFragment;
import com.phudnguyen.dusttracker.http.HttpHelper;
import com.phudnguyen.dusttracker.model.LoginResponse;
import com.phudnguyen.dusttracker.service.LocationUpdateService;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentInteractionListener, GroupDetailsFragment.GroupDetailsFragmentListener {

    public static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void startLocationUpdateService() {
        String groupId = getSharedPreferences("appPrefs", MODE_PRIVATE).getString("currentGroupId", null);
        if (groupId == null) {
            return;
        }
        Intent intent = new Intent(this, LocationUpdateService.class);
        startService(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkLocationPermission()) {
            startLocationUpdateService();
        }
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_logout:
                doLeaveGroup();
                doLogout();
                return true;
            case R.id.action_leave_group:
                doLeaveGroup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doLeaveGroup() {
        getSharedPreferences("appPrefs", Context.MODE_PRIVATE).edit().remove("currentGroupId").apply();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, new JoinGroupFragment(), JoinGroupFragment.class.getName())
                .commit();
        stopService(new Intent(this, LocationUpdateService.class));
    }

    private void doLogout() {
        getSharedPreferences("appPrefs", Context.MODE_PRIVATE).edit().remove("JWT").apply();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, new LoginFragment(), LoginFragment.class.getName())
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        startLocationUpdateService();
                    }
                } else {
                    finish();

                }
                return;
            }

        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("LocationInfo Request")
                        .setMessage("Grant permission to access location")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onLoginSuccess(LoginResponse response) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, new JoinGroupFragment(), JoinGroupFragment.class.getName())
                .commit();
    }

    @Override
    public void onGroupLoadedSuccessfully() {
        startLocationUpdateService();
    }
}

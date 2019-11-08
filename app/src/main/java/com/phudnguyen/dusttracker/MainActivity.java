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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.phudnguyen.dusttracker.fragment.CreateGroupFragment;
import com.phudnguyen.dusttracker.fragment.GroupDetailsFragment;
import com.phudnguyen.dusttracker.fragment.JoinGroupFragment;
import com.phudnguyen.dusttracker.fragment.LoginFragment;
import com.phudnguyen.dusttracker.http.HttpHelper;
import com.phudnguyen.dusttracker.model.Group;
import com.phudnguyen.dusttracker.model.LoginResponse;
import com.phudnguyen.dusttracker.service.LocationUpdateService;
import com.phudnguyen.dusttracker.utils.AppPrefs;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentInteractionListener, GroupDetailsFragment.GroupDetailsFragmentListener,
        CreateGroupFragment.CreateGroupFragmentListener, JoinGroupFragment.JoinGroupFragmentListener {

    public static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void startLocationUpdateService() {
        String groupId = AppPrefs.getGroupId(this);
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
        String savedToken = AppPrefs.getJWT(this);
        if (savedToken == null) {
            getFragmentTransaction()
                    .replace(R.id.fragment_placeholder, new LoginFragment(), LoginFragment.class.getSimpleName())
                    .commit();
        } else {
            HttpHelper.JWT.set(savedToken);
            String groupId = AppPrefs.getGroupId(this);
            if (groupId != null) {
                getFragmentTransaction()
                        .replace(R.id.fragment_placeholder, GroupDetailsFragment.newInstance(groupId), GroupDetailsFragment.class.getSimpleName())
                        .commit();
            } else {
                getFragmentTransaction()
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
        AppPrefs.setGroupId(this, null);
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
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
        getFragmentTransaction()
                .replace(R.id.fragment_placeholder, new JoinGroupFragment(), JoinGroupFragment.class.getName())
                .commit();
    }

    @Override
    public void onGroupLoadedSuccessfully() {
        startLocationUpdateService();
    }

    @Override
    public void onCreateGroupSuccess(Group group) {
        showGroupDetails(group);
    }

    @Override
    public void onCreateGroupClick() {
        getFragmentTransaction()
                .replace(R.id.fragment_placeholder, new CreateGroupFragment(), CreateGroupFragment.class.getName())
                .addToBackStack(CreateGroupFragment.class.getName())
                .commit();
    }

    @Override
    public void onJoinGroupSuccess(Group group) {
        showGroupDetails(group);
    }

    private void showGroupDetails(Group group) {
        getSupportFragmentManager()
                .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getFragmentTransaction()
                .replace(R.id.fragment_placeholder, GroupDetailsFragment.newInstance(group.getId()), GroupDetailsFragment.class.getName())
                .commit();
    }

    @NotNull
    private FragmentTransaction getFragmentTransaction() {
        return getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }
}

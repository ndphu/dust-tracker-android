package com.phudnguyen.dusttracker.fragment;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.phudnguyen.dusttracker.R;
import com.phudnguyen.dusttracker.utils.DoubleArrayEvaluator;
import com.phudnguyen.dusttracker.utils.GsonUtils;
import com.phudnguyen.dusttracker.http.HttpHelper;
import com.phudnguyen.dusttracker.model.Group;
import com.phudnguyen.dusttracker.model.GroupDetailsResponse;
import com.phudnguyen.dusttracker.model.LocationInfo;
import com.phudnguyen.dusttracker.service.LocationUpdateService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class GroupDetailsFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_GROUP_ID = "group_id";

    private String groupId;
    private LoadGroupTask loadGroupTask;
    private ActionBar mActionBar;
    private View progressBar;
    private GroupDetailsFragmentListener listener;
    private GoogleMap mMap;
    private Map<String, Marker> markerMaps = new ConcurrentHashMap<>();
    private Map<String, LocationInfo> locationMap = new ConcurrentHashMap<>();
    private boolean initializeMoved = false;

    IntentFilter filter = new IntentFilter(LocationUpdateService.BROADCAST_RECEIVER_ACTION);
    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("locationUpdateReceiver", "onReceive");
            LocationInfo locInfo = GsonUtils.GSON.fromJson(intent.getExtras().getString("LOCATION_DATA"), LocationInfo.class);
            updateLocation(locInfo);
        }
    };

    private void updateLocation(LocationInfo locInfo) {
        progressBar.setVisibility(View.GONE);
        LocationInfo prevLoc = locationMap.get(locInfo.getUserId());
        locationMap.put(locInfo.getUserId(), locInfo);

        LatLng latLng = new LatLng(locInfo.getLatitude(), locInfo.getLongitude());
        Marker marker = markerMaps.get(locInfo.getUserId());
        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(locInfo.getUsername()));
            marker.showInfoWindow();
            markerMaps.put(locInfo.getUserId(), marker);
        } else {
            // animate
            double[] startValues = new double[]{marker.getPosition().latitude, marker.getPosition().longitude};
            double[] endValues = new double[]{locInfo.getLatitude(), locInfo.getLongitude()};
            ValueAnimator latLngAnimator = ValueAnimator.ofObject(new DoubleArrayEvaluator(), startValues, endValues);
            latLngAnimator.setDuration(500);
            latLngAnimator.setInterpolator(new DecelerateInterpolator());
            final Marker finalMarker = marker;
            latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    double[] animatedValue = (double[]) animation.getAnimatedValue();
                    finalMarker.setPosition(new LatLng(animatedValue[0], animatedValue[1]));
                }
            });
            latLngAnimator.start();
//            marker.setPosition(latLng);
        }

        if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng) && !initializeMoved) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f));
            initializeMoved = true;
        }
    }

    public GroupDetailsFragment() {

    }

    public static GroupDetailsFragment newInstance(String groupId) {
        GroupDetailsFragment fragment = new GroupDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(locationUpdateReceiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().registerReceiver(locationUpdateReceiver, filter);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_details, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (GroupDetailsFragmentListener) context;
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

    }

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(locationUpdateReceiver);
        super.onDetach();
        this.listener = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        progressBar = view.findViewById(R.id.group_loading_progress);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loadGroup();
    }

    private void loadGroup() {

        if (this.loadGroupTask != null) return;
        this.loadGroupTask = new LoadGroupTask(this.groupId);
        this.loadGroupTask.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
    }

    public class LoadGroupTask extends AsyncTask<Void, Void, GroupDetailsResponse> {

        private final String groupId;

        public LoadGroupTask(String groupId) {
            this.groupId = groupId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected GroupDetailsResponse doInBackground(Void... voids) {
            try {
                return HttpHelper.get(getString(R.string.API_BASE_URL) +
                        getString(R.string.API_RETRIEVE_GROUP) + this.groupId, GroupDetailsResponse.class);
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(GroupDetailsResponse groupDetailsResponse) {
            super.onPostExecute(groupDetailsResponse);

            if (groupDetailsResponse == null) {
                return;
            }
            if (groupDetailsResponse.isSuccess()) {
                showGroupDetails(groupDetailsResponse.getGroup());
                if (GroupDetailsFragment.this.listener != null) {
                    GroupDetailsFragment.this.listener.onGroupLoadedSuccessfully();
                }
            }
        }
    }

    public static interface GroupDetailsFragmentListener {
        void onGroupLoadedSuccessfully();
    }

    private void showGroupDetails(Group group) {
        if (mActionBar != null) {
            mActionBar.setTitle(group.getName());
        }
    }
}

package com.phudnguyen.dusttracker.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.phudnguyen.dusttracker.MainActivity;
import com.phudnguyen.dusttracker.R;
import com.phudnguyen.dusttracker.http.GsonUtils;
import com.phudnguyen.dusttracker.model.LocationInfo;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;

public class LocationUpdateService extends Service implements MqttCallbackExtended {

    public static final String TAG = LocationUpdateService.class.getSimpleName();

    public static final String CHANNEL_DEFAULT_IMPORTANCE = "Default";
    public static final int ONGOING_NOTIFICATION_ID = 199991111;
    public static final String NOTIFICATION_CHANNEL_ID = LocationUpdateService.class.getName();
    public static final String LOCATION_UPDATE = "location_update";
    public static final String BROADCAST_RECEIVER_ACTION = "com.phudnguyen.dusttracker.LOCATAION_UPDATE";
    private MqttAndroidClient mqttClient;
    private MqttConnectOptions mqttConnOpts;

    private LocationManager locationManager;
    private String locationProvider = LocationManager.NETWORK_PROVIDER;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    public LocationUpdateService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMQTTClientOpts();
        final SharedPreferences appPrefs = getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
        final String groupId = appPrefs.getString("currentGroupId", null);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, "Location Provider: " + location.getProvider());
                    LocationInfo loc = new LocationInfo();
                    loc.setUserId(appPrefs.getString("userId", null));
                    loc.setUsername(appPrefs.getString("username", null));
                    loc.setGroupId(groupId);
                    loc.setLatitude(location.getLatitude());
                    loc.setLongitude(location.getLongitude());
                    loc.setTimestamp(new Date());

                    String payload = GsonUtils.GSON.toJson(loc);
                    Log.i(TAG, "Sending location data: " + payload);

                    try {
                        mqttClient.publish("/metal_head/" + groupId + "/gps", payload.getBytes(Charset.forName("UTF-8")), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public void onDestroy() {

        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        super.onDestroy();
    }

    private void initMQTTClientOpts() {
        mqttConnOpts = new MqttConnectOptions();
        mqttConnOpts.setAutomaticReconnect(true);
        mqttConnOpts.setCleanSession(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification();
        startMQTT();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(500);
        locationRequest.setInterval(5000);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        return super.onStartCommand(intent, flags, startId);
    }

    private void startMQTT() {
        try {
            if (mqttClient == null) {
                initMQTTConnection();
            } else if (!mqttClient.isConnected()) {
                mqttClient.connect(mqttConnOpts);
            }
        } catch (MqttException e) {
            Toast.makeText(this, "Fail to connect to MQTT", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void initMQTTConnection() throws MqttException {
        mqttClient = new MqttAndroidClient(getApplicationContext(), getString(R.string.MESSAGE_BROKER), UUID.randomUUID().toString());
        mqttClient.setCallback(this);
        mqttClient.connect(mqttConnOpts);
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_DEFAULT_IMPORTANCE, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle(getText(R.string.app_name))
                .setContentText("LocationInfo update service is running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setTicker("ticker text");

        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void connectionLost(Throwable cause) {
        Toast.makeText(this, "MQTT Connection Lost", Toast.LENGTH_LONG).show();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload(), Charset.forName("UTF-8"));
        Log.i(TAG, "MQTT Message received:" + payload);
        Intent i = new Intent(BROADCAST_RECEIVER_ACTION);
        i.putExtra("LOCATION_DATA", payload);
        sendBroadcast(i);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Toast.makeText(this, "MQTT Connection Success", Toast.LENGTH_LONG).show();
        String groupId = getSharedPreferences("appPrefs", MODE_PRIVATE).getString("currentGroupId", null);
        if (groupId == null)
            return;
        try {
            String topic = "/metal_head/" + groupId + "/update";
            Log.i(TAG, "Subscribing to topic: " + topic);
            mqttClient.subscribe(topic, 0);
            Log.i(TAG, "Subscribed to topic: " + topic);
            Toast.makeText(this, "topic: " + topic, Toast.LENGTH_LONG).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

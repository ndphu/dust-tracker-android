package com.phudnguyen.dusttracker;

import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MqttCallbackExtended {

    public static final String TAG = MapsActivity.class.getName();
    private GoogleMap mMap;

    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://swdcore-01.ddns.net:1883";
    private MqttConnectOptions mqttConnectOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, UUID.randomUUID().toString());
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        mqttAndroidClient.setCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mqttAndroidClient.isConnected()) {
            connectToMQTT();
        }

    }

    private void connectToMQTT() {
        new MqttConnectTask().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng ch = new LatLng(10.8015298,  106.6521173);
        mMap.addMarker(new MarkerOptions().position(ch).title("CH Office"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ch, 20f));
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
//        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
//        disconnectedBufferOptions.setBufferEnabled(true);
//        disconnectedBufferOptions.setBufferSize(100);
//        disconnectedBufferOptions.setPersistBuffer(false);
//        disconnectedBufferOptions.setDeleteOldestMessages(false);
//        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
        Log.i(TAG, "connected to " + serverURI);

    }

    private class MqttConnectTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "on Connected success");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.i(TAG, "connect failed");
                        exception.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


        }
    }
}

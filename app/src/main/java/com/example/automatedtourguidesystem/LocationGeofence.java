package com.example.automatedtourguidesystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LocationGeofence extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GeofenceTransitionsIS";
    private GoogleMap mMap;
    private String lontitude, lang;
    private String state, address, audio_url;
    private final int REQUEST_LOCATION = 11;
    private GoogleApiClient mClient;
    private Geofencing mGeofencing;
    private FusedLocationProviderClient fusedLocationClient;

    private LatLng sydney;
    private ImageView audio_play;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    protected LocationManager locationManager;
    private Location Location_cur;
    private MarkerOptions mMarkerOptions;
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    private TextView address_textview, state_textview, distance_textview, geofencing_status;
    private boolean geo_status = true;
    private   MediaPlayer mediaPlayer;
    private String start_time, end_time;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ValueEventListener a;


    public static LocationGeofence locationGeofence;
    public void locationCallback(String status) {
        if(mediaPlayer!=null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }else {
                mediaPlayer.start();
            }
        }

        setHistory();
    }


    protected void setHistory() {
        History history=new History();
        history.setStart_time(start_time);
        history.setEnd_time(java.text.DateFormat.getDateTimeInstance().format(new Date()));
        history.setLocation(state);

        if(user!=null) {
            ref.child(user.getUid()).child("History").push().setValue(history, new DatabaseReference.CompletionListener() {
                public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                    Toast.makeText(getApplicationContext(), "Reached", Toast.LENGTH_SHORT).show();

                    if (geo_status) {
                        geo_status = false;
                        audio_play.setImageResource(R.drawable.stop);
                        geofencing_status.setText(R.string.stop_geofencing);
                        if (mediaPlayer != null) {
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                        }
                    } else {
                        geo_status = true;
                        mGeofencing.unRegisterAllGeofences();
                        geofencing_status.setText(R.string.start_geofencing);
                        audio_play.setImageResource(R.drawable.play);
                    }

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        locationGeofence = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_geofence);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        if (savedInstanceState == null) {
            Intent extras = this.getIntent();
            if (extras == null) {
                state = null;
                address = null;
                lontitude = "0";
                lang = "0";
                audio_url = null;
            } else {
                state = extras.getStringExtra("state_name");
                address = extras.getStringExtra("state_address");
                lontitude = extras.getStringExtra("long");
                lang = extras.getStringExtra("lat");
                audio_url = extras.getStringExtra("audio_url");
            }
        } else {
            state = (String) savedInstanceState.getSerializable("state_name");
            address = (String) savedInstanceState.getSerializable("state_address");
            lontitude = (String) savedInstanceState.getSerializable("state_name");
            lang = (String) savedInstanceState.getSerializable("state_name");
            audio_url = (String) savedInstanceState.getSerializable("audio_url");
        }

        address_textview = findViewById(R.id.address);
        state_textview = findViewById(R.id.state);
        distance_textview = findViewById(R.id.distance);
        audio_play = findViewById(R.id.audio_play);
        geofencing_status = findViewById(R.id.geofencing_status);

        address_textview.setText(address);
        state_textview.setText(state);
//        distance_textview.setText(distance);


        if (lang != null && lontitude != null) {
            mDestination = new LatLng(Double.parseDouble(lang), Double.parseDouble(lontitude));
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Write Function To enable gps
            OnGPS();
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,

                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location_cur = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (Location_cur == null) {
                Location_cur = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (Location_cur == null) {
                Location_cur = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        }

        if (audio_url != null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audio_url);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                    }

                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.pause();
                    }
                });

                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        mGeofencing = new Geofencing(this, mClient);
//        mGeofencing.setAudioUrl(audio_url);

//        Place place=new google.maps.LatLng(-33.867, 151.195);
        mGeofencing.updateGeofencesList(Double.parseDouble(lang), Double.parseDouble(lontitude));
//

        audio_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (geo_status) {
                    start_time = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    geo_status=false;
                    audio_play.setImageResource(R.drawable.stop);
                    geofencing_status.setText(R.string.stop_geofencing);
                    mGeofencing.registerAllGeofences();
                    if(mediaPlayer!=null){
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                    }
                } else {
                    geo_status=true;
                    mGeofencing.unRegisterAllGeofences();
                    geofencing_status.setText(R.string.start_geofencing);
                    audio_play.setImageResource(R.drawable.play);
                }
            }

        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer!=null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.release();
            }
        }
    }

    private void OnGPS() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10.0f));


        // Add a Destination in Map and move the camera
        sydney = new LatLng(Double.parseDouble(lang), Double.parseDouble(lontitude));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));
        mMap.addMarker(new MarkerOptions().position(sydney).title("Destination"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        getMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == 100) {
            if (!verifyAllPermissions(grantResults)) {
                Toast.makeText(getApplicationContext(), "No sufficient permissions", Toast.LENGTH_LONG).show();
            } else {
                getMyLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean verifyAllPermissions(int[] grantResults) {

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void getMyLocation() {
        if(Location_cur==null) {
            return;
        }
        mOrigin = new LatLng(Location_cur.getLatitude(), Location_cur.getLongitude());
        // Getting LocationManager object from System Service LOCATION_SERVICE
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Toast.makeText(getApplicationContext(), mDestination.toString(), Toast.LENGTH_LONG).show();
        if (mOrigin != null && mDestination != null)
            drawRoute();
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mOrigin = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOrigin, 12));

                if (mOrigin != null && mDestination != null)
                    drawRoute();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                mMap.setMyLocationEnabled(true);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, mLocationListener);

//                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//                    @Override
//                    public void onMapLongClick(LatLng latLng) {
//                        mDestination = latLng;
//                        mMap.clear();
//                        mMarkerOptions = new MarkerOptions().position(mDestination).title("Destination");
//                        mMap.addMarker(mMarkerOptions);
//                        if(mOrigin != null && mDestination != null)
//                            drawRoute();
//                    }
//                });
//                drawRoute();
            } else {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, 100);
            }
        }
    }


    private void drawRoute() {

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mOrigin, mDestination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String mode = "mode=walking";

        // Key
        String key = "key=" + "AIzaSyCcgAmTUTcrnaQb5BCFGr8lW0YwIIOjlMM";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&"+mode +"&"+ key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        Log.i("here", url);
        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        double distance;

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            double distance_travel;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                distance = parser.getDistanceInfo(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            distance_textview.setText(String.format(String.valueOf(distance))+"Km");

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRingerPermissionsClicked(View view) {
        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 100 && resultCode == RESULT_OK) {
//
//
//            mGeofencing.updateGeofencesList(place);
//
//
//
//
//
//    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        mGeofencing = new Geofencing(this, mClient);
//        mGeofencing.setAudioUrl(audio_url);

//        Place place=new google.maps.LatLng(-33.867, 151.195);
        mGeofencing.updateGeofencesList(Double.parseDouble(lang), Double.parseDouble(lontitude));
//

        audio_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (geo_status) {
                    start_time = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    geo_status=false;
                    audio_play.setImageResource(R.drawable.stop);
                    geofencing_status.setText(R.string.stop_geofencing);
                    mGeofencing.registerAllGeofences();
                    if(mediaPlayer!=null){
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                    }
                } else {
                    geo_status=true;
                    mGeofencing.unRegisterAllGeofences();
                    geofencing_status.setText(R.string.start_geofencing);
                    audio_play.setImageResource(R.drawable.play);
                }
            }

        });

    }

    /***
     * Called when the Google API Client is suspended
     *
     @param cause cause The reason for the disconnection. Defined by constants CAUSE_.
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /***
     * Called when the Google API Client failed to connect to Google Play Services
     *
     * @param result A ConnectionResult that can be used for resolving the error
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {


    }
}

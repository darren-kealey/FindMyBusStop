package com.findstop.darren.findmystop;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.graphics.Color.GREEN;

public class FindStops extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        ResultCallback<Status> {

    private static final String TAG = FindStops.class.getSimpleName();

    private GoogleMap map;
    private GoogleApiClient gAPI;
    private Button trackon;
    private Button trackoff;
    private LocationRequest mLocation;
    private TextView DistanceDuration;
    private TextView TimeDuration;
    private Marker location_marker;
    private String dataNIOb = "https://www.opendatani.gov.uk/dataset/495c6964-e8d2-4bf1-9942-8d950b3a0ceb/resource/240bec2c-8bdd-41be-9616-d8041c6027f1/download/bus-stop-list-february-2016.geojson";
    private com.findstop.darren.findmystop.LocationTracker gps;
    private Polyline routeLine;
    private ProgressDialog pDialog;
    private MapFragment mapFragment;
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";
    private Marker geoFenceMarker;
    private static final long geofenceDur = 60 * 60 * 1000;
    private static final String request_id = "Geofence ID";
    private static final float distance_radius = 2000; // in meters
    private PendingIntent geoFencePendingIntent;
    private final int request_code = 0;
    private Circle geoFenceLimits;

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, sevenAInward.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_stops);

        // initialize GoogleMaps
        initGMaps();
        // create GoogleApiClient
        setupAPI();



        DistanceDuration = (TextView) findViewById(R.id.distance);
        TimeDuration = (TextView) findViewById(R.id.time);
        Toast.makeText(getBaseContext(), "TIP : Select a Bus Stop and track your distance from it!", Toast.LENGTH_LONG).show();
        makeJsonObReq();
        geofenceButtons();
        gAPI.connect();
    }

    // Create GoogleApiClient instance
    private void setupAPI() {

        gAPI = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .addApi( LocationServices.API )
                .build();


    }



    // Check for permission to access Location
    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }



    // Initialize GoogleMaps
    private void initGMaps(){
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        map.setMyLocationEnabled(true); // If permission is granted for the phone to


        routeLine = null;
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {


            @Override
            public boolean onMarkerClick(Marker marker) {


                if(routeLine != null){
                    routeLine.remove();
                    removeGeofence();
                }


                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                gps = new LocationTracker(FindStops.this);


                LatLng geopos = marker.getPosition();
                double geolat = geopos.latitude;
                double geolong = geopos.longitude;

                geoFenceMarker = map.addMarker(new MarkerOptions() // Creates a new marker for every Stop using the long and lat data in the geojson
                        .position(new LatLng(geolat, geolong))
                        .visible(false)




                );




                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                location_marker = map.addMarker(new MarkerOptions() // Creates a new marker for every Stop using the long and lat data in the geojson
                        .position(new LatLng(latitude, longitude))
                        .visible(false)


                );

                LatLng origin = marker.getPosition();
                LatLng dest = location_marker.getPosition();

                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(origin, dest);

                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);



                return false;
            }
        });


    }





    @Override
    public void onLocationChanged(Location location) {

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocation = LocationRequest.create();
        mLocation.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocation.setInterval(8000);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(gAPI, mLocation, this);
    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {

    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Get last known location










    // Start Geofence creation process
    private void initiateGeofence() {
        if( geoFenceMarker != null ) {
            Geofence newFence = createGeofence( geoFenceMarker.getPosition(), distance_radius );
            GeofencingRequest georequest = createGeofenceRequest(newFence);
            addGeofence( georequest );
        }
    }



    // Create a Geofence
    private Geofence createGeofence( LatLng latLng, float radius ) {

        return new Geofence.Builder()
                .setRequestId(request_id)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(geofenceDur)
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }


    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence(geofence)
                .build();
    }


    private PendingIntent createGeofencePendingIntent() {
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent( this, GeofenceIntentClass.class);
        return PendingIntent.getService(
                this, request_code, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    gAPI,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            createGeofence();
        } else {
            // inform about fail
        }
    }


    private void createGeofence() {

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(GREEN)
                .fillColor(Color.argb(100, 38,255,89) )
                .radius( distance_radius );
        geoFenceLimits = map.addCircle( circleOptions );
    }







    // Clear Geofence
    private void removeGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                gAPI,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    deleteGeofence();
                }
            }
        });
    }

    private void deleteGeofence() {
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }








    private void makeJsonObReq() {

        RequestQueue queue = Volley.newRequestQueue(this); // Creates new HTTP Volley Request



        final Handler handle = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                pDialog.incrementProgressBy(2); // Incremented By Value 2
            }
        };

        pDialog = new ProgressDialog(this);
        pDialog.setMax(100); // Progress Dialog Max Value
        pDialog.setMessage("Loading Closest Bus Stops..."); // Setting Message
        pDialog.setTitle("563 Jordanstown - City Centre"); // Setting Title
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Horizontal
        pDialog.show(); // Display Progress Dialog
        pDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (pDialog.getProgress() <= pDialog.getMax()) {
                        Thread.sleep(200);
                        handle.sendMessage(handle.obtainMessage());

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();








        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                dataNIOb, null, new Response.Listener<JSONObject>() { // Requesting a new JSON Object from the geojson file

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    // Parsing json object response
                    // response will be a json object
                    JSONArray feat = response.getJSONArray("features");


                    for (int i = 0; i < feat.length(); i++) { // JSON Array within the object request which loops through each feature object
                        JSONObject jsonObject = feat.getJSONObject(i);
                        JSONObject prop = jsonObject.getJSONObject("properties");
                        Double li = prop.getDouble("LocationID");
                        String home = prop.getString("Stop_Name");
                        Double lati = prop.getDouble("Latitude");
                        Double longa = prop.getDouble("Longitude");
                        if (li == 3571.000000 || li == 3573.000000 || li == 3493.000000 || li == 1391.000000 || li == 1487.000000 || li == 1488.000000 || li == 1395.000000 || li == 1405.000000 || li == 1409.000000
                                || li == 15285.000000 || li == 3498.000000) {

                            map.addMarker(new MarkerOptions() // Creates a new marker for every Stop using the long and lat data in the geojson
                                    .title(home)
                                    .position(new LatLng(lati, longa))
                            );

                            LatLng belfast = new LatLng(54.677007, -6.937866);
                            CameraUpdate zoomNI = CameraUpdateFactory.newLatLngZoom(belfast, 7);
                            map.animateCamera(zoomNI);
                            pDialog.dismiss();


                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    pDialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                pDialog.dismiss();

            }
        });

        // Adding request to request queue
        queue.add(jsonObjReq);


    }





    public String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "key=AIzaSyBN8WwlyU3T07RumwCBF5bLSjmXiDEpA_Y";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

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
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }








    // Fetches data from url passed
    public class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
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
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                GoogleDirectionsParser parser = new GoogleDirectionsParser();

                // Starts parsing data
                routes = parser.parse(jObject);
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
            String distance = "";
            String duration = "";

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();


                List<HashMap<String, String>> path = result.get(i);


                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(GREEN);
            }

            DistanceDuration.setText("  Distance: " + distance);
            TimeDuration.setText("  Duration: " + duration);

            routeLine = map.addPolyline(lineOptions);
        }




    }


    public void geofenceButtons(){

        trackon = (Button)findViewById(R.id.trackOn);
        trackoff = (Button)findViewById(R.id.trackOff);

        trackon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(routeLine == null){
                    Toast.makeText(getBaseContext(), "You must click on a Bus Stop before the Tracker can be activated!", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getBaseContext(), "You will receive an alert when you are less than a kilometre from this Stop", Toast.LENGTH_LONG).show();
                    initiateGeofence();
                }

            }
        });

        trackoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeGeofence();

            }
        });


    }








}
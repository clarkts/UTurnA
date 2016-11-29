package com.example.owner.uturn;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String key = "AIzaSyDVZmcC3r7c5glJWmGh-c30DnUQKvnHTxA";
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private LocationManager lm;
    private Location location;
    private boolean center;
    double startLng, startLat;
    LatLng latLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        final Button routeButton = (Button) findViewById(R.id.button_route);
        routeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    route(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        final Button navButton = (Button) findViewById(R.id.button_nav);
        navButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                view.bringToFront();
                nav(view);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
            startLng = location.getLongitude();
            startLat = location.getLatitude();
            latLng = new LatLng(startLat,startLng);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, latLng.toString(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            mPermissionDenied = false;
        }
    }

    public void route(View view) throws IOException {
        int sum = 0;
        mMap.clear();
        Random random = new Random();
        int decider = random.nextInt(4)+ 1;
        double rand = random.nextDouble();
        LatLng strt = latLng;
        LatLng end = new LatLng(strt.latitude + rand, strt.longitude - rand);
        final String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + strt.latitude + "," + strt.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&avoid=highways"
                + "&key=" + key;
        new FetchUrl().execute(new String[]{url});

        double rand1 = random.nextDouble();
        LatLng strt1 = end;
        LatLng end1 = new LatLng(strt1.latitude - rand1, strt1.longitude - rand1);
        final String url1 = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + strt1.latitude + "," + strt1.longitude
                + "&destination=" + end1.latitude + "," + end1.longitude
                + "&avoid=highways"
                + "&key=" + key;
        new FetchUrl().execute(new String[]{url1});

        double rand2 = random.nextDouble();
        LatLng strt2 = end1;
        LatLng end2 = new LatLng(strt2.latitude - rand2, strt2.longitude - rand2);
        final String url2 = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + strt2.latitude + "," + strt2.longitude
                + "&destination=" + end2.latitude + "," + end2.longitude
                + "&avoid=highways"
                + "&key=" + key;
        new FetchUrl().execute(new String[]{url2});

        double rand3 = random.nextDouble();
        LatLng strt3 = end2;
        LatLng end3 = strt;
        final String url3 = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + strt3.latitude + "," + strt3.longitude
                + "&destination=" + end3.latitude + "," + end3.longitude
                + "&avoid=highways"
                + "&key=" + key;
        new FetchUrl().execute(new String[]{url3});
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urll) {
            String data = "";
            try {
                data = downloadUrl(urll[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL u = new URL(strUrl);
            urlConnection = (HttpURLConnection)u.openConnection();
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();
                routes = parser.parse(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
        }

    }

    public void nav(View view) {
        center = !center;
        Toast.makeText(this, "Good job", Toast.LENGTH_SHORT).show();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
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
}

package com.example.cowie_inclass11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/*
Thomas Cowie
MainActivity.java
InClass11
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final OkHttpClient client = new OkHttpClient();
    final String TAG = "MainActivity";
    private ArrayList<LatLng> points;
    GoogleMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        points = new ArrayList<>();

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.containerView, mapFragment)
                .commit();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        //You should always use a builder to build the query parameters because of users submitting incorrect stuff
        HttpUrl url = HttpUrl.parse("https://www.theappsdr.com").newBuilder() //Example of how to create a url
                .addPathSegment("map")
                .addPathSegment("route")
                .build();


        Request request = new Request.Builder()
                .url(url) //You'd put url here
                .build();

        //  ****GET****
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    String bodyString = responseBody.string();
                    Log.d(TAG, "onResponse: " + bodyString);
                    //Get the points
                    JSONObject jsonResponse = new JSONObject(bodyString);
                    JSONArray jsonListOfPoints = jsonResponse.getJSONArray("path");
                    //Populate ArrayList of Points
                    for(int i = 0; i < jsonListOfPoints.length(); i++) {
                        LatLng point = new LatLng((Double) jsonListOfPoints.getJSONObject(i).get("latitude"), (Double) jsonListOfPoints.getJSONObject(i).get("longitude"));
                        points.add(point);
                    }
                    Log.d(TAG, "onResponse: " + points.toString());
                    Log.d(TAG, "onResponse: " + points.size());
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.endCap(new SquareCap());
                    polylineOptions.startCap(new RoundCap());
                    polylineOptions.addAll(points);


//                    Headers responseHeaders = response.headers();
//                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }

                    //***THIS IS HOW YOU RUN THINGS ON UI THREAD****
                    runOnUiThread(new Runnable() {  //This is how you change ui with stuff done on the thread.
                        @Override
                        public void run() {
                            //Draw Line
                            Polyline polyline = map.addPolyline(polylineOptions);

                            //Draw Markers
                            Marker marker = map.addMarker(
                                    new MarkerOptions()
                                            .position(points.get(0))
                                            .zIndex(10)); // Optional.
                            Marker marker2 = map.addMarker(
                                    new MarkerOptions()
                                            .position(points.get(points.size() - 1))
                                            .zIndex(10)); // Optional.
                            //Map Move
                            LatLngBounds bounds = new LatLngBounds.Builder()
                                    .include(points.get(0))
                                    .include(points.get(35))
                                    .build();
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 600, 600, 1));
                        }
                    });
                    //System.out.println(responseBody.string());  //responseBody.string is important so it doesn't read a null buffer.
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
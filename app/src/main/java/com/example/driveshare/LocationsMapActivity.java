package com.example.driveshare;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class LocationsMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button acceptRequestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations_map);

        acceptRequestBtn = findViewById(R.id.accept_request_button);

        acceptRequestBtn.setText("Give " + getIntent().getStringExtra("pUsername") + " a ride");

        acceptRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(LocationsMapActivity.this, getIntent().getStringExtra("pUsername"), Toast.LENGTH_SHORT).show();

                final ParseQuery<ParseObject> rideRequestQuery = ParseQuery.getQuery("RequestRide");
                rideRequestQuery.whereEqualTo("username", getIntent().getStringExtra("pUsername"));
                rideRequestQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects.size() > 0 && e == null) {
                            for (ParseObject request : objects) {
                                request.put("requestAccepted", true);

                                request.put("assignedDriver", ParseUser.getCurrentUser().getUsername());
                                request.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Intent mapIntent =
                                                    new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("http://maps.google.com/maps?saddr="
                                                            + getIntent().getDoubleExtra("dLat", 0) + ","
                                                            + getIntent().getDoubleExtra("dLong", 0) + "&" + "daddr="
                                                            + getIntent().getDoubleExtra("pLat", 0) + ","
                                                            + getIntent().getDoubleExtra("pLong", 0)));
                                            startActivity(mapIntent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

//        getIntent.getString

//        Toast.makeText(this, getIntent().getStringExtra("dLat"), Toast.LENGTH_SHORT).show();
        // Add a marker in Sydney and move the camera
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLat", 0),
                getIntent().getDoubleExtra("dLong", 0));
//        mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(dLocation));

        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLat", 0),
                getIntent().getDoubleExtra("pLong", 0));
//        mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(pLocation));

        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        Marker dMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("Driver Location"));
        Marker pMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title("Passenger Location"));

        ArrayList<Marker> markers = new ArrayList<>();
        markers.add(dMarker);
        markers.add(pMarker);

//        Preps bounds to be tied to the driver and passenger markers
        for (Marker marker : markers) {
            latLngBuilder.include(marker.getPosition());
        }

        LatLngBounds bounds = latLngBuilder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cameraUpdate);


    }
}

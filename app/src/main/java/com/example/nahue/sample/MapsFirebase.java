package com.example.nahue.sample;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class MapsFirebase extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final String TAG = "MainActivity";
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mCurrentPositionMarker2;
    FusedLocationProviderClient mFusedLocationClient;
    private String serverKey = "AIzaSyB39stRJZAxFRzpK-gQsjOfmfQxG0YU8ZE";
    LatLng origin;
    LatLng destination;
    private String[] colors = {"#7fff7272", "#7f31c7c5", "#7fff8a00"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PlaceAutocompleteFragment placeAutoComplete;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        placeAutoComplete = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
        placeAutoComplete.setBoundsBias(new LatLngBounds(
                new LatLng(-31.42320, -64.190),
                new LatLng(-31.42320, -64.190)));
        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));

               origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
               destination = new LatLng(place.getLatLng().latitude,place.getLatLng().longitude);

//
//                Polyline polyline1;
//
//                polyline1 = mGoogleMap.addPolyline(new PolylineOptions()
//                        .clickable(true)
//                        .add(
//                                new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),
//                               );
// Store a data object with the polyline, used here to indicate an arbitrary type.
//
//                polyline1.setTag("A");
                requestDirection();
//                Log.d("Maps", "Place selected: " + place.getName());


            }


            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
            }
        });

        getSupportActionBar().setTitle("Map Location Activity");


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFrag.getMapAsync(this);
    }




    public void requestDirection() {
//                Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .alternativeRoute(true)
                .transportMode(TransportMode.WALKING)
               .execute(this);
    }
    public void onDirectionSuccess (Direction direction, String rawBody) {
//                Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {

//            Route route = direction.getRouteList().get(0);
            mGoogleMap.addMarker(new MarkerOptions().position(origin));
            mGoogleMap.addMarker(new MarkerOptions().position(destination));

            for (int i = 0; i < direction.getRouteList().size(); i++) {
                Route route = direction.getRouteList().get(i);
                String color = colors[i % colors.length];
                ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                mGoogleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.parseColor(color)));
            }
            setCameraWithCoordinationBounds(direction.getRouteList().get(0));

//                    btnRequestDirection.setVisibility(View.GONE);
        } else {
//                    Snackbar.make(btnRequestDirection, direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }


    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
//        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        DatabaseReference fbLat = FirebaseDatabase.getInstance().getReference();
        DatabaseReference fbLong = FirebaseDatabase.getInstance().getReference().child("long");

        fbLat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String leftLocation = child.child("lat").getValue().toString();
                    String rightLocation = child.child("long").getValue().toString();

                    double location_left = Double.parseDouble(leftLocation);
                    double location_right = Double.parseDouble(rightLocation);
                    LatLng cod = new LatLng(location_left, location_right);
                    MarkerOptions pepe = new MarkerOptions();
                    pepe.position(cod);
                    pepe.title("Current Position");
                    pepe.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    mGoogleMap.addMarker(pepe);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1); // two minute interval
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }


    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
//                if (mCurrLocationMarker != null) {
//                    mCurrLocationMarker.remove();
//                }

                final Double[] lat = {location.getLatitude()};
                final Double[] longi = {location.getLongitude()};
                //Place current location marker
                LatLng latLng = new LatLng(lat[0], longi[0]);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                Date value = new Date();

                DatabaseReference myRef = database.getReference(value.toString()).child("lat");
                DatabaseReference myRef2 = database.getReference(value.toString()).child("long");
                if (Bluetooth.globalVariable == "true") {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
//                    met();
                    myRef.setValue(latLng.latitude);
                    myRef2.setValue(latLng.longitude);
                    System.out.println(Bluetooth.globalVariable + "Bandera 101");
                    Bluetooth.globalVariable = "false";
                    System.out.println(Bluetooth.globalVariable + "Bandera 102");
                }

                /************************************************************************************/

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        lat[0] = (Double) dataSnapshot.getValue();
                        Log.d("valor", String.valueOf(lat[0]));

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });


                myRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        longi[0] = (Double) dataSnapshot.getValue();
                        Log.d("valor", String.valueOf(longi[0]));

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });
            }
        }

//        private void met(){
//            Double ee = 0.00;
//            Double longe = 0.00;
//            double lat;
//            LatLng tutu = new LatLng(lat,longe);
//            MarkerOptions test = new MarkerOptions();
//            test.position(tutu);
//            test.title("Puto el que lee");
//            test.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//            mCurrLocationMarker2 = mGoogleMap.addMarker(test);
//        };
    };
    /************************************************************************************/

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsFirebase.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }

}
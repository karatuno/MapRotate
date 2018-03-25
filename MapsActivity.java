package com.robpercival.maplocationdemo;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,TouchableWrapper.UpdateMapAfterUserInterection {

    private GoogleMap mMap;
    Point center = new Point(720,2500);

    LocationManager locationManager;

    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    }

                }

            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.setPadding(0,1500,0,0);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("", "Can't find style. Error: ", e);
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear();
               // mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
               // Projection projection = mMap.getProjection();
               // center =projection.toScreenLocation(new LatLng(location.getLatitude(), location.getLongitude()));
               // Log.i("center", center.toString());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
             //   Projection projection = mMap.getProjection();

               // center =projection.toScreenLocation(userLocation);
                //Log.i("center", String.valueOf(center));
                mMap.clear();
               // MarkerOptions marker = new MarkerOptions().position(userLocation).title("Your Location");
               // mMap.addMarker(marker);

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                        .target(userLocation)
                        .tilt(67.5f)
                        .zoom(20)
                        .bearing(0)
                        .build()
                ));
            }
        }
    }

    //parameterised function of the interface defined here which was declared in the interface

    @Override
    public void onUpdateMapAfterUserInterection(Point touchpoint,Point newTouchpoint) {
        Point centerOfMap = center;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        final LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
       // Log.i("interface",centerOfMap.toString());
        final float angle = angleBetweenLines(centerOfMap, touchpoint,newTouchpoint);
        // abs beacause when we touch somewhere far angle between tp and ntp becomes accordingly large and behaves like spring -ayush
       if(Math.abs(angle)<5) {
           new Handler().post(new Runnable() {
               @Override
               public void run() {
                   // move the camera (NOT animateCamera() ) to new position with "bearing" updated
                   // Log.i("angle", String.valueOf(angle));
                   mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                           .target(userLocation)
                           .tilt(67.5f)
                           .zoom(20)
                           .bearing(mMap.getCameraPosition().bearing - angle)
                           .build()
                   ));

                   // Log.i("bearing", String.valueOf(mMap.getCameraPosition().bearing));
               }
           });
       }

    }
    // finds angle between tp and otp
    public float angleBetweenLines(Point center,Point endLine1,Point endLine2){
        float a = endLine1.x - center.x;
        float b = endLine1.y - center.y;
        float c = endLine2.x - center.x;
        float d = endLine2.y - center.y;

        float atan1 = (float) Math.atan2(a,b);
        float atan2 = (float) Math.atan2(c,d);

        return (float) ((atan1 - atan2) * 180 / Math.PI);
    }
}

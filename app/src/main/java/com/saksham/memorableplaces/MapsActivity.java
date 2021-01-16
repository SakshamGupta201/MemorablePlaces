package com.saksham.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;


    public void centerMapOnLocation(Location location, String title) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 5000, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                try {
                    centerMapOnLocation(lastKnownLocation, "Your Location");
                    Log.i("Success","Success in lastKnownLoc");
                }catch (Exception e){
                    Log.e("Error","Error in LastKnownLocation 1");
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
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        if (intent.getIntExtra("placeNumber", 0) == 0) {
            // Zoom in on user Location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    try {
                        centerMapOnLocation(location, "Your Location");
                    } catch (Exception e) {
                        Log.e("Error", "Error in LocationChanged");
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {

                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {

                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 5000, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    centerMapOnLocation(lastKnownLocation, "Your Location");
                    Log.i("Success","Success in LastKnownLocation");
                }else {
                    Log.e("Error","Error in LastKnownLocation");
                }
            }
            mMap.setOnMapLongClickListener(latLng -> {

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                String address = "";

                try {

                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

                    if (addressList != null && addressList.size() > 0) {
                        if (addressList.get(0).getThoroughfare() != null) {
                            if (addressList.get(0).getSubThoroughfare() != null) {
                                address += addressList.get(0).getSubThoroughfare() + " ";
                            }
                            address += addressList.get(0).getThoroughfare();
                        }
                    }

                }catch (Exception e){
                    Log.e("Error","Error in GeoCoder");
                }
                if (address.equals("")){
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm yyyy-MM-dd");
                    address += simpleDateFormat.format(new Date());
                }
                mMap.addMarker(new MarkerOptions().position(latLng).title(address));

                MainActivity.places.add(address);
                MainActivity.locations.add(latLng);

                MainActivity.arrayAdapter.notifyDataSetChanged();

                Toast.makeText(this,"Location Saved!",Toast.LENGTH_SHORT).show();

            });
        }
        else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);

            centerMapOnLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));

        }
    }
}
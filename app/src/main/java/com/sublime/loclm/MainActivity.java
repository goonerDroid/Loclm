package com.sublime.loclm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.sublime.loclm.utils.Timber;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;


public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private static final int REQUEST_CHECK_SETTINGS = 0;
    private GoogleMap mMap;
    private Observable<Location> locationUpdatesObservable;
    private Disposable updatableLocationDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);

        initLocationUpdates();
    }


    @SuppressLint("MissingPermission")
    private void initLocationUpdates(){
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates(5)
                .setInterval(100);
        locationUpdatesObservable = locationProvider
                .checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .setAlwaysShow(true)
                                .build()
                )
                .doOnNext(locationSettingsResult -> {
                    Status status = locationSettingsResult.getStatus();
                    if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException th) {
                            Timber.e("Error opening settings activity.");
                        }
                    }
                })
                .flatMap(locationSettingsResult -> locationProvider.getUpdatedLocation(locationRequest))
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {//Reference: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
            switch (resultCode) {
                case RESULT_OK:
                    Timber.d("User enabled location");
                    break;
                case RESULT_CANCELED:
                    Timber.d("User Cancelled enabling location");
                    break;
                default:
                    break;
            }

        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onLocationPermissionGranted() {
        updatableLocationDisposable = locationUpdatesObservable
                .subscribe(location -> {
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(15)
                            .build();
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                });
    }


    @Override
    protected void onStop() {
        super.onStop();
        updatableLocationDisposable.dispose();
    }
}

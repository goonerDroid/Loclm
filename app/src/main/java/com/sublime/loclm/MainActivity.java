package com.sublime.loclm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.sublime.loclm.utils.Timber;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;


public class MainActivity extends BaseActivity implements OnMapReadyCallback {


    @BindView(R.id.map)
    MapView mapView;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.et_dest)
    EditText etDest;

    private static final int REQUEST_CHECK_SETTINGS = 0;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private GoogleMap mMap;
    private Observable<Location> lastKnownLocationObservable;
    private Observable<Location> locationUpdatesObservable;
    private Observable<String> addressObservable;
    private Disposable lastKnownLocationDisposable;
    private Disposable updatableLocationDisposable;
    private Disposable addressDisposable;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());
        initLocation(locationProvider);

    }


    @SuppressLint("MissingPermission")
    private void initLocation(ReactiveLocationProvider locationProvider) {
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setNumUpdates((int) FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        getCurrentLocation(locationProvider);
        getLocationUpdates(locationProvider, locationRequest);
        getCurrentAddress(locationProvider, locationRequest);
    }

    @SuppressLint("MissingPermission")
    private void getCurrentAddress(ReactiveLocationProvider locationProvider, LocationRequest locationRequest) {
        addressObservable = locationProvider.getUpdatedLocation(locationRequest)
                .flatMap(location -> locationProvider.getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1))
                .map(addresses -> addresses != null && !addresses.isEmpty() ? addresses.get(0) : null)
                .map(address -> {
                    if (address == null) return "";

                    StringBuilder addressLines = new StringBuilder();
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressLines.append(address.getAddressLine(i)).append('\n');
                    }
                    return addressLines.toString();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(ReactiveLocationProvider locationProvider) {
        lastKnownLocationObservable = locationProvider
                .getLastKnownLocation()
                .observeOn(AndroidSchedulers.mainThread());
    }

    @SuppressLint("MissingPermission")
    private void getLocationUpdates(ReactiveLocationProvider locationProvider, LocationRequest locationRequest) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
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
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        moveCurrentLocCamera(location);
    }

    @OnClick(R.id.iv_location)
    public void onMyLocationClick() {
        moveCurrentLocCamera(location);
    }


    @SuppressWarnings("unchecked")
    @OnClick(R.id.et_dest)
    public void onDestinationViewClick() {
        Intent intent = new Intent(this, PlacesActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            etDest.setTransitionName(getString(R.string.et_destination));

            Pair<View, String> pair1 = Pair.create(etDest, etDest.getTransitionName());
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, pair1);
            startActivity(intent, options.toBundle());
            return;
        }

        startActivity(intent);
    }

    @Override
    protected void onLocationPermissionGranted() {
        mapView.getMapAsync(this);


        lastKnownLocationDisposable = lastKnownLocationObservable
                .subscribe(this::moveCurrentLocCamera);


        updatableLocationDisposable = locationUpdatesObservable
                .subscribe(location -> this.location = location);

        addressDisposable = addressObservable
                .subscribe(addressStr -> tvAddress.setText(addressStr));
    }

    private void moveCurrentLocCamera(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lastKnownLocationDisposable != null) lastKnownLocationDisposable.dispose();
        if (updatableLocationDisposable != null) updatableLocationDisposable.dispose();
        if (addressDisposable != null) addressDisposable.dispose();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

package com.sublime.loclm.activities;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sublime.loclm.R;
import com.sublime.loclm.app.BaseActivity;
import com.sublime.loclm.model.AutocompleteInfo;
import com.sublime.loclm.utils.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class PlacesActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_place)
    EditText etPlace;
    @BindView(R.id.recycler_view)
    RecyclerView placeList;
    @BindView(R.id.progress)
    ProgressBar typingProgress;


    private ReactiveLocationProvider reactiveLocationProvider;
    private CompositeDisposable compositeDisposable;
    private PlaceAdapter placeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        ButterKnife.bind(this);
        initToolbar();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            etPlace.setTransitionName(getString(R.string.et_destination));
        }

        reactiveLocationProvider = new ReactiveLocationProvider(this);

        placeAdapter = new PlaceAdapter();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        placeList.setLayoutManager(mLayoutManager);
        placeList.setItemAnimator(new DefaultItemAnimator());
        placeList.setAdapter(placeAdapter);
    }

    public void initToolbar(){
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> PlacesActivity.super.onBackPressed());
    }

    @Override
    protected void onLocationPermissionGranted() {
        compositeDisposable = new CompositeDisposable();

        Observable<String> queryObservable = RxTextView
                .textChanges(etPlace)
                .map(CharSequence::toString)
                .debounce(1, TimeUnit.SECONDS)
                .filter(s -> !TextUtils.isEmpty(s));

       @SuppressLint("MissingPermission") Observable<Location> lastKnownLocationObservable =
               reactiveLocationProvider.getLastKnownLocation();

        @SuppressWarnings("Convert2MethodRef") Observable<AutocompletePredictionBuffer> suggestionsObservable = Observable
                .combineLatest(queryObservable, lastKnownLocationObservable,
                        (query, currentLocation) -> new QueryWithCurrentLocation(query, currentLocation)).
                        flatMap(q -> {
                            if (q.location == null) return Observable.empty();

                            double latitude = q.location.getLatitude();
                            double longitude = q.location.getLongitude();
                            LatLngBounds bounds = new LatLngBounds(
                                    new LatLng(latitude - 0.05, longitude - 0.05),
                                    new LatLng(latitude + 0.05, longitude + 0.05)
                            );
                            return reactiveLocationProvider.getPlaceAutocompletePredictions(q.query, bounds, null);
                        });

        compositeDisposable.add(suggestionsObservable.subscribe(buffer -> {
            List<AutocompleteInfo> autocompleteInfoList = new ArrayList<>();
            for (AutocompletePrediction prediction : buffer) {
                AutocompleteInfo autocompleteInfo = new AutocompleteInfo();
                autocompleteInfo.setPrimaryText(prediction.getPrimaryText(null).toString());
                autocompleteInfo.setSecondaryText(prediction.getSecondaryText(null).toString());
                autocompleteInfo.setId(prediction.getPlaceId());
                autocompleteInfoList.add(autocompleteInfo);
            }
            buffer.release();
            placeAdapter.setData(autocompleteInfoList);
        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.dispose();
    }

    private static class QueryWithCurrentLocation {
        final String query;
        final Location location;

        private QueryWithCurrentLocation(String query, Location location) {
            this.query = query;
            this.location = location;
        }
    }

}

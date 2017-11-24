package com.sublime.loclm;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

public class PlacesActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_place)
    EditText etPlace;


    private ReactiveLocationProvider reactiveLocationProvider;

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
    }

    public void initToolbar(){
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> PlacesActivity.super.onBackPressed());
    }

    @Override
    protected void onLocationPermissionGranted() {

    }
}

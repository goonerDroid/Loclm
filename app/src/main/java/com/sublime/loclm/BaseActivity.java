package com.sublime.loclm;

import android.Manifest;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Created by goonerdroid
 * on 21/11/17.
 */

public abstract class BaseActivity extends FragmentActivity {


    @Override public void onStart() {
        super.onStart();

        new RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        onLocationPermissionGranted();
                    } else {
                        Toast.makeText(BaseActivity.this,
                                "We,need this permission to fetch location updates", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected abstract void onLocationPermissionGranted();
}

package com.simprints.id;

import android.Manifest.permission;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.schibsted.spain.barista.permission.PermissionGranter;

import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.RealmConfiguration;

public class FirstUseTest {

    private static final List<String> PERMISSIONS = new ArrayList<>(Arrays.asList(
            permission.ACCESS_NETWORK_STATE,
            permission.BLUETOOTH,
            permission.INTERNET,
            permission.ACCESS_FINE_LOCATION,
            permission.RECEIVE_BOOT_COMPLETED,
            permission.WAKE_LOCK,
            permission.VIBRATE
    ));

    private RealmConfiguration realmConfiguration;

    protected void setRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    @Before
    public void setUp() {
        Log.d("EndToEndTests", "FirstUseTest.setUp(): cleaning app data");
        Utils.clearApplicationData(InstrumentationRegistry.getContext(), realmConfiguration);
        for (String permission : PERMISSIONS) PermissionGranter.allowPermissionsIfNeeded(permission);
    }

    @After
    public void tearDown() {
        Log.d("EndToEndTests", "FirstUseTest.tearDown(): nothing");
    }
}

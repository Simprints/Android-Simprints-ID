package com.simprints.id;

import android.Manifest.permission;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.schibsted.spain.barista.permission.PermissionGranter;
import com.simprints.remoteadminclient.ApiException;
import com.simprints.remoteadminclient.api.DefaultApi;

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
    private String apiKey;

    protected void setRealmConfiguration(RealmConfiguration realmConfiguration) {
        this.realmConfiguration = realmConfiguration;
    }

    protected void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Before
    public void setUp() throws ApiException {
        Log.d("EndToEndTests", "FirstUseTest.setUp(): cleaning app data");

        // Clear any internal data
        StorageUtils.clearApplicationData(InstrumentationRegistry.getContext(), realmConfiguration);

        // Clear the project for the test's APIkey via remote admin
        DefaultApi apiInstance = RemoteAdminUtils.INSTANCE.getConfiguredApiInstance();
        RemoteAdminUtils.clearProjectNode(apiInstance, apiKey);

        // Allow all first-app permissions and dismiss the dialog box
        for (String permission : PERMISSIONS) PermissionGranter.allowPermissionsIfNeeded(permission);
    }

    @After
    public void tearDown() {
        Log.d("EndToEndTests", "FirstUseTest.tearDown(): nothing");
    }
}

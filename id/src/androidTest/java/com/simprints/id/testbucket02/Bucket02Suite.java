package com.simprints.id.testbucket02;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.simprints.id.TestingUtilities.Client;
import com.simprints.remoteadminclient.ApiException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({SampleTest01.class, SampleTest02.class})
public class Bucket02Suite {
    @BeforeClass
    public static void setUp() throws ApiException {
        Log.d("EndToEndTests", "Bucket02Suite.suiteSetUp(): ensuring bt & wifi are off");
        Client.toggleBluetoothSync(false, InstrumentationRegistry.getContext());
        Client.toggleWifiSync(false, InstrumentationRegistry.getContext());
    }

    @AfterClass
    public static void tearDown() throws ApiException {
        Log.d("EndToEndTests", "Bucket02Suite.suiteTearDown(): doing nothing.");
    }

}

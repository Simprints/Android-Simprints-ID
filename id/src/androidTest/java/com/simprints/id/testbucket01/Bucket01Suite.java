package com.simprints.id.testbucket01;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.simprints.id.TestingUtilities.Client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({SampleTest01.class, SampleTest02.class})
public class Bucket01Suite {

    // Now, let's say that the we put in bucket 01 all the tests that need:
    // - wifi on
    // - bluetooth on
    // - paired to a scanner (v6) with a valid fingerprint on it

    private static final String macAddress = "F0:AC:D7:CA:BE:1D";

    @BeforeClass
    public static void suiteSetUp() {
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring bluetooth is enabled");
        Client.toggleBluetoothSync(true, InstrumentationRegistry.getContext());
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring wifi is enabled");
        Client.toggleWifiSync(true, InstrumentationRegistry.getContext());
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring scanner is paired");
        Client.togglePairingSync(macAddress, true, InstrumentationRegistry.getContext());

    }

    @AfterClass
    public static void suiteTearDown() {
        Log.d("EndToEndTests", "Bucket01Suite.suiteTearDown(): unpairing scanner");
        Client.togglePairingSync(macAddress, false, InstrumentationRegistry.getContext());
    }

}

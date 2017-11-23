package com.simprints.id.bucket01;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.simprints.cerberuslibrary.BluetoothUtility;
import com.simprints.cerberuslibrary.WifiUtility;
import com.simprints.cerberuslibrary.services.UtilityServiceClient;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        HappyWorkflow.class
})
public class Bucket01Suite {

    private static final String macAddress = "F0:AC:D7:C0:E1:A3"; //"F0:AC:D7:CA:BE:1D";
    private static final String networkSsid = "Simprints 2.0";
    private static final String networkPassword = "Tech4Dev";

    @BeforeClass
    public static void suiteSetUp() {
        UtilityServiceClient client = new UtilityServiceClient(InstrumentationRegistry.getContext());
        BluetoothUtility bluetoothUtility = new BluetoothUtility(client);
        WifiUtility wifiUtility = new WifiUtility(client);

        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring bluetooth is enabled");
        bluetoothUtility.setBluetoothStateSync(true);
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring wifi is enabled");
        wifiUtility.setWifiStateSync(true);
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring scanner is paired");
        bluetoothUtility.setBluetoothPairingStateSync(macAddress, true);
        Log.d("EndToEndTests", "Bucket01Suite.suiteSetUp(): ensuring wifi is connected");
        wifiUtility.setWifiNetworkSync(networkSsid, networkPassword);
    }

    @AfterClass
    public static void suiteTearDown() {
        UtilityServiceClient client = new UtilityServiceClient(InstrumentationRegistry.getContext());
        BluetoothUtility bluetoothUtility = new BluetoothUtility(client);
        Log.d("EndToEndTests", "Bucket01Suite.suiteTearDown(): un-pairing scanner");
        bluetoothUtility.setBluetoothPairingStateSync(macAddress, false);
    }

}

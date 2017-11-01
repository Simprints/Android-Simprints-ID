package com.simprints.id.TestingUtilities;

import android.content.Context;
import android.os.Bundle;


public class Client {

    public static void toggleBluetoothSync(Boolean enable, Context context) {
        IntentServiceClient.callSync(context, enable ? Action.BLUETOOTH_ON : Action.BLUETOOTH_OFF);
    }

    public static void toggleWifiSync(Boolean enable, Context context) {
        IntentServiceClient.callSync(context, enable ? Action.WIFI_ON : Action.WIFI_OFF);
    }

    public static void togglePairingSync(String macAddress, Boolean enable, Context context) {
        Bundle extras = new Bundle();
        extras.putString(Extra.MAC_ADDRESS, macAddress);
        IntentServiceClient.callSync(context, enable ? Action.BLUETOOTH_PAIR : Action.BLUETOOTH_UNPAIR, extras);
    }

}

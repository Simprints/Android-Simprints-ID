package com.simprints.libscanner;

import android.util.Log;

import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter;
import com.simprints.libscanner.bluetooth.BluetoothComponentDevice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ScannerUtils {

    private final static Pattern SCANNER_ADDR = Pattern.compile("F0:AC:D7:C\\p{XDigit}:\\p{XDigit}{2}:\\p{XDigit}{2}");
    private final static Pattern MAC_ADDR = Pattern.compile("\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}:\\p{XDigit}{2}");
    private final static String SERIAL_PREFIX = "SP";
    private final static String MAC_ADDRESS_PREFIX = "F0:AC:D7:C";

    /**
     * Checks that a MAC address is a Simprint's scanner address
     *
     * @param macAddress The MAC address to check
     * @return True if and only if the specified MAC address is a valid scanner address
     */
    public static boolean isScannerAddress(String macAddress) {
        boolean result = SCANNER_ADDR.matcher(macAddress).matches();
        log(String.format("isScannerAddress(%s) -> %s", macAddress, result));
        return result;
    }

    /**
     * Checks that a string is a valid MAC address
     *
     * @param macAddress The string to check
     * @return True if and only if the specified string is MAC address
     * formatted as XX:XX:XX:XX:XX:XX where X is an hexadecimal digit
     */
    public static boolean isMACAddress(String macAddress) {
        boolean result = MAC_ADDR.matcher(macAddress).matches();
        log(String.format("isMACAddress(%s) -> %s", macAddress, result));
        return result;
    }

    public static String convertAddressToSerial(String macAddress) {
        return SERIAL_PREFIX + new BigInteger(macAddress
                .replace(MAC_ADDRESS_PREFIX, "")
                .replace(":", ""), 16);
    }

    /**
     * Checks which ones of the paired bluetooth devices are Simprint's scanners
     *
     * @return a list of paired Simprint's scanners
     */
    public static List<String> getPairedScanners(BluetoothComponentAdapter bluetoothAdapter) {
        ArrayList<String> pairedScanners = new ArrayList<>();
        String address;
        for (BluetoothComponentDevice d : bluetoothAdapter.getBondedDevices()) {
            address = d.getAddress();
            if (isScannerAddress(address)) {
                pairedScanners.add(address);
            }
        }
        return pairedScanners;
    }

    public static void log(String s) {
        Log.d("libscanner", s);
    }

}

package com.simprints.libscanner;

import android.support.annotation.NonNull;

import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter;
import com.simprints.libscanner.bluetooth.BluetoothComponentDevice;
import com.simprints.libscanner.bluetooth.BluetoothComponentSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.simprints.libscanner.ScannerUtils.log;

class BluetoothConnection extends Thread {

    private final static UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String macAddress;
    private final ScannerCallback callback;
    private String scannerId;
    private volatile MessageDispatcher msgDispatcher;
    private volatile BluetoothComponentAdapter bluetoothAdapter;
    private volatile BluetoothComponentSocket socket;
    private volatile boolean open;

    BluetoothConnection(@NonNull String macAddress,
                        @NonNull ScannerCallback callback,
                        @NonNull BluetoothComponentAdapter bluetoothAdapter)
    {
        this.bluetoothAdapter = bluetoothAdapter;
        this.macAddress = macAddress;
        this.callback = callback;
        this.msgDispatcher = null;
        this.socket = null;
        this.open = false;
    }

    public void run()
    {
        // Make sure bluetooth is supported
        if (bluetoothAdapter.isNull()) {
            callback.onFailure(SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED);
            return;
        }

        // Make sure bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            callback.onFailure(SCANNER_ERROR.BLUETOOTH_DISABLED);
            return;
        }

        BluetoothComponentDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        scannerId = device.getName();

        // Make sure the scanner is bonded to the phone
        if (!device.isBonded()) {
            callback.onFailure(SCANNER_ERROR.SCANNER_UNBONDED);
            return;
        }

        InputStream inputStream;
        OutputStream outputStream;
        try {
            // Get the Bluetooth Socket
            socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
            // Connect the socket
            bluetoothAdapter.cancelDiscovery();
            socket.connect();
            // Get IO streams
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException ioException) {
            log(String.format("Bluetooth connection: %s", ioException));
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            callback.onFailure(SCANNER_ERROR.IO_ERROR);
            return;
        }

        // Initialize and start the message dispatcher
        msgDispatcher = new MessageDispatcher(inputStream, outputStream);
        msgDispatcher.start();

        open = true;
        callback.onSuccess();
    }

    void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ioException) {
                log(String.format("Bluetooth disconnection: %s", ioException));
            }
        }
        open = false;
    }

    boolean isOpened() {
        return open;
    }

    String getScannerId() {
        return scannerId;
    }

    MessageDispatcher getMessageDispatcher()
    {
        return msgDispatcher;
    }
}

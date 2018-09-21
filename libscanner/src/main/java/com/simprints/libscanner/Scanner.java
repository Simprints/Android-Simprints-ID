package com.simprints.libscanner;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.libscanner.bluetooth.BluetoothComponentAdapter;
import com.simprints.libscanner.enums.HARDWARE_CONFIG;
import com.simprints.libscanner.enums.LED_STATE;
import com.simprints.libscanner.enums.SDK_ERROR;
import com.simprints.libscanner.enums.UN20_STATE;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.simprints.libscanner.SCANNER_ERROR.OFF;
import static com.simprints.libscanner.SCANNER_ERROR.UN20_SDK_ERROR;
import static com.simprints.libscanner.ScannerUtils.log;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Scanner {

    private final static long TIMEOUT_MS = 2000;
    private final static long UN20_POLL_MS = 200;
    private final static byte NO_INFO = -1;

    // Messages
    private final static Message UN20_WAKEUP_REQUEST = Message.UN20Wakeup();
    private final static Message GET_SENSOR_INFO_REQUEST = Message.getSensorInfo();
    private final static Message ENABLE_FINGER_CHECK_REQUEST = Message.enableFingerCheck();
    private final static Message DISABLE_FINGER_CHECK_REQUEST = Message.disableFingerCheck();
    private final static Message CAPTURE_IMAGE_REQUEST = Message.captureImage();
    private final static Message IMAGE_QUALITY_REQUEST = Message.imageQuality();
    private final static Message GENERATE_TEMPLATE_REQUEST = Message.generateTemplate();

    private final AtomicBoolean available;
    private volatile BluetoothConnection connection;
    private final String macAddress;
    private final BluetoothComponentAdapter bluetoothAdapter;

    private volatile short ucVersion;        // ARM controller firmware version
    private volatile short un20Version;      // UN20 client firmware version
    private volatile short batteryLevel1;
    private volatile short batteryLevel2;
    private volatile boolean crashLogValid;
    private volatile byte hwVersion;
    private volatile UN20_STATE un20State;

    private AtomicBoolean interrupted;
    private volatile short latestImageQuality;
    private volatile byte[] latestTemplate;
    private volatile SDK_ERROR latestSdkError;

    /**
     * Creates a new Scanner object to interact with a scanner of specified MAC address
     * @param macAddress The MAC address of the scanner
     */
    public Scanner(@NonNull String macAddress, @NonNull BluetoothComponentAdapter bluetoothAdapter) {
        if (!ScannerUtils.isScannerAddress(macAddress))
            throw new IllegalArgumentException("Invalid scanner mac address");

        this.macAddress = macAddress;
        this.bluetoothAdapter = bluetoothAdapter;
        connection = null;
        available = new AtomicBoolean(true);
        ucVersion = NO_INFO;
        un20Version = NO_INFO;
        batteryLevel1 = NO_INFO;
        batteryLevel2 = NO_INFO;
        crashLogValid = false;
        hwVersion = NO_INFO;
        un20State = UN20_STATE.UNKNOWN;
        interrupted = new AtomicBoolean(true);
        latestImageQuality = NO_INFO;
        latestTemplate = null;
    }

    // ************************* SCANNER REQUESTS *********************************

    /**
     * Connect to the scanner.
     *
     * @param callback The onSuccess() method of this callback is called when the connection is
     *                 established.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> already connected.
     *                 - BLUETOOTH_NOT_SUPPORTED -> use another phone.
     *                 - BLUETOOTH_DISABLED -> open settings to enable bluetooth.
     *                 - SCANNER_UNBONDED -> open settings to pair with scanner.
     *                 - IO_ERROR -> cannot be recovered, probably bug on the scanner side.
     *                 - SCANNER_UNREACHABLE -> get closer to the scanner / turn it on.
     */
    public void connect(@Nullable ScannerCallback callback) {
        log("Scanner.connect()");
        ScannerCallback releaseCallback = this.wrapCallback("Scanner.connect()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else {
            connection = new BluetoothConnection(macAddress, releaseCallback, bluetoothAdapter);
            connection.start();
            // releaseCallback is called by bluetooth connection
        }
    }

    /**
     * Disconnect from the scanner.
     *
     * @param callback The onSuccess() method of this callback is called when disconnected.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected.
     */
    public void disconnect(@Nullable ScannerCallback callback) {
        log("Scanner.disconnect()");
        ScannerCallback releaseCallback = this.wrapCallback("Scanner.disconnect()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else {
            connection.close();
            connection = null;
            releaseCallback.onSuccess();
        }
    }

    /**
     * Wake up the sensor unit of the scanner.
     *
     * @param callback The onSuccess() method of this callback is called when the sensor is up
     *                 and running.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected to the scanner, call connect() first.
     *                 - UN20_LOW_VOLTAGE -> the battery voltage is too low to start the un20 ;
     */
    public void un20Wakeup(@Nullable ScannerCallback callback) {
        log("Scanner.un20Wakeup()");
        final ScannerCallback releaseCallback = this.wrapCallback("Scanner.un20Wakeup()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else
            new Thread() {
                @Override
                public void run() {
                    try {
                        SCANNER_ERROR wakeupError = internal_un20Wakeup();
                        if (wakeupError != null) {
                            releaseCallback.onFailure(wakeupError);
                            return;
                        }
                        un20State = UN20_STATE.STARTING_UP;
                        // Poll until the un20 is actually on
                        do {
                            SystemClock.sleep(UN20_POLL_MS);
                            internal_updateSensorInfo();
                        } while (un20State != UN20_STATE.READY);

                        releaseCallback.onSuccess();
                    } catch (BrokenConnectionException e) {
                        connection.close();
                        connection = null;
                        releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);
                    }
                }
            }.start();
    }

    /**
     * Request the scanner to update the info of the Scanner object, that can then
     * be accessed with getUcVersion(), getUnVersion(), getBatteryLevel1(), getBatteryLevel2(),
     * getHardwareVersion(), getCrashLogValid(), getUn20State()
     *
     * @param callback The onSuccess() method of this callback is called when the sensor info was updated.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected to the scanner, call connect() first.
     */
    public void updateSensorInfo(@Nullable final ScannerCallback callback) {
        log("Scanner.updateSensorInfo()");
        final ScannerCallback releaseCallback = this.wrapCallback("Scanner.updateSensorInfo()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else
            new Thread() {
                @Override
                public void run() {
                    try {
                        internal_updateSensorInfo();
                        releaseCallback.onSuccess();
                    } catch (BrokenConnectionException e) {
                        connection.close();
                        connection = null;
                        releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);
                    }
                }
            }.start();
    }

    /**
     * Request the scanner to capture images until a fingerprint is obtained,
     * (then sets the UI feedback to its quality), or timeout.
     * In case of success, getLatestQualityScore() and getLatestTemplate() can then be used.
     *
     * @param qualityThreshold Threshold for UI feedback
     * @param timeout Milliseconds before timeout
     * @param callback The onSuccess() method of this callback is called when the continuous capture ends successfully.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected to the scanner, call connect() first.
     *                 - UN20_INVALID_STATE -> un20 is not running, call un20wakeup() first.
     *                 - OUTDATED_SCANNER_INFO -> call updateSensorInfo() first
     *                 - SDK_ERROR -> un20 sdk error, call getSdkError() for more details.
     *                 - INTERRUPTED -> the continuous capture was interrupted
     *                 - TIMEOUT -> the continuous capture timed out
     */
    public void startContinuousCapture(final int qualityThreshold, final long timeout, @Nullable ScannerCallback callback) {
        log("Scanner.startContinuousCapture()");
        final ScannerCallback releaseCallback = this.wrapCallback("Scanner.startContinuousCapture()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else if (un20State != UN20_STATE.READY)
            releaseCallback.onFailure(SCANNER_ERROR.UN20_INVALID_STATE);

        else if (ucVersion == NO_INFO)
            releaseCallback.onFailure(SCANNER_ERROR.OUTDATED_SCANNER_INFO);

        else {
            interrupted.set(false);
            new Thread() {
                @Override
                public void run() {
                    long startTime = SystemClock.elapsedRealtime();
                    try {
                        SCANNER_ERROR error;

                        // Set capture leds (middle orange led)
                        internal_setUI(LED_STATE.ONE_ORANGE_LED);

                        // Enable finger check
                        if ((error = internal_setFingerCheck(true)) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }

                        // Loop until :
                        do {
                            // - the continuous capture is interrupted
                            if (interrupted.get()) {
                                internal_setUI(LED_STATE.OFF_LEDS);
                                releaseCallback.onFailure(SCANNER_ERROR.INTERRUPTED);
                                return;
                            }

                            // - the continuous capture times out
                            if (SystemClock.elapsedRealtime() - startTime > timeout) {
                                internal_setUI(LED_STATE.OFF_LEDS);
                                releaseCallback.onFailure(SCANNER_ERROR.TIMEOUT);
                                return;
                            }

                            // - an image with a finger is captured
                        } while (internal_captureImage() != null);

                        // Get the image quality and template
                        if ((error = internal_getImageQuality()) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }
                        if ((error = internal_getTemplate()) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }

                        // Sets the scanner feedback accordingly and return
                        internal_setUI(latestImageQuality > qualityThreshold ? LED_STATE.GREEN_LEDS : LED_STATE.RED_LEDS);
                        releaseCallback.onSuccess();

                    } catch (BrokenConnectionException e) {
                        connection.close();
                        connection = null;
                        releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);
                    }
                }
            }.start();
        }
    }

    /**
     * Stops an ongoing continuous capture
     * @return True if the continuous capture is stopped, false if no continuous capture was ongoing.
     */
    public boolean stopContinuousCapture()
    {
        log("Scanner.stopContinuousCapture()");
        return !interrupted.getAndSet(true);
    }

    /**
     * Register a new button listener whose onClick() method will be called when
     * a trigger button press is detected.
     *
     * @param listener The new button listener
     * @return True in case of success, False in case of failure (scanner not connected)
     */
    public boolean registerButtonListener(@NonNull ButtonListener listener) {
        log("Scanner.registerButtonListener()");
        if (!isConnected())
            return false;

        connection.getMessageDispatcher().registerButtonListener(listener);
        return true;
    }

    /**
     * Unregister a button listener.
     * Unregistering a listener that was not registered suceeds but has no effect.
     *
     * @param listener The button listener
     * @return True in case of success, False in case of failure (scanner not connected)
     */
    public boolean unregisterButtonListener(@NonNull ButtonListener listener) {
        log("Scanner.unregisterButtonListener()");
        if (!isConnected())
            return false;

        connection.getMessageDispatcher().unregisterButtonListener(listener);
        return true;
    }


    /**
     * Request the scanner to force a capture.
     * In case of success, getLatestQualityScore() and getLatestTemplate() can then be used.
     *
     * @param qualityThreshold Threshold for UI feedback
     * @param callback The onSuccess() method of this callback is called when the continuous capture ends successfully.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected to the scanner, call connect() first.
     *                 - UN20_INVALID_STATE -> un20 is not running, call un20wakeup() first.
     *                 - OUTDATED_SCANNER_INFO -> call updateSensorInfo() first
     *                 - SDK_ERROR -> un20 sdk error, call getSdkError() for more details.
     *                 - INTERRUPTED -> the continuous capture was interrupted
     *                 - TIMEOUT -> the continuous capture timed out
     */
    public void forceCapture(final int qualityThreshold, @Nullable ScannerCallback callback) {
        log("Scanner.forceCapture()");
        final ScannerCallback releaseCallback = this.wrapCallback("Scanner.forceCapture()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else if (un20State != UN20_STATE.READY)
            releaseCallback.onFailure(SCANNER_ERROR.UN20_INVALID_STATE);

        else if (ucVersion == NO_INFO)
            releaseCallback.onFailure(SCANNER_ERROR.OUTDATED_SCANNER_INFO);

        else {
            new Thread() {
                @Override
                public void run() {
                    long startTime = SystemClock.elapsedRealtime();
                    try {
                        SCANNER_ERROR error;

                        // Set capture leds (middle orange led)
                        internal_setUI(LED_STATE.ONE_ORANGE_LED);

                        // Disable finger check
                        if ((error = internal_setFingerCheck(false)) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }

                        // Get an image
                        if ((error = internal_captureImage()) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }

                        // Get the image quality and template
                        if ((error = internal_getImageQuality()) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }
                        if ((error = internal_getTemplate()) != null) {
                            releaseCallback.onFailure(error);
                            return;
                        }

                        // Sets the scanner feedback accordingly and return
                        internal_setUI(latestImageQuality > qualityThreshold ? LED_STATE.GREEN_LEDS : LED_STATE.RED_LEDS);
                        releaseCallback.onSuccess();

                    } catch (BrokenConnectionException e) {
                        connection.close();
                        connection = null;
                        releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);
                    }
                }
            }.start();
        }
    }

    /**
     * Request the scanner to reset its UI.
     *
     * @param callback The onSuccess() method of this callback is called when the UI is reset.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected to the scanner, call connect() first.
     */
    public void resetUI(@Nullable ScannerCallback callback) {
        log("Scanner.resetUI()");
        final ScannerCallback releaseCallback = this.wrapCallback("Scanner.resetUI()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else
            new Thread() {
                @Override
                public void run() {
                    try {
                        internal_setUI(LED_STATE.OFF_LEDS);
                        releaseCallback.onSuccess();
                    } catch (BrokenConnectionException e) {
                        connection.close();
                        connection = null;
                        releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);
                    }
                }
            }.start();
    }

    /**
     * Request the scanner to update its hardware configuration.
     * @param hwConfig Hardware configuration
     * @param callback The onSuccess() method of this callback is called when the hw config is set.
     *                 In case of failure, the onFailure() method is called with one of the
     *                 following errors:
     *                 - BUSY -> wait for the current operation to complete.
     *                 - INVALID_STATE -> not connected to the scanner, call connect() first.
     *                 - UN20_INVALID_STATE -> the un20 must be shutdown.
     */
    public void setHardwareConfig(@NonNull final HARDWARE_CONFIG hwConfig, @Nullable ScannerCallback callback) {
        log("Scanner.setHardwareConfig()");
        final ScannerCallback releaseCallback = this.wrapCallback("Scanner.setHardwareConfig()", callback);

        if (!available.getAndSet(false))
            releaseCallback.onFailure(SCANNER_ERROR.BUSY);

        else if (!isConnected())
            releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);

        else {
            new Thread() {
                @Override
                public void run() {
                    try {
                        SCANNER_ERROR error;
                        Message answer = connection.getMessageDispatcher().sendRecv(Message.setHardwareConfig(hwConfig), TIMEOUT_MS);
                        switch (answer.getMessageStatus()) {
                            case GOOD:
                                releaseCallback.onSuccess();
                                return;
                            case UN20_STATE_ERROR:
                                releaseCallback.onFailure(SCANNER_ERROR.UN20_INVALID_STATE);
                                return;
                            default:
                                throw new RuntimeException();
                        }
                    } catch (BrokenConnectionException e) {
                        connection.close();
                        connection = null;
                        releaseCallback.onFailure(SCANNER_ERROR.INVALID_STATE);
                    }
                }
            }.start();
        }
    }

    public boolean isConnected() {
        return connection != null && connection.isOpened();
    }

    public String getScannerId() {
        if (connection == null)
            return null;
        return connection.getScannerId();
    }

    public short getUcVersion() {
        return ucVersion;
    }

    public short getUnVersion() {
        return un20Version;
    }

    public short getBatteryLevel1() {
        return batteryLevel1;
    }

    public short getBatteryLevel2() {
        return batteryLevel2;
    }

    public byte getHardwareVersion() {
        return hwVersion;
    }

    public boolean getCrashLogValid() {
        return crashLogValid;
    }

    public UN20_STATE getUn20State() {
        return un20State;
    }

    public int getImageQuality() {
        return latestImageQuality;
    }

    public byte[] getTemplate() {
        return latestTemplate;
    }

    /**
     * Request the scanner to wake up by sending a UN20_WAKEUP_REQUEST to the scanner
     * without checking if it is busy or not, using the current thread.
     *
     * (If the scanner is currently shutting down, wait until if finishes then turn it on)
     *
     * @return null if the answer status for this request is GOOD, an appropriate error else.
     */
    private SCANNER_ERROR internal_un20Wakeup() throws BrokenConnectionException {
        while (true) {
            Message answer = connection.getMessageDispatcher().sendRecv(UN20_WAKEUP_REQUEST, TIMEOUT_MS);
            switch (answer.getMessageStatus()) {
                case GOOD:
                    return null;
                case CHARGING:
                    return SCANNER_ERROR.OFF;
                case UN20_STATE_ERROR:
                    break;
                case UN20_VOLTAGE:
                    return SCANNER_ERROR.UN20_LOW_VOLTAGE;
                default:
                    throw new RuntimeException();
            }
        }
    }

    /**
     * Update the cached values of the sensor info by sending a GET_SENSOR_INFO to the scanner
     * without checking if it is busy or not, using the current thread.
     *
     * @return null if the answer status for this request is GOOD, an appropriate error else.
     */
    private SCANNER_ERROR internal_updateSensorInfo() throws BrokenConnectionException {
        Message answer = connection.getMessageDispatcher().sendRecv(GET_SENSOR_INFO_REQUEST, TIMEOUT_MS);
        switch (answer.getMessageStatus()) {
            case GOOD:
                break;
            case CHARGING:
                return SCANNER_ERROR.OFF;
            default:
                throw new RuntimeException();
        }

        ucVersion = answer.getUcVersion();
        un20Version = answer.getUn20Version();
        batteryLevel1 = answer.getBatteryLevel1();
        batteryLevel2 = answer.getBatteryLevel2();
        crashLogValid = answer.getCrashLogValid();
        hwVersion = answer.getHardwareVersion();
        un20State = answer.getUn20State();

        return null;
    }

    /**
     * Request the scanner to turn on/off ring leds by sending a SET_UI request to the scanner
     * without checking if it is busy or not, using the current thread.
     *
     * Vibration is always disabled and trigger button always enabled
     *
     * @return null if the answer status for this request is GOOD, an appropriate error else.
     */
    private SCANNER_ERROR internal_setUI(LED_STATE[] leds) throws BrokenConnectionException {
        Message request = Message.setUI(true, true, false, leds, (short) 0);
        Message answer = connection.getMessageDispatcher().sendRecv(request, TIMEOUT_MS);
        switch (answer.getMessageStatus()) {
            case GOOD:
                return null;
            case CHARGING:
                return OFF;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Request the scanner to enable/disable finger check
     * @param enable Set to true to enable finger check, and to false to disable it
     * @return null if the answer status for this request is GOOD, an appropriate error else.
     */
    private SCANNER_ERROR internal_setFingerCheck(boolean enable) throws BrokenConnectionException {
        if (ucVersion > 4) {
            Message request = enable ? ENABLE_FINGER_CHECK_REQUEST : DISABLE_FINGER_CHECK_REQUEST;
            Message answer = connection.getMessageDispatcher().sendRecv(request, TIMEOUT_MS);
            switch (answer.getMessageStatus()) {
                case GOOD:
                    break;
                case SDK_ERROR:
                    return SCANNER_ERROR.UN20_SDK_ERROR;
                case UN20_STATE_ERROR:
                    return SCANNER_ERROR.UN20_INVALID_STATE;
                case CHARGING:
                    return OFF;
                default:
                    throw new RuntimeException();
            }
        }
        return null;
    }

    /**
     * Request the scanner to capture an image
     * @return null if the answer status for this request is GOOD, an appropriate error else.
     */
    private SCANNER_ERROR internal_captureImage() throws BrokenConnectionException {
        Message answer = connection.getMessageDispatcher().sendRecv(CAPTURE_IMAGE_REQUEST, TIMEOUT_MS);
        switch (answer.getMessageStatus()) {
            case GOOD:
                return null;
            case SDK_ERROR:
                latestSdkError = SDK_ERROR.UNKNOWN;
                return SCANNER_ERROR.UN20_SDK_ERROR;
            case SDK_ERROR_CODE:
                latestSdkError = answer.getSDKErr();
                return SCANNER_ERROR.UN20_SDK_ERROR;
            case CHARGING:
                return OFF;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Request the scanner to return the image quality of the latest capture
     * @return null if the answer status for this request is GOOD, an appropriate error else.
     */
    private SCANNER_ERROR internal_getImageQuality() throws BrokenConnectionException {
        Message answer = connection.getMessageDispatcher().sendRecv(IMAGE_QUALITY_REQUEST, TIMEOUT_MS);
        switch (answer.getMessageStatus()) {
            case GOOD:
                latestImageQuality = answer.getImageQuality();
                return null;
            case SDK_ERROR:
                latestSdkError = SDK_ERROR.UNKNOWN;
                return SCANNER_ERROR.UN20_SDK_ERROR;
            case CHARGING:
                return OFF;
            case NO_IMAGE: // Must never happen
                return UN20_SDK_ERROR;
            default:
                throw new RuntimeException();
        }
    }

    private SCANNER_ERROR internal_getTemplate() throws BrokenConnectionException {
        // Generate template
        Message answer = connection.getMessageDispatcher().sendRecv(GENERATE_TEMPLATE_REQUEST, TIMEOUT_MS);
        switch (answer.getMessageStatus()) {
            case GOOD:
                break;
            case SDK_ERROR:
                latestSdkError = SDK_ERROR.UNKNOWN;
                return SCANNER_ERROR.UN20_SDK_ERROR;
            case SDK_ERROR_CODE:
                latestSdkError = answer.getSDKErr();
                return SCANNER_ERROR.UN20_SDK_ERROR;
            case CHARGING:
                return OFF;
            case NO_IMAGE:
            case NO_QUALITY:
            default:
                throw new RuntimeException();
        }

        // Extract template
        short fragmentNo = 0;
        ByteArrayOutputStream templateFragmentsStream = new ByteArrayOutputStream();
        do {
            answer = connection.getMessageDispatcher().sendRecv(Message.getTemplateFragment(fragmentNo), TIMEOUT_MS);
            switch (answer.getMessageStatus()) {
                case GOOD:
                    answer.writeFragmentBytesIn(templateFragmentsStream);
                    fragmentNo++;
                    break;
                case NO_IMAGE:
                default:
                    throw new RuntimeException();
            }
        } while (!answer.isLastFragment());

        latestTemplate = templateFragmentsStream.toByteArray();
        return null;
    }

    private ScannerCallback wrapCallback(@NonNull final String callDescription, @Nullable final ScannerCallback callback) {
        return new ScannerCallback() {
            @Override
            public void onSuccess() {
                log(String.format(Locale.UK, "%s -> success", callDescription));
                available.set(true);
                // Call back on UI thread
                if (callback != null)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess();
                        }
                    });
            }

            @Override
            public void onFailure(final SCANNER_ERROR error) {
                log(String.format(Locale.UK, "%s -> failure (%s)", callDescription, error.name()));
                if (error != SCANNER_ERROR.BUSY)
                    available.set(true);
                if (callback != null)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(error);
                        }
                    });
            }
        };
    }

}

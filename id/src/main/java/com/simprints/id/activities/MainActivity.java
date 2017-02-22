package com.simprints.id.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.simprints.id.R;
import com.simprints.id.adapters.FingerPageAdapter;
import com.simprints.id.fragments.FingerFragment;
import com.simprints.id.model.Finger;
import com.simprints.id.model.FingerRes;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.Dialogs;
import com.simprints.id.tools.Language;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.id.tools.SharedPref;
import com.simprints.id.tools.TimeoutBar;
import com.simprints.id.tools.Vibrate;
import com.simprints.id.tools.ViewPagerCustom;
import com.simprints.libcommon.FingerConfig;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.libcommon.ScanConfig;
import com.simprints.libdata.DatabaseEventListener;
import com.simprints.libdata.DatabaseSync;
import com.simprints.libdata.Event;
import com.simprints.libscanner.Message;
import com.simprints.libscanner.Scanner;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.FingerIdentifier;
import com.simprints.libsimprints.Registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.simprints.id.model.Finger.NB_OF_FINGERS;
import static com.simprints.id.model.Finger.Status;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        Scanner.ScannerListener,
        DatabaseEventListener {

    private final static long AUTO_SWIPE_DELAY = 500;
    private final static int FAST_SWIPE_SPEED = 100;
    private final static int SLOW_SWIPE_SPEED = 1000;

    private final static int ALERT_ACTIVITY_REQUEST_CODE = 0;
    private final static int MATCHING_ACTIVITY_REQUEST_CODE = 1;
    private final static int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    private final static int PRIVACY_ACTIVITY_REQUEST_CODE = 3;
    private final static int ABOUT_ACTIVITY_REQUEST_CODE = 4;

    private final static ScanConfig DEFAULT_CONFIG;

    private SharedPref sharedPref;

    static {
        DEFAULT_CONFIG = new ScanConfig();
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_THUMB, FingerConfig.REQUIRED, 0);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_INDEX_FINGER, FingerConfig.REQUIRED, 1);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_3RD_FINGER, FingerConfig.OPTIONAL, 4);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_4TH_FINGER, FingerConfig.OPTIONAL, 5);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_5TH_FINGER, FingerConfig.OPTIONAL, 6);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_5TH_FINGER, FingerConfig.OPTIONAL, 7);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_4TH_FINGER, FingerConfig.OPTIONAL, 8);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_3RD_FINGER, FingerConfig.OPTIONAL, 9);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_THUMB, FingerConfig.OPTIONAL, 2);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_INDEX_FINGER, FingerConfig.OPTIONAL, 3);
    }

    private AppState appState;

    private Handler handler;

    private Finger[] fingers = new Finger[NB_OF_FINGERS];
    private List<Finger> activeFingers;
    private int currentActiveFingerNo;

    private Message.LED_STATE[] LEDS;

    private Status previousStatus;

    private List<ImageView> indicators;
    private Button scanButton;
    private ViewPagerCustom viewPager;
    private FingerPageAdapter pageAdapter;
    private TimeoutBar timeoutBar;

    private Registration registrationResult;

    private MenuItem continueItem;
    private boolean promptContinue = false;
    private MenuItem syncItem;

    private ProgressDialog un20WakeupDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);

        appState = AppState.getInstance();
        appState.getScanner().setScannerListener(this);
        appState.getData().setListener(this);

        handler = new Handler();

        fingers = new Finger[NB_OF_FINGERS];
        activeFingers = new ArrayList<>();
        currentActiveFingerNo = 0;

        LEDS = new Message.LED_STATE[Message.LED_MAX_COUNT];

        previousStatus = Status.NOT_COLLECTED;

        indicators = new ArrayList<>();
        scanButton = (Button) findViewById(R.id.scan_button);
        viewPager = (ViewPagerCustom) findViewById(R.id.view_pager);
        pageAdapter = new FingerPageAdapter(getSupportFragmentManager(), activeFingers);
        un20WakeupDialog = Dialogs.getUn20Dialog(this);
        registrationResult = null;
        sharedPref = new SharedPref(getApplicationContext());
        timeoutBar = new TimeoutBar(getApplicationContext(),
                (ProgressBar) findViewById(R.id.pb_timeout));

        initActiveFingers();
        initBarAndDrawer();
        initIndicators();
        initScanButton();
        initViewPager();
        refreshDisplay();
    }

    private void initActiveFingers() {
        for (int i = 0; i < NB_OF_FINGERS; i++) {
            FingerIdentifier id = FingerIdentifier.values()[i];
            fingers[i] = new Finger(id, DEFAULT_CONFIG.get(id) == FingerConfig.REQUIRED, false, DEFAULT_CONFIG.getPriority(id));
            if (fingers[i].isActive()) {
                activeFingers.add(fingers[i]);
            }
        }

        activeFingers.get(activeFingers.size() - 1).setLastFinger(true);
        Arrays.sort(fingers);
    }

    private void initBarAndDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        syncItem = navigationView.getMenu().findItem(R.id.nav_sync);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.show();

        switch (appState.getCallout()) {
            case REGISTER:
                actionBar.setTitle(R.string.register_title);
                break;
            case IDENTIFY:
                actionBar.setTitle(R.string.identify_title);
                break;
            case UPDATE:
                actionBar.setTitle(R.string.update_title);
                break;
            case VERIFY:
                actionBar.setTitle(R.string.verify_title);
                break;
        }
    }

    private void initIndicators() {
        LinearLayout indicatorLayout = (LinearLayout) findViewById(R.id.indicator_layout);
        indicatorLayout.removeAllViewsInLayout();
        indicators.clear();
        for (int i = 0; i < activeFingers.size(); i++) {
            ImageView indicator = new ImageView(this);
            indicator.setAdjustViewBounds(true);
            final int finalI = i;
            indicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewPager.setCurrentItem(finalI);
                }
            });
            indicators.add(indicator);
            indicatorLayout.addView(indicator, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void initScanButton() {
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleContinuousCapture();
            }
        });
        scanButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (activeFingers.get(currentActiveFingerNo).getStatus() != Status.COLLECTING) {
                    activeFingers.get(currentActiveFingerNo).setStatus(Status.NOT_COLLECTED);
                    activeFingers.get(currentActiveFingerNo).setTemplate(null);
                    refreshDisplay();
                }
                return true;
            }
        });
    }

    private void toggleContinuousCapture() {
        switch (activeFingers.get(currentActiveFingerNo).getStatus()) {
            case GOOD_SCAN:
                activeFingers.get(currentActiveFingerNo).setStatus(Status.RESCAN_GOOD_SCAN);
                refreshDisplay();
                break;
            case RESCAN_GOOD_SCAN:
            case BAD_SCAN:
            case NOT_COLLECTED:
                scanButton.setEnabled(false);
                appState.getScanner().startContinuousCapture(sharedPref.getQualityThresholdInt(),
                        sharedPref.getTimeoutInt());
                timeoutBar.startTimeoutBar();
                break;
            case COLLECTING:
                scanButton.setEnabled(false);
                appState.getScanner().stopContinuousCapture();
                timeoutBar.cancelTimeoutBar();
                break;
        }
    }

    private void initViewPager() {
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentActiveFingerNo = position;
                refreshDisplay();
                if (LEDS[0] != Message.LED_STATE.LED_STATE_OFF) {
                    if (appState.getScanner() != null) {
                        appState.getScanner().resetUI();
                        Arrays.fill(LEDS, Message.LED_STATE.LED_STATE_OFF);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING;
            }
        });
        viewPager.setCurrentItem(currentActiveFingerNo);
    }

    private void refreshDisplay() {
        // Update indicators display
        int nbCollected = 0;

        promptContinue = true;

        for (int i = 0; i < activeFingers.size(); i++) {
            boolean selected = currentActiveFingerNo == i;
            Finger finger = activeFingers.get(i);
            indicators.get(i).setImageResource(finger.getStatus().getDrawableId(selected));

            if (finger.getTemplate() != null) {
                nbCollected++;
            }
            if (finger.getStatus() != Status.GOOD_SCAN
                    && finger.getStatus() != Status.RESCAN_GOOD_SCAN) {
                promptContinue = false;
            }
        }

        // Update scan button display
        Status activeStatus = activeFingers.get(currentActiveFingerNo).getStatus();
        scanButton.setText(activeStatus.getButtonTextId());
        scanButton.setTextColor(activeStatus.getButtonTextColor());
        scanButton.setBackgroundColor(activeStatus.getButtonBgColor());

        timeoutBar.setProgressBar(activeStatus);

        FingerFragment fragment = pageAdapter.getFragment(currentActiveFingerNo);
        if (fragment != null) {
            fragment.updateTextAccordingToStatus();
        }

        if (continueItem != null) {
            if (activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING) {
                continueItem.setIcon(R.drawable.ic_menu_forward_grey);
                continueItem.setEnabled(false);
            } else {
                if (nbCollected == 0) {
                    continueItem.setIcon(R.drawable.ic_menu_forward_grey);
                } else if (nbCollected > 0 && promptContinue) {
                    continueItem.setIcon(R.drawable.ic_menu_forward_green);
                } else if (nbCollected > 0) {
                    continueItem.setIcon(R.drawable.ic_menu_forward_white);
                }
                continueItem.setEnabled(nbCollected > 0);
            }
        }
    }

    private void resetUIfromError() {
        activeFingers.get(currentActiveFingerNo).setStatus(Status.NOT_COLLECTED);
        activeFingers.get(currentActiveFingerNo).setTemplate(null);
        appState.getScanner().resetUI();
        refreshDisplay();
        scanButton.setEnabled(true);
    }

    private void finishWithUnexpectedError() {
        Intent intent = new Intent(this, AlertActivity.class);
        intent.putExtra("alertType", ALERT_TYPE.UNEXPECTED_ERROR);
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST_CODE);
    }

    private void nudgeMode() {
        boolean nudge = sharedPref.getNudgeModeBool();

        if (nudge) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentActiveFingerNo < activeFingers.size()) {
                        viewPager.setScrollDuration(SLOW_SWIPE_SPEED);
                        viewPager.setCurrentItem(currentActiveFingerNo + 1);
                        viewPager.setScrollDuration(FAST_SWIPE_SPEED);
                    }
                }
            }, AUTO_SWIPE_DELAY);
        }
    }

    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {
        Log.d(this, String.format(Locale.UK, "onScannerEvent %s %s", event.name(), event.details()));
        Finger finger = activeFingers.get(currentActiveFingerNo);

        switch (event) {
            case TRIGGER_PRESSED: // Trigger pressed
                if (finger.getStatus() != Status.GOOD_SCAN) {
                    toggleContinuousCapture();
                } else if (promptContinue) {
                    if (continueItem.isEnabled()) {
                        onActionForward();
                    }
                }
                break;

            case CONTINUOUS_CAPTURE_STARTED:
                previousStatus = finger.getStatus();
                finger.setStatus(Status.COLLECTING);
                refreshDisplay();
                scanButton.setEnabled(true);
                break;

            case CONTINUOUS_CAPTURE_STOPPED: // Continuous capture stopped
                finger.setStatus(previousStatus);
                refreshDisplay();
                scanButton.setEnabled(true);
                break;

            case CONTINUOUS_CAPTURE_SUCCESS: // Image captured successfully
                timeoutBar.stopTimeoutBar();
                Arrays.fill(LEDS, Message.LED_STATE.LED_STATE_ON);
                appState.getScanner().generateTemplate();
                break;

            case GENERATE_TEMPLATE_SUCCESS: // Template generated successfully
                appState.getScanner().extractTemplate();
                break;

            case EXTRACT_TEMPLATE_SUCCESS: // Template extracted successfully
                int quality = appState.getScanner().getImageQuality();

                if (finger.getTemplate() == null || finger.getTemplate().getQualityScore() < quality) {
                    try {
                        activeFingers
                                .get(currentActiveFingerNo)
                                .setTemplate(
                                        new Fingerprint(
                                                finger.getId(),
                                                appState.getScanner().getTemplate()));
                    } catch (IllegalArgumentException ex) {
                        FirebaseCrash.report(ex);
                        resetUIfromError();
                        return;
                    }
                }

                int qualityScore1 = sharedPref.getQualityThresholdInt();

                if (quality >= qualityScore1) {
                    activeFingers.get(currentActiveFingerNo).setStatus(Status.GOOD_SCAN);
                    nudgeMode();
                } else {
                    activeFingers.get(currentActiveFingerNo).setStatus(Status.BAD_SCAN);
                }

                Vibrate.vibrate(this, 100);
                refreshDisplay();

                break;

            case EXTRACT_IMAGE_IO_ERROR:
            case CAPTURE_IMAGE_WRONG:
            case CAPTURE_IMAGE_INVALID_PARAM:
            case CAPTURE_IMAGE_LINE_DROPPED:
            case GENERATE_TEMPLATE_LOW_FEAT_NUMBER:
            case GENERATE_TEMPLATE_INVALID_TYPE:
            case GENERATE_TEMPLATE_EXTRACT_FAIL:
            case EXTRACT_IMAGE_QUALITY_NO_IMAGE: // Image quality extraction failed because there is no image available
            case EXTRACT_IMAGE_QUALITY_SDK_ERROR: // Image quality extraction failed because of an error in UN20 SDK
            case EXTRACT_IMAGE_QUALITY_FAILURE: // Image quality extraction failed for abnormal reasons, SHOULD NOT HAPPEN
            case GENERATE_TEMPLATE_NO_IMAGE: // Template generation failed because there is no image available
            case GENERATE_TEMPLATE_NO_QUALITY: // Template generation failed because there is no image quality available
            case GENERATE_TEMPLATE_SDK_ERROR: // Template generation failed because of an error in UN20 SDK
            case GENERATE_TEMPLATE_FAILURE: // Template generation failed for abnormal reasons, SHOULD NOT HAPPEN
            case EXTRACT_TEMPLATE_NO_TEMPLATE: // Template extraction failed because there is no template available
            case EXTRACT_TEMPLATE_IO_ERROR: // Template extraction failed because of an IO error
            case EXTRACT_TEMPLATE_FAILURE: // Template extraction failed for abnormal reasons, SHOULD NOT HAPPEN
                appState.getScanner().setBadCaptureUI();
                resetUIfromError();
                break;

            case CONTINUOUS_CAPTURE_ERROR:
            case CAPTURE_IMAGE_INVALID_STATE: // Image capture failed because the un20 is not awaken
                resetUIfromError();
                appState.getScanner().un20Wakeup();
                timeoutBar.cancelTimeoutBar();
                un20WakeupDialog.show();
                break;

            case UN20_WAKEUP_SUCCESS: // UN20 woken up successfully
                un20WakeupDialog.cancel();
                break;

            case CONNECTION_SCANNER_UNREACHABLE:
                break;
            case DISCONNECTION_INITIATED: // Disconnection initiated
                break;

            // success conditions
            case SEND_REQUEST_SUCCESS: // Request sent successfully
            case CONNECTION_INITIATED:
            case CONNECTION_SUCCESS: // Successfully connected to scanner
            case DISCONNECTION_SUCCESS: // Successfully disconnected from scanner
            case UPDATE_SENSOR_INFO_SUCCESS: // Sensor info was successfully updated
            case SET_UI_SUCCESS: // UI was successfully set
                break;

            case SEND_REQUEST_IO_ERROR: // Request sending failed because of an IO error
                Intent intent = new Intent(this, AlertActivity.class);
                intent.putExtra("alertType", ALERT_TYPE.DISCONNECTED);
                startActivityForResult(intent, ALERT_ACTIVITY_REQUEST_CODE);
                break;

            // error conditions
            case SCANNER_BUSY:
                break;
            case NOT_CONNECTED: // Cannot perform request because the phone is not connected to the scanner
            case NO_RESPONSE: // The scanner is not answering
            case CONNECTION_ALREADY_CONNECTED: // Connection failed because the phone is already connected/connecting/disconnecting
            case CONNECTION_BLUETOOTH_DISABLED: // Connection failed because phone's bluetooth is disabled
            case CONNECTION_SCANNER_UNBONDED: // Connection failed because the scanner is not bonded to the phone
            case CONNECTION_BAD_SCANNER_FEATURE: // Connection failed because the scanner does not support the default UUID as it should
            case CONNECTION_IO_ERROR: // Connection failed because of an IO error
            case DISCONNECTION_IO_ERROR: // Disconnection failed because of an IO error
            case UPDATE_SENSOR_INFO_FAILURE: // Updating sensor info failed for abnormal reasons, SHOULD NOT HAPPEN
            case SET_SENSOR_CONFIG_SUCCESS: // Sensor configuration was successfully set
            case SET_SENSOR_CONFIG_FAILURE: // Setting sensor configuration failed for abnormal reasons, SHOULD NOT HAPPEN
            case SET_UI_FAILURE: // Setting UI failed for abnormal reasons, SHOULD NOT HAPPEN
            case CAPTURE_IMAGE_SDK_ERROR: // Image capture failed because of an error in UN20 SDK
            case CAPTURE_IMAGE_FAILURE: // Image capture failed for abnormal reasons, SHOULD NOT HAPPEN
            case EXTRACT_IMAGE_NO_IMAGE: // Image extraction failed because there is no image available
            case EXTRACT_IMAGE_FAILURE: // Image extraction failed for abnormal reasons, SHOULD NOT HAPPEN
            case UN20_SHUTDOWN_INVALID_STATE: // UN20 shut down failed because it is already shut / waking up or down
            case UN20_SHUTDOWN_FAILURE: // UN20 shut down failed for abnormal reasons, SHOULD NOT HAPPEN
            case UN20_WAKEUP_INVALID_STATE: // UN20 wake up failed because it is already woken up / waking up or down
            case UN20_WAKEUP_FAILURE: // UN20 wake up failed for abnormal reasons, SHOULD NOT HAPPEN
            case EXTRACT_CRASH_LOG_NO_CRASHLOG: // Crash log extraction failed because there is no crash log available
            case EXTRACT_CRASH_LOG_FAILURE: // Crash log extraction failed for abnormal reasons, SHOULD NOT HAPPEN
            case SET_HARDWARE_CONFIG_INVALID_STATE: // Hardware configuration failed because UN20 is not shutdown
            case SET_HARDWARE_CONFIG_INVALID_CONFIG: // Hardware configuration failed because an invalid config was specified
            case SET_HARDWARE_CONFIG_FAILURE: // Hardware configuration failed for abnormal reasons, SHOULD NOT HAPPEN
                finishWithUnexpectedError();
                break;
        }
    }

    @Override
    public void onDataEvent(Event event) {
        Log.d(this, String.format(Locale.UK, "onDataEvent %s %s", event.name(), event.details()));
        switch (event) {
            case SYNC_INTERRUPTED:
                if (syncItem == null)
                    return;

                syncItem.setEnabled(true);
                syncItem.setTitle("Syncing Failed");
                syncItem.setIcon(R.drawable.ic_menu_sync_failed);
                break;
            case SYNC_SUCCESS:
                if (syncItem == null)
                    return;

                syncItem.setEnabled(true);
                syncItem.setTitle("Sync Complete");
                syncItem.setIcon(R.drawable.ic_menu_sync_success);
                break;
            case CONNECTED:
                appState.setConnected(true);
                if (!appState.getSignedIn())
                    appState.getData().signIn();
                updateSyncStatus();
                break;
            case DISCONNECTED:
                appState.setConnected(false);
                updateSyncStatus();
                break;
            case SIGNED_IN:
                appState.setSignedIn(true);
                updateSyncStatus();
                break;
            case SIGNED_OUT:
                appState.setSignedIn(false);
                updateSyncStatus();
                break;
        }
    }

    protected void onActionForward() {
        // Gathers the fingerprints in a list
        activeFingers.get(currentActiveFingerNo);

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();
        int nbRequiredFingerprints = 0;

        for (Finger finger : activeFingers) {
            if (finger.getStatus() == Status.GOOD_SCAN ||
                    finger.getStatus() == Status.BAD_SCAN ||
                    finger.getStatus() == Status.RESCAN_GOOD_SCAN) {
                fingerprints.add(new Fingerprint(finger.getId(), finger.getTemplate().getTemplateBytes()));

                nbRequiredFingerprints++;
            }
        }

        if (nbRequiredFingerprints < 1) {
            Toast.makeText(this, "Please scan at least 1 required finger", Toast.LENGTH_LONG).show();
        } else {
            Person person = new Person(appState.getGuid(), fingerprints);
            if (appState.getCallout() == AppState.Callout.REGISTER || appState.getCallout() == AppState.Callout.UPDATE) {
                appState.getData().savePerson(person);

                registrationResult = new Registration(appState.getGuid());
                for (Fingerprint fp : fingerprints) {
                    if (RemoteConfig.get().getBoolean(RemoteConfig.ENABLE_RETURNING_TEMPLATES))
                        registrationResult.setTemplate(fp.getFingerId(), fp.getTemplateBytes());
                    else
                        registrationResult.setTemplate(fp.getFingerId(), new byte[1]);
                }

                Intent resultData = new Intent(Constants.SIMPRINTS_REGISTER_INTENT);
                resultData.putExtra(Constants.SIMPRINTS_REGISTRATION, registrationResult);
                setResult(RESULT_OK, resultData);
                finish();
            } else {
                Intent intent = new Intent(this, MatchingActivity.class);
                intent.putExtra("Person", person);
                startActivityForResult(intent, MATCHING_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        continueItem = menu.findItem(R.id.action_forward);
        refreshDisplay();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_forward) {
            onActionForward();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_autoAdd:
                if (activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING) {
                    toggleContinuousCapture();
                }
                autoAdd();
                break;
            case R.id.nav_add:
                if (activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING) {
                    toggleContinuousCapture();
                }
                addFinger();
                break;
            case R.id.nav_help:
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
                break;
            case R.id.privacy:
                startActivityForResult(new Intent(this, PrivacyActivity.class),
                        PRIVACY_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.nav_sync:
                backgroundSync();
                return true;
            case R.id.nav_about:
                startActivityForResult(new Intent(this, AboutActivity.class),
                        ABOUT_ACTIVITY_REQUEST_CODE);
                break;
            case R.id.nav_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class),
                        SETTINGS_ACTIVITY_REQUEST_CODE);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addFinger() {
        final boolean[] checked = new boolean[fingers.length];
        String[] labels = new String[fingers.length];
        for (int i = 0; i < fingers.length; i++) {
            checked[i] = fingers[i].isActive();
            labels[i] = getString(FingerRes.get(fingers[i]).getNameId());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Add Finger(s)")
                .setMultiChoiceItems(labels, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                        Finger finger = fingers[i];
                        switch (DEFAULT_CONFIG.get(finger.getId())) {
                            case DO_NOT_COLLECT:
                                checked[i] = false;
                                ((AlertDialog) dialogInterface).getListView().setItemChecked(i, false);
                                break;
                            case OPTIONAL:
                                checked[i] = isChecked;
                                finger.setActive(isChecked);
                                break;
                            case REQUIRED:
                                checked[i] = true;
                                ((AlertDialog) dialogInterface).getListView().setItemChecked(i, true);
                                break;
                        }
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Finger currentActiveFinger = activeFingers.get(currentActiveFingerNo);
                        activeFingers.get(activeFingers.size() - 1).setLastFinger(false);
                        for (Finger finger : fingers) {
                            if (finger.isActive() && !activeFingers.contains(finger)) {
                                activeFingers.add(finger);
                            }
                            if (!finger.isActive() && activeFingers.contains(finger)) {
                                activeFingers.remove(finger);
                            }
                        }
                        Collections.sort(activeFingers);

                        if (currentActiveFinger.isActive()) {
                            currentActiveFingerNo = activeFingers.indexOf(currentActiveFinger);
                        } else {
                            currentActiveFingerNo = 0;
                        }
                        activeFingers.get(activeFingers.size() - 1).setLastFinger(true);

                        initIndicators();
                        pageAdapter.notifyDataSetChanged();
                        viewPager.setCurrentItem(currentActiveFingerNo);
                        refreshDisplay();
                    }
                });

        builder.create().show();
    }

    public void autoAdd() {
        activeFingers.get(activeFingers.size() - 1).setLastFinger(false);

        for (Finger finger : fingers) {
            if (DEFAULT_CONFIG.get(finger.getId()) != FingerConfig.DO_NOT_COLLECT && !activeFingers.contains(finger)) {
                activeFingers.add(finger);
                finger.setActive(true);
                break;
            }
        }
        Collections.sort(activeFingers);

        activeFingers.get(activeFingers.size() - 1).setLastFinger(true);

        initIndicators();
        pageAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(currentActiveFingerNo);
        refreshDisplay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY_REQUEST_CODE:
            case PRIVACY_ACTIVITY_REQUEST_CODE:
            case ABOUT_ACTIVITY_REQUEST_CODE:
                super.onActivityResult(requestCode, resultCode, data);
                break;
            default:
                setResult(resultCode, data);
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING) {
                toggleContinuousCapture();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getBaseContext().getResources().updateConfiguration(Language.selectLanguage(
                getApplicationContext()), getBaseContext().getResources().getDisplayMetrics());
        updateSyncStatus();
    }

    private void updateSyncStatus() {
        if (!appState.isConnected()) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Offline");
            syncItem.setIcon(R.drawable.ic_menu_sync_off);
        } else if (!appState.getSignedIn()) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Signed Out");
            syncItem.setIcon(R.drawable.ic_menu_sync_off);
        } else if (appState.getSignedIn()) {
            syncItem.setEnabled(true);
            syncItem.setTitle("Sync");
            syncItem.setIcon(R.drawable.ic_menu_sync_ready);
        }
    }

    private void backgroundSync() {
        DatabaseSync.sync(getApplicationContext(), appState.getAppKey(), this, appState.getUserId());

        if (syncItem != null) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Syncing...");
            syncItem.setIcon(R.drawable.ic_menu_syncing);
        }
    }

}

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
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Callout;
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
import com.simprints.libscanner.ButtonListener;
import com.simprints.libscanner.ResultListener;
import com.simprints.libscanner.SCANNER_ERROR;
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
        DatabaseEventListener {

    private final static long AUTO_SWIPE_DELAY = 500;
    private final static int FAST_SWIPE_SPEED = 100;
    private final static int SLOW_SWIPE_SPEED = 1000;

    private final static int ALERT_ACTIVITY_REQUEST_CODE = 0;
    private final static int MATCHING_ACTIVITY_REQUEST_CODE = 1;
    private final static int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    private final static int PRIVACY_ACTIVITY_REQUEST_CODE = 3;
    private final static int ABOUT_ACTIVITY_REQUEST_CODE = 4;

    private boolean buttonContinue = false;

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

    private ButtonListener scannerButton;

    private AppState appState;

    private Handler handler;

    private Finger[] fingers = new Finger[NB_OF_FINGERS];
    private List<Finger> activeFingers;
    private int currentActiveFingerNo;

    private List<ImageView> indicators;
    private Button scanButton;
    private ViewPagerCustom viewPager;
    private FingerPageAdapter pageAdapter;
    private TimeoutBar timeoutBar;

    private Registration registrationResult;
    private Status previousStatus;

    private MenuItem continueItem;
    private MenuItem syncItem;

    private ProgressDialog un20WakeupDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);

        appState = AppState.getInstance();
        appState.getData().setListener(this);

        handler = new Handler();

        fingers = new Finger[NB_OF_FINGERS];
        activeFingers = new ArrayList<>();
        currentActiveFingerNo = 0;
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
        scannerButton = new ButtonListener() {
            @Override
            public void onClick() {
                if (buttonContinue)
                    onActionForward();
                else if (activeFingers.get(currentActiveFingerNo).getStatus() != Status.GOOD_SCAN)
                    toggleContinuousCapture();
            }
        };
        appState.getScanner().registerButtonListener(scannerButton);

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
        Finger finger = activeFingers.get(currentActiveFingerNo);

        switch (activeFingers.get(currentActiveFingerNo).getStatus()) {
            case GOOD_SCAN:
                activeFingers.get(currentActiveFingerNo).setStatus(Status.RESCAN_GOOD_SCAN);
                refreshDisplay();
                break;
            case RESCAN_GOOD_SCAN:
            case BAD_SCAN:
            case NOT_COLLECTED:
                previousStatus = finger.getStatus();
                finger.setStatus(Status.COLLECTING);
                refreshDisplay();
                scanButton.setEnabled(true);
                refreshDisplay();
                startContinuousCapture();
                break;
            case COLLECTING:
                stopContinuousCapture();
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
                appState.getScanner().resetUI(new ResultListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(SCANNER_ERROR scanner_error) {

                    }
                });
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

        boolean promptContinue = true;

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

        buttonContinue = false;

        if (continueItem != null) {
            if (activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING) {
                continueItem.setIcon(R.drawable.ic_menu_forward_grey);
                continueItem.setEnabled(false);
            } else {
                if (nbCollected == 0) {
                    continueItem.setIcon(R.drawable.ic_menu_forward_grey);
                } else if (nbCollected > 0 && promptContinue) {
                    continueItem.setIcon(R.drawable.ic_menu_forward_green);
                    buttonContinue = true;
                } else if (nbCollected > 0) {
                    continueItem.setIcon(R.drawable.ic_menu_forward_white);
                }
                continueItem.setEnabled(nbCollected > 0);
            }
        }
    }

    private void resetUIFromError() {
        activeFingers.get(currentActiveFingerNo).setStatus(Status.NOT_COLLECTED);
        activeFingers.get(currentActiveFingerNo).setTemplate(null);

        appState.getScanner().resetUI(new ResultListener() {
            @Override
            public void onSuccess() {
                refreshDisplay();
                scanButton.setEnabled(true);
            }

            @Override
            public void onFailure(SCANNER_ERROR scanner_error) {
                switch (scanner_error) {
                    case BUSY:
                        resetUIFromError();
                        break;
                    default:
                        finishWithUnexpectedError();
                }
            }
        });
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
            if (appState.getCallout() == Callout.REGISTER || appState.getCallout() == Callout.UPDATE) {
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
    protected void onDestroy() {
        super.onDestroy();
        if (appState.getScanner() != null) {
            appState.getScanner().unregisterButtonListener(scannerButton);
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

    @SuppressWarnings("deprecation")
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

    private void startContinuousCapture() {
        timeoutBar.startTimeoutBar();

        appState.getScanner().startContinuousCapture(sharedPref.getQualityThresholdInt(),
                sharedPref.getTimeoutInt() * 1000, new ResultListener() {
                    @Override
                    public void onSuccess() {
                        timeoutBar.stopTimeoutBar();
                        captureSuccess();
                    }

                    @Override
                    public void onFailure(final SCANNER_ERROR scanner_error) {
                        if (scanner_error == SCANNER_ERROR.TIMEOUT)
                            forceCapture();
                        else
                            handleError(scanner_error);
                    }
                });
    }

    private void stopContinuousCapture() {
        appState.getScanner().stopContinuousCapture();
    }

    private void startUn20() {
        timeoutBar.cancelTimeoutBar();
        un20WakeupDialog.show();

        appState.getScanner().un20Wakeup(new ResultListener() {
            @Override
            public void onSuccess() {
                resetUIFromError();
                un20WakeupDialog.cancel();
            }

            @Override
            public void onFailure(SCANNER_ERROR scanner_error) {
                finishWithUnexpectedError();
            }
        });
    }

    private void forceCapture() {
        appState.getScanner().forceCapture(sharedPref.getQualityThresholdInt(), new ResultListener() {
                    @Override
                    public void onSuccess() {
                        captureSuccess();
                    }

                    @Override
                    public void onFailure(SCANNER_ERROR scanner_error) {
                        handleError(scanner_error);
                    }
                }
        );
    }

    private void captureSuccess() {
        Finger finger = activeFingers.get(currentActiveFingerNo);
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
                resetUIFromError();
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

        Vibrate.vibrate(MainActivity.this, 100);
        refreshDisplay();
    }

    private void handleError(SCANNER_ERROR scanner_error) {
        Finger finger = activeFingers.get(currentActiveFingerNo);

        switch (scanner_error) {
            case BUSY:
            case INTERRUPTED:
            case TIMEOUT:
                finger.setStatus(previousStatus);
                timeoutBar.cancelTimeoutBar();
                refreshDisplay();
                break;

            case UN20_INVALID_STATE:
                startUn20();
                break;

            case OUTDATED_SCANNER_INFO:
                appState.getScanner().updateSensorInfo(new ResultListener() {
                    @Override
                    public void onSuccess() {
                        resetUIFromError();
                    }

                    @Override
                    public void onFailure(SCANNER_ERROR scanner_error) {
                        handleError(scanner_error);
                    }
                });
                break;

            case INVALID_STATE:
            case SCANNER_UNREACHABLE:
                reconnect();
                break;

            case IO_ERROR:
            case NO_RESPONSE:
            case UNEXPECTED:
            case BLUETOOTH_DISABLED:
            case BLUETOOTH_NOT_SUPPORTED:
            case SCANNER_UNBONDED:
            case UN20_FAILURE:
            case UN20_LOW_VOLTAGE:
            case UN20_SDK_ERROR:
                finishWithUnexpectedError();
        }
    }

    private void reconnect() {
        un20WakeupDialog.show();

        if (appState.getScanner() != null) {
            appState.getScanner().connect(new ResultListener() {
                @Override
                public void onSuccess() {
                    un20WakeupDialog.cancel();
                    resetUIFromError();
                }

                @Override
                public void onFailure(SCANNER_ERROR scanner_error) {
                    finishWithUnexpectedError();
                }
            });
        } else {
            finishWithUnexpectedError();
        }
    }

}

package com.simprints.id.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.simprints.id.Application;
import com.simprints.id.R;
import com.simprints.id.activities.about.AboutActivity;
import com.simprints.id.activities.matching.MatchingActivity;
import com.simprints.id.adapters.FingerPageAdapter;
import com.simprints.id.controllers.Setup;
import com.simprints.id.controllers.SetupCallback;
import com.simprints.id.data.DataManager;
import com.simprints.id.domain.callout.CalloutAction;
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError;
import com.simprints.id.exceptions.unsafe.InvalidSyncGroupError;
import com.simprints.id.exceptions.unsafe.UnexpectedScannerError;
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError;
import com.simprints.id.fragments.FingerFragment;
import com.simprints.id.model.ALERT_TYPE;
import com.simprints.id.model.Finger;
import com.simprints.id.model.FingerRes;
import com.simprints.id.services.sync.SyncClient;
import com.simprints.id.services.sync.SyncService;
import com.simprints.id.services.sync.SyncTaskParameters;
import com.simprints.id.services.sync.SyncTaskParameters.GlobalSyncTaskParameters;
import com.simprints.id.services.sync.SyncTaskParameters.UserSyncTaskParameters;
import com.simprints.id.tools.AlertLauncher;
import com.simprints.id.tools.AppState;
import com.simprints.id.tools.FormatResult;
import com.simprints.id.tools.LanguageHelper;
import com.simprints.id.tools.Log;
import com.simprints.id.tools.RemoteConfig;
import com.simprints.id.tools.TimeHelper;
import com.simprints.id.tools.TimeoutBar;
import com.simprints.id.tools.Vibrate;
import com.simprints.id.tools.ViewPagerCustom;
import com.simprints.libcommon.FingerConfig;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.libcommon.Progress;
import com.simprints.libcommon.ScanConfig;
import com.simprints.libdata.AuthListener;
import com.simprints.libdata.ConnectionListener;
import com.simprints.libscanner.ButtonListener;
import com.simprints.libscanner.SCANNER_ERROR;
import com.simprints.libscanner.Scanner;
import com.simprints.libscanner.ScannerCallback;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.FingerIdentifier;
import com.simprints.libsimprints.Registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.reactivex.observers.DisposableObserver;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import timber.log.Timber;

import static com.simprints.id.model.Finger.NB_OF_FINGERS;
import static com.simprints.id.model.Finger.Status;
import static com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST;
import static com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private final static long AUTO_SWIPE_DELAY = 500;
    private final static int FAST_SWIPE_SPEED = 100;
    private final static int SLOW_SWIPE_SPEED = 1000;

    private final static int ALERT_ACTIVITY_REQUEST_CODE = 0;
    private final static int MATCHING_ACTIVITY_REQUEST_CODE = 1;
    private final static int SETTINGS_ACTIVITY_REQUEST_CODE = 2;
    private final static int PRIVACY_ACTIVITY_REQUEST_CODE = 3;
    private final static int ABOUT_ACTIVITY_REQUEST_CODE = 4;

    private boolean buttonContinue = false;

    private boolean rightToLeft = false;

    private static ScanConfig DEFAULT_CONFIG;

    private final ButtonListener scannerButtonListener = new ButtonListener() {
        @Override
        public void onClick() {
            if (buttonContinue)
                onActionForward();
            else if (activeFingers.get(currentActiveFingerNo).getStatus() != Status.GOOD_SCAN)
                toggleContinuousCapture();
        }
    };

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

    private AuthListener authListener = new AuthListener() {
        @Override
        public void onSignIn() {

        }

        @Override
        public void onSignOut() {
            setOfflineSyncItem();
        }

    };

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnection() {
            setReadySyncItem();
        }

        @Override
        public void onDisconnection() {
            setOfflineSyncItem();
        }
    };

    private DataManager dataManager;

    private SyncClient syncClient;

    private AlertLauncher alertLauncher;

    private TimeHelper timeHelper;

    // Singletons
    private AppState appState;
    private Setup setup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = ((Application) getApplication());
        dataManager = app.getDataManager();
        appState = app.getAppState();
        setup = app.getSetup();
        syncClient = SyncService.Companion.getClient(this);
        alertLauncher = new AlertLauncher(this);
        timeHelper = app.getTimeHelper();

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);

        dataManager.setMsSinceBootOnMainStart(timeHelper.msSinceBoot());

        handler = new Handler();

        fingers = new Finger[NB_OF_FINGERS];
        activeFingers = new ArrayList<>();
        currentActiveFingerNo = 0;
        previousStatus = Status.NOT_COLLECTED;

        indicators = new ArrayList<>();
        scanButton = findViewById(R.id.scan_button);
        viewPager = findViewById(R.id.view_pager);
        pageAdapter = new FingerPageAdapter(getSupportFragmentManager(), activeFingers);
        un20WakeupDialog = initUn20Dialog();
        registrationResult = null;
        timeoutBar = new TimeoutBar(getApplicationContext(),
                (ProgressBar) findViewById(R.id.pb_timeout),
                dataManager.getTimeoutS() * 1000);

        setFingerStatus();
        initActiveFingers();
        initBarAndDrawer();
        initIndicators();
        initScanButton();
        initViewPager();
        refreshDisplay();
    }

    @Override
    protected void onStart() {
        super.onStart();
        appState.getScanner().registerButtonListener(scannerButtonListener);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        LanguageHelper.setLanguage(this, dataManager.getLanguage());
        try {
            startListeners();
        } catch (UninitializedDataManagerError error) {
            dataManager.logError(error);
            handleUnexpectedError(error);
        }
    }

    private void startListeners() {
        dataManager.registerRemoteAuthListener(authListener);
        dataManager.registerRemoteConnectionListener(connectionListener);
        updateConnectionState();
        syncClient.startListening(newSyncObserver());
    }

    private void updateConnectionState() {
        if (dataManager.isConnected()) {
            connectionListener.onConnection();
        } else {
            connectionListener.onDisconnection();
        }
    }

    private void setFingerStatus() {
        DEFAULT_CONFIG = new ScanConfig();
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_THUMB          , FingerConfig.REQUIRED, 0, 0);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_INDEX_FINGER   , FingerConfig.REQUIRED, 1, 1);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_3RD_FINGER     , FingerConfig.OPTIONAL, 4, 2);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_4TH_FINGER     , FingerConfig.OPTIONAL, 5, 3);
        DEFAULT_CONFIG.set(FingerIdentifier.LEFT_5TH_FINGER     , FingerConfig.OPTIONAL, 6, 4);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_THUMB         , FingerConfig.OPTIONAL, 2, 5);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_INDEX_FINGER  , FingerConfig.OPTIONAL, 3, 6);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_3RD_FINGER    , FingerConfig.OPTIONAL, 7, 7);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_4TH_FINGER    , FingerConfig.OPTIONAL, 8, 8);
        DEFAULT_CONFIG.set(FingerIdentifier.RIGHT_5TH_FINGER    , FingerConfig.OPTIONAL, 9, 9);


        // We set the two defaults in the config for the first reset.
        Map<FingerIdentifier, Boolean> fingerStatus = dataManager.getFingerStatus();
        fingerStatus.put(FingerIdentifier.LEFT_THUMB, true);
        fingerStatus.put(FingerIdentifier.LEFT_INDEX_FINGER, true);
        dataManager.setFingerStatus(fingerStatus);
    }

    private void initActiveFingers() {
        FingerIdentifier[] fingerIdentifiers = FingerIdentifier.values();
        if (dataManager.getFingerStatusPersist()) {
            Map<FingerIdentifier, Boolean> fingerStatus = dataManager.getFingerStatus();
            for (int i = 0; i < NB_OF_FINGERS; i++) {
                FingerIdentifier id = fingerIdentifiers[i];
                fingers[i] = new Finger(id, fingerStatus.get(id), false, DEFAULT_CONFIG.getPriority(id), DEFAULT_CONFIG.getOrder(id));
            }
        } else {
            for (int i = 0; i < NB_OF_FINGERS; i++) {
                FingerIdentifier id = fingerIdentifiers[i];
                fingers[i] = new Finger(id, DEFAULT_CONFIG.get(id) == FingerConfig.REQUIRED, false, DEFAULT_CONFIG.getPriority(id), DEFAULT_CONFIG.getOrder(id));
            }
        }

        for (int i = 0; i < NB_OF_FINGERS; i++) {
            if (fingers[i].isActive()) {
                activeFingers.add(fingers[i]);
            }
        }
        Collections.sort(activeFingers);
        activeFingers.get(activeFingers.size() - 1).setLastFinger(true);
        Arrays.sort(fingers);
    }

    private ProgressDialog initUn20Dialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getString(R.string.reconnecting_message));
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        return dialog;
    }

    private void initBarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        syncItem = navigationView.getMenu().findItem(R.id.nav_sync);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.show();

        switch (dataManager.getCalloutAction()) {
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
            default:
                handleUnexpectedError(InvalidCalloutParameterError.Companion.forParameter("CalloutParameters"));
        }
    }

    private void initIndicators() {
        LinearLayout indicatorLayout = findViewById(R.id.indicator_layout);
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
        // If the layout is from right to left, we need to reverse the scrolling direction
        rightToLeft = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

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
                appState.getScanner().resetUI(null);
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

        if (rightToLeft) {
            viewPager.setRotationY(180);
        }
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
            if (rightToLeft && fragment.getView() != null) {
                fragment.getView().setRotationY(180);
            }
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

        appState.getScanner().resetUI(new ScannerCallback() {
            @Override
            public void onSuccess() {
                refreshDisplay();
                scanButton.setEnabled(true);
                un20WakeupDialog.dismiss();
            }

            @Override
            public void onFailure(SCANNER_ERROR scanner_error) {
                switch (scanner_error) {
                    case BUSY:
                        resetUIFromError();
                        break;
                    case INVALID_STATE:
                        reconnect();
                        break;
                    default:
                        handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "MainActivity"));
                }
            }
        });
    }


    /**
     * Start alert activity
     */
    private void launchAlert(ALERT_TYPE alertType) {
        alertLauncher.launch(alertType, ALERT_ACTIVITY_REQUEST_CODE);
    }

    private void handleUnexpectedError(Error error) {
        dataManager.logError(error);
        launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
    }

    private void nudgeMode() {
        boolean nudge = dataManager.getNudgeMode();

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


    protected void onActionForward() {
        // Gathers the fingerprints in a list
        activeFingers.get(currentActiveFingerNo);

        ArrayList<Fingerprint> fingerprints = new ArrayList<>();
        int nbRequiredFingerprints = 0;

        for (Finger finger : activeFingers) {
            if ((finger.getStatus() == Status.GOOD_SCAN ||
                    finger.getStatus() == Status.BAD_SCAN ||
                    finger.getStatus() == Status.RESCAN_GOOD_SCAN) &&
                    finger.getTemplate() != null) {
                fingerprints.add(new Fingerprint(finger.getId(), finger.getTemplate().getTemplateBytes()));

                nbRequiredFingerprints++;
            }
        }

        if (nbRequiredFingerprints < 1) {
            Toast.makeText(this, "Please scan at least 1 required finger", Toast.LENGTH_LONG).show();
        } else {
            Person person = new Person(dataManager.getPatientId(), fingerprints);
            if (dataManager.getCalloutAction() == CalloutAction.REGISTER || dataManager.getCalloutAction() == CalloutAction.UPDATE) {
                try {
                    dataManager.savePerson(person);
                } catch (UninitializedDataManagerError error) {
                    dataManager.logError(error);
                    launchAlert(ALERT_TYPE.UNEXPECTED_ERROR);
                    return;
                }

                registrationResult = new Registration(dataManager.getPatientId());
                if (RemoteConfig.get().getBoolean(RemoteConfig.ENABLE_RETURNING_TEMPLATES)) {
                    for (Fingerprint fp : fingerprints) {
                        registrationResult.setTemplate(fp.getFingerId(), fp.getTemplateBytes());
                    }
                }

                Intent resultData = new Intent(Constants.SIMPRINTS_REGISTER_INTENT);
                FormatResult.put(resultData, registrationResult, dataManager.getResultFormat());
                setResult(RESULT_OK, resultData);
                finish();
            } else {
                Intent intent = new Intent(this, MatchingActivity.class);
                intent.putExtra(IntentKeys.matchingActivityProbePersonKey, person);
                startActivityForResult(intent, MATCHING_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (activeFingers.get(currentActiveFingerNo).getStatus() == Status.COLLECTING) {
            toggleContinuousCapture();
        } else {
            setup.stop();
            startActivityForResult(new Intent(this, RefusalActivity.class), REFUSAL_ACTIVITY_REQUEST);
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
                sync();
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addFinger() {
        final boolean[] checked = new boolean[fingers.length + 1];
        String[] labels = new String[fingers.length + 1];
        for (int i = 0; i < fingers.length; i++) {
            checked[i] = fingers[i].isActive();
            labels[i] = getString(FingerRes.get(fingers[i]).getNameId());
        }
        labels[fingers.length] = getString(R.string.persistence_label);
        checked[fingers.length] = dataManager.getFingerStatusPersist();
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Add Finger(s)")
                .setMultiChoiceItems(labels, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                        if (i == fingers.length) {
                            checked[i] = isChecked;
                            dataManager.setFingerStatusPersist(isChecked);
                            return;
                        }

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
                        Map<FingerIdentifier, Boolean> fingerStatus = dataManager.getFingerStatus();

                        for (Finger finger : fingers) {
                            if (finger.isActive() && !activeFingers.contains(finger)) {
                                activeFingers.add(finger);
                                if (dataManager.getFingerStatusPersist())
                                    fingerStatus.put(finger.getId(), true);
                            }
                            if (!finger.isActive() && activeFingers.contains(finger)) {
                                activeFingers.remove(finger);
                                if (dataManager.getFingerStatusPersist())
                                    fingerStatus.put(finger.getId(), false);
                            }
                        }
                        dataManager.setFingerStatus(fingerStatus);
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
        if (!this.isFinishing()) {
            builder.create().show();
        }
    }

    public void autoAdd() {
        activeFingers.get(activeFingers.size() - 1).setLastFinger(false);

        // Construct a list of fingers sorted by priority
        Finger[] fingersSortedByPriority = new Finger[NB_OF_FINGERS];
        for (Finger finger : fingers) {
            fingersSortedByPriority[finger.getPriority()] = finger;
        }

        // Auto-add the next finger sorted by the "priority" field
        for (Finger finger : fingersSortedByPriority) {
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
                appState.getScanner().registerButtonListener(scannerButtonListener);
                super.onActivityResult(requestCode, resultCode, data);
                break;

            case REFUSAL_ACTIVITY_REQUEST:
            case ALERT_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_TRY_AGAIN) {
                    reconnect();
                } else {
                    setResult(resultCode, data);
                    finish();
                }
                break;
            default:
                setResult(resultCode, data);
                finish();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListeners();
    }

    private void stopListeners() {
        try {
            syncClient.stopListening();
            dataManager.unregisterRemoteAuthListener(authListener);
            dataManager.unregisterRemoteConnectionListener(connectionListener);
        } catch (UninitializedDataManagerError error) {
            handleUnexpectedError(error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Scanner scanner = appState.getScanner();
        if (scanner != null) {
            scanner.unregisterButtonListener(scannerButtonListener);
        }
    }


    private void sync() {
        SyncTaskParameters syncParameters;
        switch (dataManager.getSyncGroup()) {
            case GLOBAL:
                syncParameters = new GlobalSyncTaskParameters(dataManager.getAppKey());
                break;
            case USER:
                syncParameters = new UserSyncTaskParameters(dataManager.getAppKey(), dataManager.getUserId());
                break;
            default:
                handleUnexpectedError(new InvalidSyncGroupError());
                return;
        }
        syncClient.sync(syncParameters,
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        syncClient.startListening(newSyncObserver());
                        return null;
                    }
                },
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        setErrorSyncItem();
                        Toast.makeText(MainActivity.this,
                                R.string.wait_for_current_sync_to_finish,
                                Toast.LENGTH_LONG).show();
                        return null;
                    }
                });
    }

    private void setCompleteSyncItem() {
        setSyncItem(true, R.string.nav_sync_complete, R.drawable.ic_sync_success);
    }

    private void setReadySyncItem() {
        setSyncItem(true, R.string.nav_sync, R.drawable.ic_menu_sync_ready);
    }

    private void setOfflineSyncItem() {
        setSyncItem(false, R.string.not_signed_in, R.drawable.ic_menu_sync_off);
    }

    private void setSyncItem(Boolean enabled, @StringRes int title, @DrawableRes int icon) {
        setSyncItem(enabled, getString(title), icon);
    }

    private void setSyncItem(Boolean enabled, String title, @DrawableRes int icon) {
        if (syncItem != null) {
            syncItem.setEnabled(enabled);
            syncItem.setTitle(title);
            syncItem.setIcon(icon);
        }
    }

    private void setErrorSyncItem() {
        setSyncItem(true, R.string.nav_sync_failed, R.drawable.ic_sync_failed);
    }

    private void setProgressSyncItem(Progress progress) {
        if (isProgressZero(progress))
            setSyncItem(false,
                    getString(R.string.syncing_calculating),
                    R.drawable.ic_syncing);
        else
            setSyncItem(false,
                    getString(R.string.syncing_with_progress, progress.getCurrentValue(), progress.getMaxValue()),
                    R.drawable.ic_syncing);
    }

    private boolean isProgressZero(Progress progress) {
        return progress.getCurrentValue() == 0 && progress.getMaxValue() == 0;
    }

    private DisposableObserver<Progress> newSyncObserver() {
        return new DisposableObserver<Progress>() {

            @Override
            public void onNext(Progress progress) {
                Timber.d("onNext");
                setProgressSyncItem(progress);
            }

            @Override
            public void onComplete() {
                Timber.d("onComplete");
                setCompleteSyncItem();
                syncClient.stopListening();
            }

            @Override
            public void onError(Throwable throwable) {
                Timber.d("onError");
                setErrorSyncItem();
                logThrowable(throwable);
                syncClient.stopListening();
            }

            private void logThrowable(Throwable throwable) {
                if (throwable instanceof Error) {
                    dataManager.logError((Error) throwable);
                } else if (throwable instanceof RuntimeException) {
                    dataManager.logSafeException((RuntimeException) throwable);
                }
            }

        };
    }

    private void startContinuousCapture() {
        timeoutBar.startTimeoutBar();

        appState.getScanner().startContinuousCapture(dataManager.getQualityThreshold(),
                dataManager.getTimeoutS() * 1000, new ScannerCallback() {
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


    private void forceCapture() {
        appState.getScanner().forceCapture(dataManager.getQualityThreshold(), new ScannerCallback() {
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

    /**
     * For hardware version <=4, set bad scan if force capture isn't possible
     */
    private void forceCaptureNotPossible() {
        activeFingers.get(currentActiveFingerNo).setStatus(Status.BAD_SCAN);
        Vibrate.vibrate(MainActivity.this, dataManager.getVibrateMode(), 100);
        refreshDisplay();
    }

    private void cancelCaptureUI() {
        activeFingers.get(currentActiveFingerNo).setStatus(previousStatus);
        timeoutBar.cancelTimeoutBar();
        refreshDisplay();
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
                // TODO : change exceptions in libcommon
            } catch (IllegalArgumentException ex) {
                dataManager.logError(new Error("IllegalArgumentException in MainActivity.captureSuccess()"));
                resetUIFromError();
                return;
            }
        }

        int qualityScore1 = dataManager.getQualityThreshold();

        if (quality >= qualityScore1) {
            activeFingers.get(currentActiveFingerNo).setStatus(Status.GOOD_SCAN);
            nudgeMode();
        } else {
            activeFingers.get(currentActiveFingerNo).setStatus(Status.BAD_SCAN);
        }

        Vibrate.vibrate(MainActivity.this, dataManager.getVibrateMode(), 100);
        refreshDisplay();
    }

    private void handleError(SCANNER_ERROR scanner_error) {
        switch (scanner_error) {
            case BUSY:
            case INTERRUPTED:
            case TIMEOUT:
                cancelCaptureUI();
                break;

            case OUTDATED_SCANNER_INFO:
                cancelCaptureUI();
                appState.getScanner().updateSensorInfo(new ScannerCallback() {
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
            case UN20_INVALID_STATE:
                cancelCaptureUI();
                reconnect();
                break;

            case UN20_SDK_ERROR:
                forceCaptureNotPossible();
                break;

            case IO_ERROR:
            case NO_RESPONSE:
            case UNEXPECTED:
            case BLUETOOTH_DISABLED:
            case BLUETOOTH_NOT_SUPPORTED:
            case SCANNER_UNBONDED:
            case UN20_FAILURE:
            case UN20_LOW_VOLTAGE:
                cancelCaptureUI();
                handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "MainActivity"));
        }
    }

    private void reconnect() {
        appState.getScanner().unregisterButtonListener(scannerButtonListener);

        SetupCallback setupCallback = new SetupCallback() {
            @Override
            public void onSuccess() {
                Log.INSTANCE.d(MainActivity.this, "reconnect.onSuccess()");
                un20WakeupDialog.dismiss();
                appState.getScanner().registerButtonListener(scannerButtonListener);
            }

            @Override
            public void onProgress(int progress, int detailsId) {
                Log.INSTANCE.d(MainActivity.this, "reconnect.onProgress()");
            }

            @Override
            public void onError(int resultCode, Intent resultData) {
                Log.INSTANCE.d(MainActivity.this, "reconnect.onError()");
                un20WakeupDialog.dismiss();
                launchAlert(ALERT_TYPE.DISCONNECTED);
            }

            @Override
            public void onAlert(@NonNull ALERT_TYPE alertType) {
                Log.INSTANCE.d(MainActivity.this, "reconnect.onAlert()");
                un20WakeupDialog.dismiss();
                launchAlert(alertType);
            }
        };

        if (!this.isFinishing()) {
            un20WakeupDialog.show();
        }

        setup.start(this, setupCallback);
    }

}

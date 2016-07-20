package com.simprints.id.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.simprints.id.BaseApplication;
import com.simprints.id.R;
import com.simprints.id.fragments.FingerFragment;
import com.simprints.id.model.Finger;
import com.simprints.libcommon.FingerConfig;
import com.simprints.libdata.Data;
import com.simprints.libscanner.EVENT;
import com.simprints.libscanner.Scanner;

import static com.simprints.libscanner.EVENT.EXTRACT_IMAGE_QUALITY_SUCCESS;

public class MainActivity extends AppCompatActivity implements
            NavigationView.OnNavigationItemSelectedListener,
            Scanner.ScannerListener,
            Data.DataListener {

    private enum State {
        UNKNOWN,
        IDLE,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        WAITING_FOR_TRIGGER,
        CAPTURING_IMAGE,
        EXTRACTING_IMAGE_QUALITY,
        GENERATING_TEMPLATE,
        EXTRACTING_TEMPLATE,
        EXTRACTING_IMAGE
    };

    private State state = State.UNKNOWN;

    private Context context;
    private boolean isExiting = false;

    private Scanner scanner = null;
    private Button scanButton;

    private Data data = null;

    private static ViewPager viewPager;
    private static int noOfFingers = 0;
    private static int currentFingerNo = 0;
    private static FingerFragment currentFingerFragment = null;

    private static FingerFragment[] fingerFragments = new FingerFragment[10];
    private static ImageView[] fingerIndicators = new ImageView[10];
//    private static int[] fingerConfiguration = {
//            Finger.DO_NOT_COLLECT,
//            Finger.DO_NOT_COLLECT,
//            Finger.DO_NOT_COLLECT,
//            Finger.OPTIONAL,
//            Finger.REQUIRED,
//            Finger.REQUIRED,
//            Finger.OPTIONAL,
//            Finger.DO_NOT_COLLECT,
//            Finger.DO_NOT_COLLECT,
//            Finger.DO_NOT_COLLECT };
    private static FingerConfig.FingerConfiguration[] fingerConfiguration = {
        FingerConfig.FingerConfiguration.DO_NOT_COLLECT,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED,
        FingerConfig.FingerConfiguration.REQUIRED};
    private static int[] fingerStatus = new int[10];
    private static String[] fingerName = new String[10];
    private static int[] fingerGraphic = {
            R.drawable.hand_bb_r5_c1,
            R.drawable.hand_bb_r4_c1,
            R.drawable.hand_bb_r3_c1,
            R.drawable.hand_bb_r2_c1,
            R.drawable.hand_bb_r1_c1,
            R.drawable.hand_bb_l1_c1,
            R.drawable.hand_bb_l2_c1,
            R.drawable.hand_bb_l3_c1,
            R.drawable.hand_bb_l4_c1,
            R.drawable.hand_bb_l5_c1
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

        if (BaseApplication.getMode() == BaseApplication.REGISTER_SUBJECT) {
            actionBar.setTitle(R.string.register_title);
        }
        else {
            actionBar.setTitle(R.string.identify_title);
        }

        scanner = BaseApplication.getScanner();
        scanner.setScannerListener(this);

        data = BaseApplication.getData();
        data.setDataListener(this);

        setFingers();

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FingerPageAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(noOfFingers);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentFingerNo = position;
                refreshFingers();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (fingerStatus[currentFingerNo] == Finger.COLLECTING) {
                    return true;
                }
                else {
                    return false;
                }
            }
        });
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        viewPager.setPageMargin(-margin);
        viewPager.setPadding(margin, 0, margin, 0);
        viewPager.setClipToPadding(false);

        scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setText(R.string.scan_label);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fingerStatus[currentFingerNo] == Finger.GOOD_SCAN) {
                    fingerStatus[currentFingerNo] = Finger.COLLECTING;
                    scanButton.setText(R.string.cancel_button);
                    scanButton.setTextColor(Color.WHITE);
                    scanButton.setBackgroundColor(Color.BLUE);
                    scanner.startContinuousCapture();
                }
                else
                if (fingerStatus[currentFingerNo] == Finger.BAD_SCAN) {
                    fingerStatus[currentFingerNo] = Finger.COLLECTING;
                    scanButton.setText(R.string.cancel_button);
                    scanButton.setTextColor(Color.WHITE);
                    scanButton.setBackgroundColor(Color.BLUE);
                    scanner.startContinuousCapture();
                }
                else
                if (fingerStatus[currentFingerNo] == Finger.NOT_COLLECTED) {
                    fingerStatus[currentFingerNo] = Finger.COLLECTING;
                    scanButton.setText(R.string.cancel_button);
                    scanButton.setTextColor(Color.WHITE);
                    scanButton.setBackgroundColor(Color.BLUE);
                    scanner.startContinuousCapture();
                }
                else {
                    fingerStatus[currentFingerNo] = Finger.NOT_COLLECTED;
                    scanner.stopContinuousCapture();
                    scanButton.setText(R.string.scan_label);
                    scanButton.setTextColor(Color.WHITE);
                    scanButton.setBackgroundColor(Color.GRAY);
                }
            }
        });
    }

    @Override
    public void onScannerEvent(com.simprints.libscanner.EVENT event) {
        Log.w("Simprints", "ID: onScannerEvent event name = " + event.name() + " details = " + event.details());
        switch (event) {

            case CAPTURE_IMAGE_SUCCESS: // Image captured successfully
                scanner.extractImageQuality();
                break;

            case EXTRACT_IMAGE_QUALITY_SUCCESS: // Image quality extracted successfully
                Log.w("Simprints", "ID: image quality = " + scanner.getImageQuality());
                int imageQuality = scanner.getImageQuality();
                if (imageQuality >= 50) {
                    currentFingerFragment.setFingerText("<font color=green>Good Image<br/><font color=grey>Please scan next finger");
                    fingerStatus[currentFingerNo] = Finger.GOOD_SCAN;
                    scanButton.setBackgroundColor(Color.GREEN);
                    scanButton.setText("Re-scan");
                }
                else {
                    fingerStatus[currentFingerNo] = Finger.BAD_SCAN;
                    scanButton.setBackgroundColor(Color.RED);
                    scanButton.setText("Re-scan");
                }
                break;

            // info messages
            case CONNECTION_INITIATED: // Connection initiated
            case DISCONNECTION_INITIATED: // Disconnection initiated
            case TRIGGER_PRESSED: // Trigger pressed
            case CONTINUOUS_CAPTURE_STARTED: // Continous capture started
            case CONTINUOUS_CAPTURE_STOPPED: // Continous capture stopped
                break;

            // success conditions
            case SEND_REQUEST_SUCCESS: // Request sent successfully
            case PAIR_SUCCESS: // Paired successfully
            case CONNECTION_SUCCESS: // Successfully connected to scanner
            case DISCONNECTION_SUCCESS: // Successfully disconnected from scanner
            case UPDATE_SENSOR_INFO_SUCCESS: // Sensor info was successfully updated
            case SET_UI_SUCCESS: // UI was successfully set
            case EXTRACT_IMAGE_SUCCESS: // Image extracted successfully
            case EXTRACT_TEMPLATE_SUCCESS: // Template extracted successfully
            case GENERATE_TEMPLATE_SUCCESS: // Template generated successfully
            case UN20_WAKEUP_SUCCESS: // UN20 woken up successfully
            case UN20_SHUTDOWN_SUCCESS: // UN20 shut down successfully
            case EXTRACT_CRASH_LOG_SUCCESS: // Crash log extracted successfully
            case SET_HARDWARE_CONFIG_SUCCESS: // Hardware configuration was successfully set
                break;

                // error conditions
            case SCANNER_BUSY: // Cannot perform request because the scanner is busy
            case NOT_CONNECTED: // Cannot perform request because the phone is not connected to the scanner
            case NO_RESPONSE: // The scanner is not answering
            case SEND_REQUEST_IO_ERROR: // Request sending failed because of an IO error
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
            case PAIR_FAILURE: // Pairing failed for abnormal reasons, SHOULD NOT HAPPEN
            case CAPTURE_IMAGE_SDK_ERROR: // Image capture failed because of an error in UN20 SDK
            case CAPTURE_IMAGE_INVALID_STATE: // Image capture failed because the un20 is not awaken
            case CAPTURE_IMAGE_FAILURE: // Image capture failed for abnormal reasons, SHOULD NOT HAPPEN
            case EXTRACT_IMAGE_IO_ERROR: // Image extraction failed because of an IO error
            case EXTRACT_IMAGE_NO_IMAGE: // Image extraction failed because there is no image available
            case EXTRACT_IMAGE_FAILURE: // Image extraction failed for abnormal reasons, SHOULD NOT HAPPEN
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
            case UN20_SHUTDOWN_INVALID_STATE: // UN20 shut down failed because it is already shut / waking up or down
            case UN20_SHUTDOWN_FAILURE: // UN20 shut down failed for abnormal reasons, SHOULD NOT HAPPEN
            case UN20_WAKEUP_INVALID_STATE: // UN20 wake up failed because it is already woken up / waking up or down
            case UN20_WAKEUP_FAILURE: // UN20 wake up failed for abnormal reasons, SHOULD NOT HAPPEN
            case EXTRACT_CRASH_LOG_NO_CRASHLOG: // Crash log extraction failed because there is no crash log available
            case EXTRACT_CRASH_LOG_FAILURE: // Crash log extraction failed for abnormal reasons, SHOULD NOT HAPPEN
            case SET_HARDWARE_CONFIG_INVALID_STATE: // Hardware configuration failed because UN20 is not shutdown
            case SET_HARDWARE_CONFIG_INVALID_CONFIG: // Hardware configuration failed because an invalid config was specified
            case SET_HARDWARE_CONFIG_FAILURE: // Hardware configuration failed for abnormal reasons, SHOULD NOT HAPPEN
                int alertType = BaseApplication.GENERIC_FAILURE;
                String alertMessage = event.details();
                Intent intent = new Intent(this, AlertActivity.class);
                intent.putExtra("alertType", alertType);
                intent.putExtra("alertMessage", alertMessage);
                startActivity(intent);
                finish();
                break;

            default:
                break;
        }
        if (event.equals(EVENT.CAPTURE_IMAGE_SUCCESS)) {
            scanner.extractImageQuality();
        }
        else
        if (event.equals(EXTRACT_IMAGE_QUALITY_SUCCESS)) {

        }
    }

    @Override
    public void onDataEvent(com.simprints.libdata.EVENT event) {
        Log.w("Simprints", "ID: onDataEvent event name = " + event.name() + " details = " + event.details());
    }

    public void scannerBusyError() {

    }

    public void scannerNotConnectedError() {

    }

    public void scannerNotRespondingError() {

    }

    private class FingerPageAdapter extends FragmentPagerAdapter {

        public FingerPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            currentFingerFragment = fingerFragments[pos];
            return currentFingerFragment;
        }

        @Override
        public int getCount() {
            return noOfFingers;
        }
    }

    private void setFingers() {
        noOfFingers = 0;
        for (int fingerNo = 0; fingerNo < 10; fingerNo++) {
            if (fingerConfiguration[fingerNo] == FingerConfig.FingerConfiguration.REQUIRED || fingerConfiguration[fingerNo] == FingerConfig.FingerConfiguration.ADDED) {
                switch (fingerNo) {
                    case 0:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.r_5_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.r_5_finger_name);
                        break;
                    case 1:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.r_4_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.r_4_finger_name);
                        break;
                    case 2:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.r_3_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.r_3_finger_name);
                        break;
                    case 3:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.r_2_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.r_2_finger_name);
                        break;
                    case 4:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.r_1_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.r_1_finger_name);
                        break;
                    case 5:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.l_1_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.l_1_finger_name);
                        break;
                    case 6:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.l_2_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.l_2_finger_name);
                        break;
                    case 7:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.l_3_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.l_3_finger_name);
                        break;
                    case 8:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.l_4_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.l_4_finger_name);
                        break;
                    case 9:
                        fingerIndicators[noOfFingers] = (ImageView) findViewById(R.id.l_5_indicator);
                        fingerName[noOfFingers] = "<font color=gray>Please scan <br/><font color=blue>" + getString(R.string.l_5_finger_name);
                        break;
                }
                FingerFragment fingerFragment = FingerFragment.newInstance(fingerNo, fingerName[noOfFingers], fingerGraphic[noOfFingers]);
                fingerFragments[noOfFingers] = fingerFragment;
                fingerIndicators[noOfFingers].setVisibility(View.VISIBLE);
                fingerStatus[noOfFingers] = Finger.NOT_COLLECTED;
                noOfFingers += 1;
            }
        }
        refreshFingers();
    }

    private void refreshFingers() {
        for (int fingerNo = 0; fingerNo < noOfFingers; fingerNo++) {
            if (fingerNo == currentFingerNo) {
                if (fingerStatus[fingerNo] == Finger.NOT_COLLECTED) {
                    fingerIndicators[fingerNo].setImageResource(R.drawable.ic_blank_selected);
                }
                if (fingerStatus[fingerNo] == Finger.GOOD_SCAN) {
                    fingerIndicators[fingerNo].setImageResource(R.drawable.ic_ok_selected);
                }
                if (fingerStatus[fingerNo] == Finger.BAD_SCAN) {
                    fingerIndicators[fingerNo].setImageResource(R.drawable.ic_alert_selected);
                }
            }
            else {
                if (fingerStatus[fingerNo] == Finger.NOT_COLLECTED) {
                    fingerIndicators[fingerNo].setImageResource(R.drawable.ic_blank_deselected);
                }
                if (fingerStatus[fingerNo] == Finger.GOOD_SCAN) {
                    fingerIndicators[fingerNo].setImageResource(R.drawable.ic_ok_deselected);
                }
                if (fingerStatus[fingerNo] == Finger.BAD_SCAN) {
                    fingerIndicators[fingerNo].setImageResource(R.drawable.ic_alert_deselected);
                }
            }
        }
    }

    protected void onActionForward() {
        if (BaseApplication.getMode() == BaseApplication.REGISTER_SUBJECT) {
            //TODO: return registration class from libsimprints
            finish();
        }
        else {
            Intent intent = new Intent(this, MatchingActivity.class);
            startActivity(intent);
            finish();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_add) {
            addFinger();
        }
        if (id == R.id.nav_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_tutorial) {
            Intent intent = new Intent(this, TutorialActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_troubleshoot) {
            Intent intent = new Intent(this, TroubleshootActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addFinger() {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add);
        dialog.setTitle("Add Finger(s)");

        Button r5Button = (Button) dialog.findViewById(R.id.r_5_button);
        if (fingerConfiguration[0] == FingerConfig.FingerConfiguration.OPTIONAL) {
            r5Button.setVisibility(View.VISIBLE);
            r5Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fingerConfiguration[0] = FingerConfig.FingerConfiguration.ADDED;
                    setFingers();
                    dialog.dismiss();
                }
            });
        }
        else {
            r5Button.setVisibility(View.GONE);
        }

        Button r4Button = (Button) dialog.findViewById(R.id.r_4_button);
        if (fingerConfiguration[1] == FingerConfig.FingerConfiguration.OPTIONAL) {
            r4Button.setVisibility(View.VISIBLE);
            r4Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fingerConfiguration[1] = FingerConfig.FingerConfiguration.ADDED;
                    setFingers();
                    dialog.dismiss();
                }
            });
        }
        else {
            r4Button.setVisibility(View.GONE);
        }

        Button r3Button = (Button) dialog.findViewById(R.id.r_3_button);
        if (fingerConfiguration[2] == FingerConfig.FingerConfiguration.OPTIONAL) {
            r3Button.setVisibility(View.VISIBLE);
            r3Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fingerConfiguration[2] = FingerConfig.FingerConfiguration.ADDED;
                    setFingers();
                    dialog.dismiss();
                }
            });
        }
        else {
            r3Button.setVisibility(View.GONE);
        }

        Button l3Button = (Button) dialog.findViewById(R.id.l_3_button);
        if (fingerConfiguration[6] == FingerConfig.FingerConfiguration.OPTIONAL) {
            l3Button.setVisibility(View.VISIBLE);
            l3Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fingerConfiguration[6] = FingerConfig.FingerConfiguration.ADDED;
                    setFingers();
                    dialog.dismiss();
                }
            });
        }
        else {
            l3Button.setVisibility(View.GONE);
        }
        dialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                isExiting = true;
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isExiting == true) {
            Scanner scanner = BaseApplication.getScanner();
            if (scanner != null) {
                scanner.disconnect();
            }
        }
    }
}

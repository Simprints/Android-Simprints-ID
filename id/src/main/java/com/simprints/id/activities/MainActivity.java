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
import com.simprints.libscanner.Scanner;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context;
    private boolean isExiting = false;

    private Button scanButton;

    private static ViewPager viewPager;
    private static int noOfFingers = 0;
    private static int currentFingerNo = 0;

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

        scanButton = (Button) findViewById(R.id.scan_button);
        scanButton.setText(R.string.scan_label);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fingerStatus[currentFingerNo] != Finger.COLLECTING) {
                    fingerStatus[currentFingerNo] = Finger.COLLECTING;
                    scanButton.setText(R.string.cancel_button);
                    scanButton.setTextColor(Color.WHITE);
                    scanButton.setBackgroundColor(Color.RED);
                }
                else {
                    fingerStatus[currentFingerNo] = Finger.NOT_COLLECTED;
                    scanButton.setText(R.string.scan_label);
                    scanButton.setTextColor(Color.WHITE);
                    scanButton.setBackgroundColor(Color.BLUE);
                }
            }
        });
    }

    private class FingerPageAdapter extends FragmentPagerAdapter {

        public FingerPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            FingerFragment fingerFragment = fingerFragments[pos];
            return fingerFragment;
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

package com.simprints.id.fragments;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.R;

public class FingerFragment extends Fragment {

    private int fingerNo;
    private String fingerName;
    private String title;
    private String text;
    private TextView titleTextView;
    private TextView fingerTextView;
    private ImageView fingerImageView;
    private ProgressBar fingerProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finger, container, false);
        fingerNo = getArguments().getInt("fingerNo");
        fingerName = getArguments().getString("fingerName)");
        final String title = getArguments().getString("title");
        final String text = getArguments().getString("text");
        titleTextView = (TextView) view.findViewById(R.id.title);
        fingerTextView = (TextView) view.findViewById(R.id.finger_text);
        fingerImageView = (ImageView) view.findViewById(R.id.finger_image);
        fingerProgressBar = (ProgressBar) view.findViewById(R.id.finger_progress_bar);
        titleTextView.setText(getArguments().getString("title"));
        fingerTextView.setText(fingerName);
        return view;
    }

    public static FingerFragment newInstance(int fingerNo, String fingerName) {
        FingerFragment fingerFragment = new FingerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("fingerNo", fingerNo);
        bundle.putString("fingerName", fingerName);
        fingerFragment.setArguments(bundle);
        return fingerFragment;
    }

    public int getFingerNo() {
        return fingerNo;
    }

    public void showProgressBar(int visibility) {
        fingerProgressBar.setVisibility(visibility);
    }

    public void showFingerText(int visibility) {
        fingerTextView.setVisibility(visibility);
    }

    public void setFingerText(String fingerText) {
        fingerTextView.setText(fingerText);
    }

    public void showFingerImage(int visibility) {
        fingerImageView.setVisibility(visibility);
    }

    public void setFingerColor(int color) {
        int red   = (color & 0xFF0000) / 0xFFFF;
        int green = (color & 0xFF00) / 0xFF;
        int blue  = (color & 0xFF);
        float[] matrix = { 0, 0, 0, 0, red,
                           0, 0, 0, 0, green,
                           0, 0, 0, 0, blue,
                           0, 0, 0, 1, 0};
        ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
        fingerImageView.setColorFilter(colorFilter);
    }

    public void clearFingerColor() {
        fingerImageView.setColorFilter(null);
    }

    public void setFingerImage(int fingerImage) {
        fingerImageView.setImageResource(fingerImage);
    }
}
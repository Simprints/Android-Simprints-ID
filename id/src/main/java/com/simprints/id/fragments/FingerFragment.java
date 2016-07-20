package com.simprints.id.fragments;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simprints.id.R;

public class FingerFragment extends Fragment {

    protected View view;
    private int fingerNo;
    private String fingerName;
    private int fingerImage;
    private TextView fingerTextView;
    private ImageView fingerImageView;
    private ProgressBar fingerProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_finger, container, false);
        fingerNo = getArguments().getInt("fingerNo");
        fingerName = getArguments().getString("fingerName");
        fingerImage = getArguments().getInt("fingerImage");
        fingerTextView = (TextView) view.findViewById(R.id.finger_text);
        fingerImageView = (ImageView) view.findViewById(R.id.finger_image);
        fingerProgressBar = (ProgressBar) view.findViewById(R.id.finger_progress_bar);
        fingerTextView.setText(Html.fromHtml(fingerName));
        fingerImageView.setImageResource(fingerImage);
        fingerImageView.setVisibility(View.VISIBLE);
        return view;
    }

    public static FingerFragment newInstance(int fingerNo, String fingerName, int fingerImage) {
        FingerFragment fingerFragment = new FingerFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("fingerNo", fingerNo);
        bundle.putString("fingerName", fingerName);
        bundle.putInt("fingerImage", fingerImage);
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
        fingerTextView = (TextView) view.findViewById(R.id.finger_text);
        Log.w("Simprints", "ID: set fingerText to " + fingerText);
        fingerTextView.setText(Html.fromHtml(fingerText));
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
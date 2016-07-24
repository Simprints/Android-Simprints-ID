package com.simprints.id.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.activities.MainActivity;
import com.simprints.id.model.Finger;
import com.simprints.id.model.FingerRes;

public class FingerFragment extends Fragment {

    private TextView fingerTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finger, container, false);
        fingerTextView = (TextView) view.findViewById(R.id.finger_text);

        Finger finger = (Finger) getArguments().get("finger");
        assert finger != null;
        MainActivity.log(String.format("Creating new finger fragment view for finger %s", finger.getId().name()));

        ImageView fingerImageView = (ImageView) view.findViewById(R.id.finger_image);
        fingerImageView.setImageResource(FingerRes.get(finger).getDrawableId());
        fingerImageView.setVisibility(View.VISIBLE);

        updateTextAccordingToStatus();


        return view;
    }

    public void updateTextAccordingToStatus() {
        Finger finger = (Finger) getArguments().get("finger");
        assert finger != null;

        FingerRes fingerRes = FingerRes.get(finger);
        switch (finger.getStatus()) {
            case NOT_COLLECTED:
            case COLLECTING:
                setText(String.format(getString(R.string.please_scan),
                        getString(fingerRes.getNameId())));
                break;
            case GOOD_SCAN:
                setText(getString(R.string.good_scan_message));
                break;
            case BAD_SCAN:
                setText(String.format(getString(R.string.poor_scan_message),
                        getString(fingerRes.getNameId())));
                break;
        }
    }

    public static FingerFragment newInstance(Finger finger) {
        MainActivity.log(String.format("Creating new instance of finger fragment for finger %s", finger.getId().name()));
        FingerFragment fingerFragment = new FingerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("finger", finger);
        fingerFragment.setArguments(bundle);
        return fingerFragment;
    }

    private void setText(String fingerText) {
        Log.d("Simprints", String.format("ID: set fingerText to %s", fingerText));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            fingerTextView.setText(Html.fromHtml(fingerText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            //noinspection deprecation
            fingerTextView.setText(Html.fromHtml(fingerText));
        }
    }

    public Finger getFinger() {
        Finger finger = getArguments().getParcelable("finger");
        assert finger != null;
        return finger;
    }
}
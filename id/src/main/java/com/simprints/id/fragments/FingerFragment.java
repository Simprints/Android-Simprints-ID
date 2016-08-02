package com.simprints.id.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.id.R;
import com.simprints.id.model.Finger;
import com.simprints.id.model.FingerRes;

@SuppressWarnings("unused")
public class FingerFragment extends Fragment {

    private final static String FINGER_ARG = "finger";
    private TextView resultText;
    private TextView fingerNumber;
    private TextView directionText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finger, container, false);
        resultText = (TextView) view.findViewById(R.id.finger_result_text);
        fingerNumber = (TextView) view.findViewById(R.id.finger_number_text);
        directionText = (TextView) view.findViewById(R.id.finger_direction_text);

        Finger finger = (Finger) getArguments().get(FINGER_ARG);
        assert finger != null;

        FingerRes.setFingerRes(4);

        ImageView fingerImageView = (ImageView) view.findViewById(R.id.finger_image);
        fingerImageView.setImageResource(FingerRes.get(finger).getDrawableId());
        fingerImageView.setVisibility(View.VISIBLE);

        updateTextAccordingToStatus();

        return view;
    }

    public void updateTextAccordingToStatus() {
        Finger finger = (Finger) getArguments().get(FINGER_ARG);
        assert finger != null;

        FingerRes fingerRes = FingerRes.get(finger);

        resultText.setText(finger.getStatus().getTextResult());
        resultText.setTextColor(finger.getStatus().getTextResultColor());
        fingerNumber.setText(fingerRes.getNameId());
        fingerNumber.setTextColor(Color.BLUE);
        if(finger.isLastFinger() && (finger.getStatus() == Finger.Status.GOOD_SCAN
                || finger.getStatus() == Finger.Status.RESCAN_GOOD_SCAN)) {
            directionText.setText(R.string.last_scan_message);
        }else{
            directionText.setText(finger.getStatus().getTextDirection());
        }
        directionText.setTextColor(finger.getStatus().getTextDirectionColor());
    }

    public static FingerFragment newInstance(Finger finger) {
        FingerFragment fingerFragment = new FingerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(FINGER_ARG, finger);
        fingerFragment.setArguments(bundle);
        return fingerFragment;
    }

    public Finger getFinger() {
        Finger finger = getArguments().getParcelable(FINGER_ARG);
        assert finger != null;
        return finger;
    }
}
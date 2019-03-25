package com.simprints.fingerprint.activities.collect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.fingerprint.R;
import com.simprints.fingerprint.activities.collect.models.Finger;
import com.simprints.fingerprint.activities.collect.models.FingerRes;
import com.simprints.fingerprint.activities.collect.models.FingerStatus;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FingerFragment extends Fragment {

    private final static String FINGER_ARG = "finger";
    private TextView resultText;
    private TextView fingerNumber;
    private TextView directionText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finger, container, false);
        resultText = view.findViewById(R.id.finger_result_text);
        fingerNumber = view.findViewById(R.id.finger_number_text);
        directionText = view.findViewById(R.id.finger_direction_text);

        Finger finger = (Finger) getArguments().get(FINGER_ARG);
        assert finger != null;

        FingerRes.setFingerRes();

        ImageView fingerImageView = view.findViewById(R.id.finger_image);
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
        resultText.setTextColor(getResources().getColor(finger.getStatus().getTextResultColorRes()));
        fingerNumber.setText(getString(fingerRes.getNameId()));
        fingerNumber.setTextColor(getResources().getColor(R.color.simprints_blue));
        if (finger.isLastFinger() && (finger.getStatus() == FingerStatus.GOOD_SCAN
            || finger.getStatus() == FingerStatus.RESCAN_GOOD_SCAN)) {
            directionText.setText(R.string.empty);
        } else {
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

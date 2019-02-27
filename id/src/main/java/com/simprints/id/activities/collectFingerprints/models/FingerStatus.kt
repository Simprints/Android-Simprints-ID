package com.simprints.id.activities.collectFingerprints.models

import android.graphics.Color
import com.simprints.id.R

enum class FingerStatus(private val dotSelectedDrawableId: Int,
                        private val dotDeselectedDrawableId: Int,
                        val buttonTextId: Int,
                        val buttonBgColorRes: Int,
                        val textResult: Int,
                        val textResultColorRes: Int,
                        val textDirection: Int,
                        val buttonTextColor: Int = Color.WHITE,
                        val textDirectionColor: Int = Color.GRAY) {

    NOT_COLLECTED(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
        R.string.scan_label, R.color.simprints_grey, R.string.empty, android.R.color.white,
        R.string.please_scan),
    COLLECTING(R.drawable.ic_blank_selected, R.drawable.ic_blank_deselected,
        R.string.cancel_button, R.color.simprints_blue,
        R.string.empty, android.R.color.white,
        R.string.scanning),
    GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
        R.string.good_scan_message, R.color.simprints_green,
        R.string.good_scan_message, R.color.simprints_green,
        R.string.good_scan_direction),
    RESCAN_GOOD_SCAN(R.drawable.ic_ok_selected, R.drawable.ic_ok_deselected,
        R.string.rescan_label_question, R.color.simprints_green,
        R.string.good_scan_message, R.color.simprints_green,
        R.string.good_scan_direction),
    BAD_SCAN(R.drawable.ic_alert_selected, R.drawable.ic_alert_deselected,
        R.string.rescan_label, R.color.simprints_red,
        R.string.poor_scan_message, R.color.simprints_red,
        R.string.poor_scan_direction),
    NO_FINGER_DETECTED(R.drawable.ic_alert_selected, R.drawable.ic_alert_deselected,
        R.string.rescan_label, R.color.simprints_red,
        R.string.no_finger_detected_message, R.color.simprints_red,
        R.string.poor_scan_direction),
    FINGER_SKIPPED(R.drawable.ic_alert_selected, R.drawable.ic_alert_deselected,
        R.string.rescan_label, R.color.simprints_red,
        R.string.finger_skipped_message, R.color.simprints_red,
        R.string.good_scan_direction);

    fun getDrawableId(selected: Boolean): Int {
        return if (selected) dotSelectedDrawableId else dotDeselectedDrawableId
    }
}

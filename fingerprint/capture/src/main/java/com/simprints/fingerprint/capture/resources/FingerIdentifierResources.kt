package com.simprints.fingerprint.capture.resources

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
@DrawableRes
internal fun IFingerIdentifier.fingerDrawable(): Int = when (this) {
    IFingerIdentifier.RIGHT_5TH_FINGER -> R.drawable.hand_bb_r5_c4
    IFingerIdentifier.RIGHT_4TH_FINGER -> R.drawable.hand_bb_r4_c4
    IFingerIdentifier.RIGHT_3RD_FINGER -> R.drawable.hand_bb_r3_c4
    IFingerIdentifier.RIGHT_INDEX_FINGER -> R.drawable.hand_bb_r2_c4
    IFingerIdentifier.RIGHT_THUMB -> R.drawable.hand_bb_r1_c4
    IFingerIdentifier.LEFT_THUMB -> R.drawable.hand_bb_l1_c4
    IFingerIdentifier.LEFT_INDEX_FINGER -> R.drawable.hand_bb_l2_c4
    IFingerIdentifier.LEFT_3RD_FINGER -> R.drawable.hand_bb_l3_c4
    IFingerIdentifier.LEFT_4TH_FINGER -> R.drawable.hand_bb_l4_c4
    IFingerIdentifier.LEFT_5TH_FINGER -> R.drawable.hand_bb_l5_c4
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun IFingerIdentifier.nameTextId(): Int = when (this) {
    IFingerIdentifier.RIGHT_5TH_FINGER -> IDR.string.fingerprint_capture_finger_r_5
    IFingerIdentifier.RIGHT_4TH_FINGER -> IDR.string.fingerprint_capture_finger_r_4
    IFingerIdentifier.RIGHT_3RD_FINGER -> IDR.string.fingerprint_capture_finger_r_3
    IFingerIdentifier.RIGHT_INDEX_FINGER -> IDR.string.fingerprint_capture_finger_r_2
    IFingerIdentifier.RIGHT_THUMB -> IDR.string.fingerprint_capture_finger_r_1
    IFingerIdentifier.LEFT_THUMB -> IDR.string.fingerprint_capture_finger_l_1
    IFingerIdentifier.LEFT_INDEX_FINGER -> IDR.string.fingerprint_capture_finger_l_2
    IFingerIdentifier.LEFT_3RD_FINGER -> IDR.string.fingerprint_capture_finger_l_3
    IFingerIdentifier.LEFT_4TH_FINGER -> IDR.string.fingerprint_capture_finger_l_4
    IFingerIdentifier.LEFT_5TH_FINGER -> IDR.string.fingerprint_capture_finger_l_5
}

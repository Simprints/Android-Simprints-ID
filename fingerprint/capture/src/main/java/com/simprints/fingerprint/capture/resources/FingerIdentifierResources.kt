package com.simprints.fingerprint.capture.resources

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.fingerprint.capture.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
@DrawableRes
internal fun SampleIdentifier.fingerDrawable(): Int = when (this) {
    SampleIdentifier.RIGHT_5TH_FINGER -> R.drawable.hand_bb_r5_c4
    SampleIdentifier.RIGHT_4TH_FINGER -> R.drawable.hand_bb_r4_c4
    SampleIdentifier.RIGHT_3RD_FINGER -> R.drawable.hand_bb_r3_c4
    SampleIdentifier.RIGHT_INDEX_FINGER -> R.drawable.hand_bb_r2_c4
    SampleIdentifier.RIGHT_THUMB -> R.drawable.hand_bb_r1_c4
    SampleIdentifier.LEFT_THUMB -> R.drawable.hand_bb_l1_c4
    SampleIdentifier.LEFT_INDEX_FINGER -> R.drawable.hand_bb_l2_c4
    SampleIdentifier.LEFT_3RD_FINGER -> R.drawable.hand_bb_l3_c4
    SampleIdentifier.LEFT_4TH_FINGER -> R.drawable.hand_bb_l4_c4
    SampleIdentifier.LEFT_5TH_FINGER -> R.drawable.hand_bb_l5_c4
    SampleIdentifier.NONE -> throw IllegalArgumentException("Must be a finger sample identifier")
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun SampleIdentifier.nameTextId(): Int = when (this) {
    SampleIdentifier.RIGHT_5TH_FINGER -> IDR.string.fingerprint_capture_finger_r_5
    SampleIdentifier.RIGHT_4TH_FINGER -> IDR.string.fingerprint_capture_finger_r_4
    SampleIdentifier.RIGHT_3RD_FINGER -> IDR.string.fingerprint_capture_finger_r_3
    SampleIdentifier.RIGHT_INDEX_FINGER -> IDR.string.fingerprint_capture_finger_r_2
    SampleIdentifier.RIGHT_THUMB -> IDR.string.fingerprint_capture_finger_r_1
    SampleIdentifier.LEFT_THUMB -> IDR.string.fingerprint_capture_finger_l_1
    SampleIdentifier.LEFT_INDEX_FINGER -> IDR.string.fingerprint_capture_finger_l_2
    SampleIdentifier.LEFT_3RD_FINGER -> IDR.string.fingerprint_capture_finger_l_3
    SampleIdentifier.LEFT_4TH_FINGER -> IDR.string.fingerprint_capture_finger_l_4
    SampleIdentifier.LEFT_5TH_FINGER -> IDR.string.fingerprint_capture_finger_l_5
    SampleIdentifier.NONE -> throw IllegalArgumentException("Must be a finger sample identifier")
}

package com.simprints.fingerprint.capture.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.infra.resources.R as IDR
import com.simprints.fingerprint.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

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
    IFingerIdentifier.RIGHT_5TH_FINGER -> IDR.string.r_5_finger_name
    IFingerIdentifier.RIGHT_4TH_FINGER -> IDR.string.r_4_finger_name
    IFingerIdentifier.RIGHT_3RD_FINGER -> IDR.string.r_3_finger_name
    IFingerIdentifier.RIGHT_INDEX_FINGER -> IDR.string.r_2_finger_name
    IFingerIdentifier.RIGHT_THUMB -> IDR.string.r_1_finger_name
    IFingerIdentifier.LEFT_THUMB -> IDR.string.l_1_finger_name
    IFingerIdentifier.LEFT_INDEX_FINGER -> IDR.string.l_2_finger_name
    IFingerIdentifier.LEFT_3RD_FINGER -> IDR.string.l_3_finger_name
    IFingerIdentifier.LEFT_4TH_FINGER -> IDR.string.l_4_finger_name
    IFingerIdentifier.LEFT_5TH_FINGER -> IDR.string.l_5_finger_name
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@Suppress("unused")
@ColorRes
internal fun IFingerIdentifier.nameTextColour(): Int = IDR.color.simprints_blue

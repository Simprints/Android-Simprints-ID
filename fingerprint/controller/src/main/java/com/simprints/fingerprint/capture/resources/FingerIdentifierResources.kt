package com.simprints.fingerprint.capture.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.infra.resources.R as IDR
import com.simprints.fingerprint.R
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
@DrawableRes
internal fun FingerIdentifier.fingerDrawable(): Int =
    when (this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> R.drawable.hand_bb_r5_c4
        FingerIdentifier.RIGHT_4TH_FINGER -> R.drawable.hand_bb_r4_c4
        FingerIdentifier.RIGHT_3RD_FINGER -> R.drawable.hand_bb_r3_c4
        FingerIdentifier.RIGHT_INDEX_FINGER -> R.drawable.hand_bb_r2_c4
        FingerIdentifier.RIGHT_THUMB -> R.drawable.hand_bb_r1_c4
        FingerIdentifier.LEFT_THUMB -> R.drawable.hand_bb_l1_c4
        FingerIdentifier.LEFT_INDEX_FINGER -> R.drawable.hand_bb_l2_c4
        FingerIdentifier.LEFT_3RD_FINGER -> R.drawable.hand_bb_l3_c4
        FingerIdentifier.LEFT_4TH_FINGER -> R.drawable.hand_bb_l4_c4
        FingerIdentifier.LEFT_5TH_FINGER -> R.drawable.hand_bb_l5_c4
    }

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun FingerIdentifier.nameTextId(): Int =
    when (this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> IDR.string.r_5_finger_name
        FingerIdentifier.RIGHT_4TH_FINGER -> IDR.string.r_4_finger_name
        FingerIdentifier.RIGHT_3RD_FINGER -> IDR.string.r_3_finger_name
        FingerIdentifier.RIGHT_INDEX_FINGER -> IDR.string.r_2_finger_name
        FingerIdentifier.RIGHT_THUMB -> IDR.string.r_1_finger_name
        FingerIdentifier.LEFT_THUMB -> IDR.string.l_1_finger_name
        FingerIdentifier.LEFT_INDEX_FINGER -> IDR.string.l_2_finger_name
        FingerIdentifier.LEFT_3RD_FINGER -> IDR.string.l_3_finger_name
        FingerIdentifier.LEFT_4TH_FINGER -> IDR.string.l_4_finger_name
        FingerIdentifier.LEFT_5TH_FINGER -> IDR.string.l_5_finger_name
    }

@ExcludedFromGeneratedTestCoverageReports("UI code")
@Suppress("unused")
@ColorRes
internal fun FingerIdentifier.nameTextColour(): Int = IDR.color.simprints_blue

package com.simprints.fingerprint.activities.collect.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier

@DrawableRes
fun FingerIdentifier.fingerDrawable(): Int =
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

@StringRes
fun FingerIdentifier.nameTextId(): Int =
    when (this) {
        FingerIdentifier.RIGHT_5TH_FINGER -> R.string.r_5_finger_name
        FingerIdentifier.RIGHT_4TH_FINGER -> R.string.r_4_finger_name
        FingerIdentifier.RIGHT_3RD_FINGER -> R.string.r_3_finger_name
        FingerIdentifier.RIGHT_INDEX_FINGER -> R.string.r_2_finger_name
        FingerIdentifier.RIGHT_THUMB -> R.string.r_1_finger_name
        FingerIdentifier.LEFT_THUMB -> R.string.l_1_finger_name
        FingerIdentifier.LEFT_INDEX_FINGER -> R.string.l_2_finger_name
        FingerIdentifier.LEFT_3RD_FINGER -> R.string.l_3_finger_name
        FingerIdentifier.LEFT_4TH_FINGER -> R.string.l_4_finger_name
        FingerIdentifier.LEFT_5TH_FINGER -> R.string.l_5_finger_name
    }

@Suppress("unused")
@ColorRes
fun FingerIdentifier.nameTextColour(): Int =
    R.color.simprints_blue

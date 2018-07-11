package com.simprints.id.domain;

import com.simprints.id.R;

public class FingerRes {

    private static FingerRes[] fingerRes;


    private int drawableId;
    private int nameId;

    private FingerRes(int drawableId, int nameId) {
        this.drawableId = drawableId;
        this.nameId = nameId;
    }

    public static FingerRes get(Finger finger) {
        return fingerRes[finger.getId().ordinal()];
    }

    public static void setFingerRes(){

        fingerRes = new FingerRes[]{
            new FingerRes(R.drawable.hand_bb_r5_c4, R.string.r_5_finger_name),
            new FingerRes(R.drawable.hand_bb_r4_c4, R.string.r_4_finger_name),
            new FingerRes(R.drawable.hand_bb_r3_c4, R.string.r_3_finger_name),
            new FingerRes(R.drawable.hand_bb_r2_c4, R.string.r_2_finger_name),
            new FingerRes(R.drawable.hand_bb_r1_c4, R.string.r_1_finger_name),
            new FingerRes(R.drawable.hand_bb_l1_c4, R.string.l_1_finger_name),
            new FingerRes(R.drawable.hand_bb_l2_c4, R.string.l_2_finger_name),
            new FingerRes(R.drawable.hand_bb_l3_c4, R.string.l_3_finger_name),
            new FingerRes(R.drawable.hand_bb_l4_c4, R.string.l_4_finger_name),
            new FingerRes(R.drawable.hand_bb_l5_c4, R.string.l_5_finger_name),
        };
    }

    public int getDrawableId() {
        return drawableId;
    }

    public int getNameId() {
        return nameId;
    }
}

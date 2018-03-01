package com.simprints.id.model;

import com.simprints.id.R;

public class FingerRes {

    private static FingerRes[] fingerRes;

    static {
        fingerRes = new FingerRes[]{
                new FingerRes(R.drawable.hand_bb_r5_c1, R.string.r_5_finger_name),
                new FingerRes(R.drawable.hand_bb_r4_c1, R.string.r_4_finger_name),
                new FingerRes(R.drawable.hand_bb_r3_c1, R.string.r_3_finger_name),
                new FingerRes(R.drawable.hand_bb_r2_c1, R.string.r_2_finger_name),
                new FingerRes(R.drawable.hand_bb_r1_c1, R.string.r_1_finger_name),
                new FingerRes(R.drawable.hand_bb_l1_c1, R.string.l_1_finger_name),
                new FingerRes(R.drawable.hand_bb_l2_c1, R.string.l_2_finger_name),
                new FingerRes(R.drawable.hand_bb_l3_c1, R.string.l_3_finger_name),
                new FingerRes(R.drawable.hand_bb_l4_c1, R.string.l_4_finger_name),
                new FingerRes(R.drawable.hand_bb_l5_c1, R.string.l_5_finger_name),
        };
    }

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
        switch (4) {
            case 2:
                fingerRes = new FingerRes[]{
                        new FingerRes(R.drawable.hand_bb_r5_c2, R.string.r_5_finger_name),
                        new FingerRes(R.drawable.hand_bb_r4_c2, R.string.r_4_finger_name),
                        new FingerRes(R.drawable.hand_bb_r3_c2, R.string.r_3_finger_name),
                        new FingerRes(R.drawable.hand_bb_r2_c2, R.string.r_2_finger_name),
                        new FingerRes(R.drawable.hand_bb_r1_c2, R.string.r_1_finger_name),
                        new FingerRes(R.drawable.hand_bb_l1_c2, R.string.l_1_finger_name),
                        new FingerRes(R.drawable.hand_bb_l2_c2, R.string.l_2_finger_name),
                        new FingerRes(R.drawable.hand_bb_l3_c2, R.string.l_3_finger_name),
                        new FingerRes(R.drawable.hand_bb_l4_c2, R.string.l_4_finger_name),
                        new FingerRes(R.drawable.hand_bb_l5_c2, R.string.l_5_finger_name),
                };
                break;
            case 3:
                fingerRes = new FingerRes[]{
                        new FingerRes(R.drawable.hand_bb_r5_c3, R.string.r_5_finger_name),
                        new FingerRes(R.drawable.hand_bb_r4_c3, R.string.r_4_finger_name),
                        new FingerRes(R.drawable.hand_bb_r3_c3, R.string.r_3_finger_name),
                        new FingerRes(R.drawable.hand_bb_r2_c3, R.string.r_2_finger_name),
                        new FingerRes(R.drawable.hand_bb_r1_c3, R.string.r_1_finger_name),
                        new FingerRes(R.drawable.hand_bb_l1_c3, R.string.l_1_finger_name),
                        new FingerRes(R.drawable.hand_bb_l2_c3, R.string.l_2_finger_name),
                        new FingerRes(R.drawable.hand_bb_l3_c3, R.string.l_3_finger_name),
                        new FingerRes(R.drawable.hand_bb_l4_c3, R.string.l_4_finger_name),
                        new FingerRes(R.drawable.hand_bb_l5_c3, R.string.l_5_finger_name),
                };
                break;
            case 4:
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
                break;
            case 5:
                fingerRes = new FingerRes[]{
                        new FingerRes(R.drawable.hand_bb_r5_c5, R.string.r_5_finger_name),
                        new FingerRes(R.drawable.hand_bb_r4_c5, R.string.r_4_finger_name),
                        new FingerRes(R.drawable.hand_bb_r3_c5, R.string.r_3_finger_name),
                        new FingerRes(R.drawable.hand_bb_r2_c5, R.string.r_2_finger_name),
                        new FingerRes(R.drawable.hand_bb_r1_c5, R.string.r_1_finger_name),
                        new FingerRes(R.drawable.hand_bb_l1_c5, R.string.l_1_finger_name),
                        new FingerRes(R.drawable.hand_bb_l2_c5, R.string.l_2_finger_name),
                        new FingerRes(R.drawable.hand_bb_l3_c5, R.string.l_3_finger_name),
                        new FingerRes(R.drawable.hand_bb_l4_c5, R.string.l_4_finger_name),
                        new FingerRes(R.drawable.hand_bb_l5_c5, R.string.l_5_finger_name),
                };
                break;
        }
    }


    public int getDrawableId() {
        return drawableId;
    }

    public int getNameId() {
        return nameId;
    }
}

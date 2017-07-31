package com.simprints.id.tools;


import com.simprints.libsimprints.Tier;

public class TierHelper {
    public static Tier computeTier(float score) {
        if (score < 20f) {
            return Tier.TIER_5;
        } else if (score < 35f) {
            return Tier.TIER_4;
        } else if (score < 50f) {
            return Tier.TIER_3;
        } else if (score < 75f) {
            return Tier.TIER_2;
        } else {
            return Tier.TIER_1;
        }
    }
}

package com.simprints.id.libdata.models.firebase;

import com.simprints.libsimprints.Identification;

@SuppressWarnings("WeakerAccess")
public class fb_Match {
    public String personGuid;
    public float score;

    public fb_Match() {
    }

    public fb_Match(Identification id) {
        this.personGuid = id.getGuid();
        this.score = id.getConfidence();
    }
}

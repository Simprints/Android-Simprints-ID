package com.simprints.libdata.models.firebase;

import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Utils;
import com.simprints.libdata.models.realm.rl_Fingerprint;
import com.simprints.libsimprints.FingerIdentifier;

@SuppressWarnings("unused")
public class fb_Fingerprint {
    public FingerIdentifier fingerId;
    public String template;
    public int qualityScore;

    public fb_Fingerprint() {
    }

    public fb_Fingerprint(Fingerprint print) {
        this.fingerId = print.getFingerId();
        this.qualityScore = print.getQualityScore();
        this.template = Utils.byteArrayToBase64(print.getTemplateBytes());
    }
}

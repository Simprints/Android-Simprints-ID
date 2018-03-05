package com.simprints.id.libdata.models.realm;

import com.simprints.libcommon.Fingerprint;
import com.simprints.id.libdata.models.firebase.fb_Fingerprint;

import io.realm.RealmObject;

@SuppressWarnings("WeakerAccess")
public class rl_Fingerprint extends RealmObject {
    public int fingerId;
    public byte[] template;
    public int qualityScore;
    public rl_Person person;

    public rl_Fingerprint() {
    }

    public rl_Fingerprint(fb_Fingerprint print, rl_Person person) {
        Fingerprint catchPrint;
        try {
            catchPrint = new Fingerprint(print.fingerId, print.template);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        this.fingerId = catchPrint.getFingerId().ordinal();
        this.qualityScore = catchPrint.getQualityScore();
        this.template = catchPrint.getTemplateBytes();
        this.person = person;
    }

    public rl_Fingerprint(Fingerprint print, rl_Person person) {
        this.fingerId = print.getFingerId().ordinal();
        this.qualityScore = print.getQualityScore();
        this.template = print.getTemplateBytes();
        this.person = person;
    }
}


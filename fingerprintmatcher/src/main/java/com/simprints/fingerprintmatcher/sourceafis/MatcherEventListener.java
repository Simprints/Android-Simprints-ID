package com.simprints.fingerprintmatcher.sourceafis;

import com.simprints.fingerprintmatcher.EVENT;
import com.simprints.fingerprintmatcher.Progress;

public interface MatcherEventListener {

    void onMatcherEvent(EVENT event);

    void onMatcherProgress(Progress progress);

}

package com.simprints.libmatcher.sourceafis;

import com.simprints.libmatcher.EVENT;
import com.simprints.libmatcher.Progress;

public interface MatcherEventListener {

    void onMatcherEvent(EVENT event);

    void onMatcherProgress(Progress progress);

}

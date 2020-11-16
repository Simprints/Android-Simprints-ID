package sourceafis;

import com.simprints.fingerprintmatcher.old.EVENT;
import com.simprints.fingerprintmatcher.old.Progress;

public interface MatcherEventListener {

    void onMatcherEvent(EVENT event);

    void onMatcherProgress(Progress progress);

}

package com.simprints.fingerprintmatcher.sourceafis.matching;

import com.simprints.fingerprintmatcher.sourceafis.matching.minutia.EdgeHash;
import com.simprints.fingerprintmatcher.sourceafis.matching.minutia.EdgeTable;
import com.simprints.fingerprintmatcher.sourceafis.templates.Template;
public final class ProbeIndex
{
    public Template template;
    public EdgeTable edges;
    public EdgeHash edgeHash;
}

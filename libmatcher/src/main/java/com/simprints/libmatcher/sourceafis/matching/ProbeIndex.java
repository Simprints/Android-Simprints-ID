package com.simprints.libmatcher.sourceafis.matching;

import com.simprints.libmatcher.sourceafis.matching.minutia.EdgeHash;
import com.simprints.libmatcher.sourceafis.matching.minutia.EdgeTable;
import com.simprints.libmatcher.sourceafis.templates.Template;
public final class ProbeIndex
{
    public Template template;
    public EdgeTable edges;
    public EdgeHash edgeHash;
}

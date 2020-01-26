package com.simprints.fingerprintscanner.v2.stream

import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream

class RootMessageStream(
    incoming: RootMessageInputStream,
    outgoing: RootMessageOutputStream
) : MessageStream<RootMessageInputStream, RootMessageOutputStream>(incoming, outgoing)

package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream

class RootMessageChannel(
    incoming: RootMessageInputStream,
    outgoing: RootMessageOutputStream
) : MessageChannel<RootMessageInputStream, RootMessageOutputStream>(incoming, outgoing)

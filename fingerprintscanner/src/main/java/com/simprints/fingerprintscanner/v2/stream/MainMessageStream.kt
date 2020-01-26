package com.simprints.fingerprintscanner.v2.stream

import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream

class MainMessageStream(
    incoming: MainMessageInputStream,
    outgoing: MainMessageOutputStream
) : MessageStream<MainMessageInputStream, MainMessageOutputStream>(incoming, outgoing)

package com.simprints.fingerprintscanner.v2.channel

import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream

class MainMessageChannel(
    incoming: MainMessageInputStream,
    outgoing: MainMessageOutputStream
) : MessageChannel<MainMessageInputStream, MainMessageOutputStream>(incoming, outgoing)

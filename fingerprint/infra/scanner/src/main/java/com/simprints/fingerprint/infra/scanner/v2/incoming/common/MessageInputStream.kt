package com.simprints.fingerprint.infra.scanner.v2.incoming.common

import com.simprints.fingerprint.infra.scanner.v2.incoming.IncomingConnectable

/**
 * High-level interface for incoming messages based on an [java.io.InputStream]
 */
interface MessageInputStream : IncomingConnectable

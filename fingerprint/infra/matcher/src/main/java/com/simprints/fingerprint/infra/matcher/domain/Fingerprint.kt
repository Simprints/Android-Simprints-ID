package com.simprints.fingerprint.infra.matcher.domain

class Fingerprint(val fingerId: FingerIdentifier, val template: ByteArray, val format: String)

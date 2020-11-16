package com.simprints.fingerprintmatcher

class FingerprintRecord(val id: String, val fingerprints: List<Fingerprint>)

class Fingerprint(val fingerId: FingerIdentifier, val template: ByteArray, val format: TemplateFormat)

enum class FingerIdentifier {
    RIGHT_5TH_FINGER,
    RIGHT_4TH_FINGER,
    RIGHT_3RD_FINGER,
    RIGHT_INDEX_FINGER,
    RIGHT_THUMB,
    LEFT_THUMB,
    LEFT_INDEX_FINGER,
    LEFT_3RD_FINGER,
    LEFT_4TH_FINGER,
    LEFT_5TH_FINGER
}

enum class TemplateFormat {
    ISO_19794_2_2011
}

enum class MatchingAlgorithm {
    SIM_AFIS    // Accept only ISO_19794_2_2011 templates
}

fun MatchingAlgorithm.doesAcceptTemplatesOfFormat(format: TemplateFormat): Boolean =
    when (this) {
        MatchingAlgorithm.SIM_AFIS -> format == TemplateFormat.ISO_19794_2_2011
    }

class MatchResult(val id: String, score: Int)

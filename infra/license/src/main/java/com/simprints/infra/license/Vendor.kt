package com.simprints.infra.license

@JvmInline
value class Vendor(val value: String) {
    companion object {
        val RANK_ONE_FACE_VENDOR = Vendor("RANK_ONE_FACE")
        val NEC_FINGERPRINT_VENDOR = Vendor("NEC_FINGERPRINT")
    }
}

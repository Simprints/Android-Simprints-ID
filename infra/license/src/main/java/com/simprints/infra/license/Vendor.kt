package com.simprints.infra.license

@JvmInline
value class Vendor(val value: String) {
    companion object {
        val RANK_ONE = Vendor("RANK_ONE_FACE")
        val NEC = Vendor("NEC_FINGERPRINT")
    }
}

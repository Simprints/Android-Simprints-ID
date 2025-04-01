package com.simprints.infra.external.credential.store.model

sealed class ExternalCredential {

    abstract val subjectId: String?

    data class QrCode(
        override val subjectId: String?,
        val data: String,
    ) : ExternalCredential()
}

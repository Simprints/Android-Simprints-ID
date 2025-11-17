package com.simprints.feature.externalcredential.ext

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.infra.resources.R as IDR

fun Resources.getQuantityCredentialString(
    @StringRes id: Int,
    @StringRes specificCredentialRes: Int,
    @StringRes multipleCredentialsRes: Int,
    credentialTypes: List<ExternalCredentialType>,
): String {
    val documentTypeRes = if (credentialTypes.size == 1) {
        specificCredentialRes
    } else {
        multipleCredentialsRes
    }
    return getString(id, documentTypeRes)
}

fun Resources.getCredentialFieldTitle(type: ExternalCredentialType): String = when (type) {
    ExternalCredentialType.NHISCard -> IDR.string.mfid_nhis_card_credential_field
    ExternalCredentialType.GhanaIdCard -> IDR.string.mfid_ghana_id_credential_field
    ExternalCredentialType.FaydaCard -> IDR.string.mfid_fayda_card_credential_field
    ExternalCredentialType.QRCode -> IDR.string.mfid_qr_credential_field
}.run(::getString)

fun Resources.getCredentialTypeString(type: ExternalCredentialType?): String = getCredentialTypeRes(type).run(::getString)

fun Resources.getCredentialTypeRes(type: ExternalCredentialType?): Int = when (type) {
    ExternalCredentialType.NHISCard -> IDR.string.mfid_type_nhis_card
    ExternalCredentialType.GhanaIdCard -> IDR.string.mfid_type_ghana_id_card
    ExternalCredentialType.FaydaCard -> IDR.string.mfid_type_fayda_card
    ExternalCredentialType.QRCode -> IDR.string.mfid_type_qr_code
    null -> IDR.string.mfid_type_any_document
}

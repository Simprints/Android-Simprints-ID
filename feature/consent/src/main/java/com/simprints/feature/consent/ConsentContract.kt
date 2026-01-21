package com.simprints.feature.consent

object ConsentContract {
    val DESTINATION = R.id.consentFragment

    fun getParams(consentType: ConsentType) = ConsentParams(consentType)
}

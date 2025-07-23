package com.simprints.feature.consent

object ConsentContract {
    val DESTINATION = R.id.consentFragment

    fun getParams(type: ConsentType) = ConsentParams(type)
}

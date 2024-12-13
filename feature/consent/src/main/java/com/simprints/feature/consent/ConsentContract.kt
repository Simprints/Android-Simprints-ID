package com.simprints.feature.consent

import com.simprints.feature.consent.screens.consent.ConsentFragmentArgs

object ConsentContract {
    val DESTINATION = R.id.consentFragment

    fun getArgs(type: ConsentType) = ConsentFragmentArgs(type).toBundle()
}

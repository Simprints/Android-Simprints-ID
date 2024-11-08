package com.simprints.feature.consent.screens.consent.helpers

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

/**
 * Factory must be injected into the hilt entry point to receive the correct
 * activity context which in turn will be passed to the text helpers.
 */
internal class ConsentTextHelperFactory @Inject constructor(
    @ActivityContext private val context: Context,
) {

    fun createGeneral() = GeneralConsentTextHelper(context)
    fun createParental() = ParentalConsentTextHelper(context)
}

package com.simprints.id.domain.calloutValidation.calloutParameters

import com.simprints.id.domain.calloutValidation.calloutParameter.concrete.UnexpectedExtrasParameter


/**
 * Abstraction of any collection of callout parameters that can be validated.
 */
interface CalloutParameters {

    val unexpectedExtrasParameter: UnexpectedExtrasParameter

    fun validate()

}

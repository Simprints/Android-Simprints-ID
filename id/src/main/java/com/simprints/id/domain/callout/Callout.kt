package com.simprints.id.domain.callout

import android.content.Intent
import com.simprints.id.domain.callout.CalloutAction.Companion.calloutAction
import com.simprints.id.domain.callout.CalloutParameters.Companion.calloutParameters

class Callout(val action: CalloutAction, val parameters: CalloutParameters) {

    companion object {
        @JvmStatic
        fun Intent?.toCallout(): Callout =
            Callout(this.calloutAction, this.calloutParameters)
    }

}

package com.simprints.id.session.callout

import android.content.Intent
import com.simprints.id.session.callout.CalloutAction.Companion.calloutAction
import com.simprints.id.session.callout.CalloutParameters.Companion.calloutParameters

class Callout(val action: CalloutAction, val parameters: CalloutParameters) {

    companion object {
        fun Intent?.toCallout(): Callout =
            Callout(this.calloutAction, this.calloutParameters)
    }

}

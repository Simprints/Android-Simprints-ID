package com.simprints.id.domain.sessionParameters.calloutParameter.extras

import com.simprints.id.domain.callout.CalloutAction.UPDATE
import com.simprints.id.domain.callout.CalloutAction.VERIFY
import com.simprints.id.domain.sessionParameters.calloutParameter.CalloutParameter
import com.simprints.id.domain.sessionParameters.calloutParameter.TypeParameter


class PatientIdParameter(private val typeParameter: TypeParameter,
                         val updateIdParameter: UpdateIdParameter,
                         val verifyIdParameter: VerifyIdParameter,
                         private val defaultValue: String = "")
    : CalloutParameter<String> {

    override val value: String
        get() =
            when (typeParameter.value) {
                UPDATE -> updateIdParameter.value
                VERIFY -> verifyIdParameter.value
                else -> defaultValue
            }

    override fun validate() {
    }

}

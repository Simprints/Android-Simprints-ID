package com.simprints.id.domain.calloutValidation.calloutParameter.concrete

import com.simprints.id.domain.calloutValidation.CalloutType.UPDATE
import com.simprints.id.domain.calloutValidation.CalloutType.VERIFY
import com.simprints.id.domain.calloutValidation.calloutParameter.CalloutParameter


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

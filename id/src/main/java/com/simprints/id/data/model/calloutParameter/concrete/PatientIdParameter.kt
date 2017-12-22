package com.simprints.id.data.model.calloutParameter.concrete

import com.simprints.id.data.model.CalloutType.UPDATE
import com.simprints.id.data.model.CalloutType.VERIFY
import com.simprints.id.data.model.calloutParameter.CalloutParameter


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
        updateIdParameter.validate()
        verifyIdParameter.validate()
    }

}

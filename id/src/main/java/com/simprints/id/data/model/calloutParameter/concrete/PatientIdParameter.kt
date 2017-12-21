package com.simprints.id.data.model.calloutParameter.concrete

import com.simprints.id.data.model.CalloutType.UPDATE
import com.simprints.id.data.model.CalloutType.VERIFY
import com.simprints.id.data.model.calloutParameter.CalloutParameter


class PatientIdParameter(typeParameter: TypeParameter,
                         val updateIdParameter: UpdateIdParameter,
                         val verifyIdParameter: VerifyIdParameter)
    : CalloutParameter<String> {

    private val defaultValue: String = ""

    override val value: String =
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

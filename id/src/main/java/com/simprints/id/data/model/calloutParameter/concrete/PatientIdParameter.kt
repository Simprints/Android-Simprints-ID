package com.simprints.id.data.model.calloutParameter.concrete

import android.content.Intent
import com.simprints.id.data.model.CalloutType
import com.simprints.id.data.model.calloutParameter.CalloutParameter
import java.util.*

class PatientIdParameter(intent: Intent)
    : CalloutParameter<String> {

    private val valueWhenMissing: String = ""

    val updateIdParameter = UpdateIdParameter(intent)
    val verifyIdParameter = UpdateIdParameter(intent)

    private fun newId() = UUID.randomUUID().toString()

    override val value: String =
            when (intent.action) {
                CalloutType.UPDATE.intentAction -> updateIdParameter.value
                CalloutType.VERIFY.intentAction -> verifyIdParameter.value
                CalloutType.IDENTIFY.intentAction -> newId()
                else -> valueWhenMissing
            }

    override fun validate() {
        updateIdParameter.validate()
        verifyIdParameter.validate()
    }

}
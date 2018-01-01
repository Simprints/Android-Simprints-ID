package com.simprints.id.domain.sessionParameters.readers

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import java.util.*


class PatientIdReader(private val verifyIdExtractor: Reader<String>,
                      private val updateIdExtractor: Reader<String>,
                      private val generateGUID: () -> String = { UUID.randomUUID().toString() })
    : Reader<String> {

    override fun readFrom(callout: Callout): String =
        when (callout.action) {
            CalloutAction.UPDATE -> updateIdExtractor.readFrom(callout)
            CalloutAction.VERIFY -> verifyIdExtractor.readFrom(callout)
            CalloutAction.IDENTIFY,
            CalloutAction.REGISTER -> generateGUID()
            else -> throw InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
        }

}

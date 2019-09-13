package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetTriggerButtonActiveResponse(val operationResultCode: OperationResultCode) : VeroResponse(VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE)

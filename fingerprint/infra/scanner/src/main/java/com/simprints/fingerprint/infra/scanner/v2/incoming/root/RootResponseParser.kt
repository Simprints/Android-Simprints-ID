package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.ENTER_CYPRESS_OTA_MODE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.ENTER_MAIN_MODE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.ENTER_STM_OTA_MODE
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_CYPRESS_EXTENDED_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_CYPRESS_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_EXTENDED_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_HARDWARE_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.GET_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.SET_EXTENDED_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType.SET_VERSION
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterCypressOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterStmOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetHardwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.SetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import javax.inject.Inject

class RootResponseParser @Inject constructor() : MessageParser<RootResponse> {
    override fun parse(messageBytes: ByteArray): RootResponse = try {
        RootMessageProtocol.getDataBytes(messageBytes).let { data ->
            when (RootMessageProtocol.getMessageType(messageBytes)) {
                ENTER_MAIN_MODE -> EnterMainModeResponse.fromBytes(data)
                ENTER_CYPRESS_OTA_MODE -> EnterCypressOtaModeResponse.fromBytes(data)
                ENTER_STM_OTA_MODE -> EnterStmOtaModeResponse.fromBytes(data)
                GET_CYPRESS_VERSION -> GetCypressVersionResponse.fromBytes(data)
                GET_VERSION -> GetVersionResponse.fromBytes(data)
                SET_VERSION -> SetVersionResponse.fromBytes(data)
                GET_HARDWARE_VERSION -> GetHardwareVersionResponse.fromBytes(data)
                GET_EXTENDED_VERSION -> GetExtendedVersionResponse.fromBytes(data)
                GET_CYPRESS_EXTENDED_VERSION -> GetCypressExtendedVersionResponse.fromBytes(data)
                SET_EXTENDED_VERSION -> SetVersionResponse.fromBytes(data)
            }
        }
    } catch (e: Exception) {
        handleExceptionDuringParsing(e)
    }
}

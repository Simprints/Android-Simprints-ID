package com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageDistortionConfigurationMatrixResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityPreviewResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetSupportedImageFormatsResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetSupportedTemplateTypesResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetUn20ExtendedAppVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.SetScanLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.StartOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.VerifyOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.WriteOtaChunkResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageParser
import javax.inject.Inject

class Un20ResponseParser @Inject constructor() : MessageParser<Un20Response> {
    override fun parse(messageBytes: ByteArray): Un20Response = try {
        Pair(
            Un20MessageProtocol.getMinorTypeByte(messageBytes),
            Un20MessageProtocol.getDataBytes(messageBytes),
        ).let { (minorTypeByte, data) ->
            when (Un20MessageProtocol.getMessageType(messageBytes)) {
                Un20MessageType.GetUn20ExtendedAppVersion -> GetUn20ExtendedAppVersionResponse.fromBytes(data)
                Un20MessageType.CaptureFingerprint -> CaptureFingerprintResponse.fromBytes(data)
                Un20MessageType.GetImageQualityPreview -> GetImageQualityPreviewResponse.fromBytes(data)
                Un20MessageType.SetScanLedState -> SetScanLedStateResponse.fromBytes(data)
                Un20MessageType.GetSupportedTemplateTypes -> GetSupportedTemplateTypesResponse.fromBytes(data)
                is Un20MessageType.GetTemplate -> GetTemplateResponse.fromBytes(minorTypeByte, data)
                Un20MessageType.GetSupportedImageFormats -> GetSupportedImageFormatsResponse.fromBytes(data)
                is Un20MessageType.GetImage -> GetImageResponse.fromBytes(minorTypeByte, data)
                is Un20MessageType.GetUnprocessedImage -> GetImageResponse.fromBytes(minorTypeByte, data)
                Un20MessageType.GetImageDistortionConfigurationMatrix -> GetImageDistortionConfigurationMatrixResponse.fromBytes(data)

                Un20MessageType.GetImageQuality -> GetImageQualityResponse.fromBytes(data)
                Un20MessageType.StartOta -> StartOtaResponse.fromBytes(data)
                Un20MessageType.WriteOtaChunk -> WriteOtaChunkResponse.fromBytes(data)
                Un20MessageType.VerifyOta -> VerifyOtaResponse.fromBytes(data)
            }
        }
    } catch (e: Exception) {
        handleExceptionDuringParsing(e)
    }
}

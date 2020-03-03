package com.simprints.fingerprintscanner.v2.incoming.main.message.parsers

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.*
import com.simprints.fingerprintscanner.v2.incoming.common.MessageParser

class Un20ResponseParser : MessageParser<Un20Response> {

    override fun parse(messageBytes: ByteArray): Un20Response =
        try {
            Pair(
                Un20MessageProtocol.getMinorTypeByte(messageBytes),
                Un20MessageProtocol.getDataBytes(messageBytes)
            ).let { (minorTypeByte, data) ->
                when (Un20MessageProtocol.getMessageType(messageBytes)) {
                    Un20MessageType.GetUn20AppVersion -> GetUn20AppVersionResponse.fromBytes(data)
                    Un20MessageType.CaptureFingerprint -> CaptureFingerprintResponse.fromBytes(data)
                    Un20MessageType.GetSupportedTemplateTypes -> GetSupportedTemplateTypesResponse.fromBytes(data)
                    is Un20MessageType.GetTemplate -> GetTemplateResponse.fromBytes(minorTypeByte, data)
                    Un20MessageType.GetSupportedImageFormats -> GetSupportedImageFormatsResponse.fromBytes(data)
                    is Un20MessageType.GetImage -> GetImageResponse.fromBytes(minorTypeByte, data)
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

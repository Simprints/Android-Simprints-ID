package com.simprints.fingerprintscanner.v2.incoming.message.parsers

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.message.un20.responses.GetSupportedTemplateTypesResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import org.junit.Test

class MessageParserTest {

    @Test
    fun parseVeroResponse_buildsMessageCorrectlyFromProtocol() {
        val messageParser = VeroResponseParser()

        val rawBytes = "20 10 01 00 FF".hexToByteArray()
        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
        assertThat((actualResponse as GetUn20OnResponse).value).isEqualTo(expectedResponse.value)
    }

    @Test
    fun parseVeroEvent_buildsMessageCorrectlyFromProtocol() {
        val messageParser = VeroEventParser()

        val rawBytes = "3A 00 00 00".hexToByteArray()
        val expectedResponse = TriggerButtonPressedEvent()
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
    }

    @Test
    fun parseUn20Response_buildsMessageCorrectlyFromProtocol() {
        val messageParser = Un20ResponseParser()

        val rawBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val expectedResponse = GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))
        val actualResponse = messageParser.parse(rawBytes)

        assertThat(actualResponse).isInstanceOf(expectedResponse::class.java)
        assertThat((actualResponse as GetSupportedTemplateTypesResponse).supportedTemplateTypes)
            .containsExactlyElementsIn(expectedResponse.supportedTemplateTypes)
            .inOrder()
    }
}

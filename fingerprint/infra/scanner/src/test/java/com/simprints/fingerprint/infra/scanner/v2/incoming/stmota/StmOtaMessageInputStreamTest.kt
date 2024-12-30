package com.simprints.fingerprint.infra.scanner.v2.incoming.stmota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StmOtaMessageInputStreamTest {
    private val stmOtaResponseParser = StmOtaResponseParser()
    private val stmOtaMessageInputStream = StmOtaMessageInputStream(stmOtaResponseParser)

    @Test
    fun stmOtaMessageInputStream_receiveStmOtaResponse_correctlyForwardsResponse() = runTest {
        val messageBytes = "79".hexToByteArray()
        val expectedResponse = CommandAcknowledgement(CommandAcknowledgement.Kind.ACK)
        val bytesFlow = MutableSharedFlow<ByteArray>()
        stmOtaMessageInputStream.connect(bytesFlow)
        launch { bytesFlow.emit(messageBytes) }
        val response = stmOtaMessageInputStream.receiveResponse<CommandAcknowledgement>()
        assertThat(response).isInstanceOf(expectedResponse::class.java)
    }
}

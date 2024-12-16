package com.simprints.infra.orchestration.data.responses

import com.google.common.truth.Truth.assertThat
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.config.ExitFormOption
import org.junit.Test

class AppRefusalResponseTest {
    @Test
    fun testFromResult() {
        mapOf(
            ExitFormResult(true, null, null) to AppRefusalResponse("", ""),
            ExitFormResult(true, null, "reason") to AppRefusalResponse("", "reason"),
            ExitFormResult(true, ExitFormOption.AppNotWorking, "") to AppRefusalResponse("APP_NOT_WORKING", ""),
        ).forEach { (result, expected) ->
            val actual = AppRefusalResponse.fromResult(result)

            assertThat(actual).isEqualTo(expected)
        }
    }
}

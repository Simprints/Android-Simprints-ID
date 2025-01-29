package com.simprints.infra.events.sampledata

import com.simprints.core.domain.modality.Modes
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.Timestamp
import java.util.UUID

object SampleDefaults {
    const val DEFAULT_PROJECT_ID = "DVXF1mu4CAa5FmiPWHXr"
    val DEFAULT_MODULE_ID = "0".asTokenizableRaw()
    val DEFAULT_MODULE_ID_2 = "1".asTokenizableRaw()
    val DEFAULT_MODULES = listOf(DEFAULT_MODULE_ID.value, DEFAULT_MODULE_ID_2.value)

    val DEFAULT_USER_ID = "user_id".asTokenizableRaw()
    val DEFAULT_USER_ID_2 = "user_id_2".asTokenizableRaw()
    const val DEFAULT_METADATA = "DEFAULT_METADATA"

    val CREATED_AT: Timestamp = Timestamp(1234L)
    val ENDED_AT: Timestamp = Timestamp(4567L)

    val GUID1 = UUID.randomUUID().toString()
    val GUID2 = UUID.randomUUID().toString()
    val GUID3 = UUID.randomUUID().toString()

    val TIME1 = System.currentTimeMillis()

    val DEFAULT_MODES = listOf(Modes.FINGERPRINT)
}

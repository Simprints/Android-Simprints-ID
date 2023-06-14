package com.simprints.infra.events.sampledata

import com.simprints.core.domain.modality.Modes
import java.util.*

object SampleDefaults {
    const val DEFAULT_DEVICE_ID = "device_id"
    const val DEFAULT_PROJECT_ID = "DVXF1mu4CAa5FmiPWHXr"
    const val DEFAULT_MODULE_ID = "0"
    const val DEFAULT_MODULE_ID_2 = "1"
    val DEFAULT_MODULES = listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)

    const val DEFAULT_USER_ID = "user_id"
    const val DEFAULT_USER_ID_2 = "user_id_2"
    const val DEFAULT_PROJECT_SECRET = "3xDCW0IL/m7nNBWPlVQljh4RzZgcho3Gp7WEj07YqgSER6ESXeY8tVczlNsxubug7co45/PsfG7JiC9oo/U54w=="
    const val DEFAULT_METADATA = "DEFAULT_METADATA"

    const val CREATED_AT: Long = 1234L
    const val ENDED_AT: Long = 4567L
    const val DEFAULT_ENDED_AT: Long = 0L

    const val STATIC_GUID = "3f0f8e9a-0a0c-456c-846e-577b1440b6fb"
    val GUID1 = UUID.randomUUID().toString()
    val GUID2 = UUID.randomUUID().toString()

    val TIME1 = System.currentTimeMillis()

    val DEFAULT_MODES = listOf(Modes.FINGERPRINT)
}

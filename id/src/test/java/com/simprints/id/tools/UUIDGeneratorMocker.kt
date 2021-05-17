package com.simprints.id.tools

import com.simprints.id.sampledata.SampleDefaults
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.*

fun mockUUID() {
    mockkStatic(UUID::class)
    val guid = mockk<UUID>()
    every { guid.toString() } returns SampleDefaults.STATIC_GUID
    every { UUID.randomUUID() } returns guid
}


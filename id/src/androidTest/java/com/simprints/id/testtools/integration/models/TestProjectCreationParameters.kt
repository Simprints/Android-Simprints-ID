package com.simprints.id.testtools.integration.models

import java.util.*

data class TestProjectCreationParameters(val name: String = "Test Project",
                                         val description: String = "Test Project for Android tests",
                                         val creator: String = "Android Squad Testers",
                                         val legacyId: String = UUID.randomUUID().toString())

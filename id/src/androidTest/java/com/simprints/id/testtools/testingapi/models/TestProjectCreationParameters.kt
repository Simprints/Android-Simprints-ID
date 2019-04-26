package com.simprints.id.testtools.testingapi.models

data class TestProjectCreationParameters(val name: String = "Test Project",
                                         val description: String = "Test Project for Android tests",
                                         val creator: String = "Android Squad Testers",
                                         val legacyId: String? = null)

package com.simprints.id.data.consent.longconsent

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import java.io.File

class LongConsentLocalDataSourceImplTest {

    companion object {
        private const val DEFAULT_LANGUAGE = "EN"
        private const val PROJECT_ID_TEST = "project_id_test"
        const val ABSOLUTE_PATH = "app_root_folder"
    }

    @MockK lateinit var loginInfoManagerMock: LoginInfoManager
    private lateinit var longConsentLocalDataSource: LongConsentLocalDataSourceImpl

    private val longConsentsPath = "$ABSOLUTE_PATH${File.separator}long-consents"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns PROJECT_ID_TEST
        longConsentLocalDataSource = LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock)
    }

    @Test
    fun createBaseFilePath_shouldHaveTheRightPath() {
        val actualBaseFilePath = longConsentLocalDataSource.baseFilePath.toString()

        assertThat(actualBaseFilePath).isEqualTo(longConsentsPath)
    }

    @Test
    fun createLocalFilePath_shouldHaveTheRightPath() {
        val actualFilePathForProject = longConsentLocalDataSource.filePathForProject.toString()

        assertThat(actualFilePathForProject).contains("${longConsentsPath}${File.separator}${PROJECT_ID_TEST}")
    }

    @Test
    fun createFileForLanguage_shouldHaveTheRightPath() {
        val actualFile = longConsentLocalDataSource.createFileForLanguage(DEFAULT_LANGUAGE).toString()

        assertThat(actualFile).contains("${longConsentsPath}${File.separator}${PROJECT_ID_TEST}${File.separator}${DEFAULT_LANGUAGE}.txt")
    }
}

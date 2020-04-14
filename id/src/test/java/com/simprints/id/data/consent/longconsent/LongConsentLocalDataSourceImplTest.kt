package com.simprints.id.data.consent.longconsent

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.loginInfo.LoginInfoManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class LongConsentLocalDataSourceImplTest {

    companion object {
        private const val DEFAULT_LANGUAGE = "EN"
        private const val PROJECT_ID_TEST = "project_id_test"
        const val ABSOLUTE_PATH = "app_root_folder"
        private const val LONG_CONSENTS_PATH = "$ABSOLUTE_PATH/long-consents"
    }

    @MockK lateinit var loginInfoManagerMock: LoginInfoManager
    private lateinit var longConsentLocalDataSource: LongConsentLocalDataSourceImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns PROJECT_ID_TEST
        longConsentLocalDataSource = LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock)
    }

    @Test
    fun createBaseFilePath_shouldHaveTheRightPath() {
        val actualBaseFilePath = longConsentLocalDataSource.baseFilePath.toString()

        assertThat(actualBaseFilePath).isEqualTo(LONG_CONSENTS_PATH)
    }

    @Test
    fun createLocalFilePath_shouldHaveTheRightPath() {
        val actualFilePathForProject = longConsentLocalDataSource.filePathForProject.toString()

        assertThat(actualFilePathForProject).contains("${LONG_CONSENTS_PATH}/${PROJECT_ID_TEST}")
    }

    @Test
    fun createFileForLanguage_shouldHaveTheRightPath() {
        val actualFile = longConsentLocalDataSource.createFileForLanguage(DEFAULT_LANGUAGE).toString()

        assertThat(actualFile).contains("${LONG_CONSENTS_PATH}/${PROJECT_ID_TEST}/${DEFAULT_LANGUAGE}.txt")
    }
}

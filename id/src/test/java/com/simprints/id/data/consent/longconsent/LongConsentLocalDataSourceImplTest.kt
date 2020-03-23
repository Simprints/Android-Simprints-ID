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
        private const val EN = "EN"
        private const val PROJECT_ID_TEST = "project_id_test"
        const val ABSOLUTE_PATH = "app_root_folder"
        private const val LONG_CONSENTS_PATH = "$ABSOLUTE_PATH/long-consents"
    }

    @MockK lateinit var loginInfoManagerMock: LoginInfoManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns PROJECT_ID_TEST
    }

    @Test
    fun createBaseFilePath_shouldHaveTheRightPath() {
        val longConsentLocalDataSource = LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock)

        assertThat(longConsentLocalDataSource.baseFilePath.toString()).isEqualTo(LONG_CONSENTS_PATH)
    }

    @Test
    fun createLocalFilePath_shouldHaveTheRightPath() {
        val longConsentLocalDataSource = LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock)

        assertThat(longConsentLocalDataSource.filePathForProject.toString()).contains("${LONG_CONSENTS_PATH}/${PROJECT_ID_TEST}")
    }

    @Test
    fun createFileForLanguage_shouldHaveTheRightPath() {
        val longConsentLocalDataSourceSpy = (LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock))

        assertThat(longConsentLocalDataSourceSpy.createFileForLanguage(EN).toString()).contains("${LONG_CONSENTS_PATH}/${PROJECT_ID_TEST}/${EN}.txt")
    }
}

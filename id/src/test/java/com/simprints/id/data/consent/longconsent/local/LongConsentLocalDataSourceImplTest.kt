package com.simprints.id.data.consent.longconsent.local

import com.google.common.truth.Truth.assertThat
import com.simprints.core.login.LoginInfoManager
import io.mockk.*
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
        val actualFilePathForProject = longConsentLocalDataSource.projectFilePath.toString()

        assertThat(actualFilePathForProject).contains("${longConsentsPath}${File.separator}${PROJECT_ID_TEST}")
    }

    @Test
    fun createFileForLanguage_shouldHaveTheRightPath() {
        val actualFile = longConsentLocalDataSource.createFileForLanguage(DEFAULT_LANGUAGE).toString()

        assertThat(actualFile).contains("${longConsentsPath}${File.separator}${PROJECT_ID_TEST}${File.separator}${DEFAULT_LANGUAGE}.txt")
    }

    @Test
    fun deleteConsentFolders_shouldRemove_allRelatedFiles_andFolders() {
        longConsentLocalDataSource = spyk(LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock))

        val file: File = mockk(relaxed = true) {
            every { delete() } returns true
        }

        val directory: File = mockk(relaxed = true) {
            every { isDirectory } returns true
            every { delete() } returns true
            every { listFiles() } returns arrayOf(file)
        }

        every { longConsentLocalDataSource.baseFilePath } returns mockk {
            every { listFiles() } returns arrayOf(directory)
        }


        longConsentLocalDataSource.deleteLongConsents()

        verify(exactly = 1) { file.delete() }
        verify(exactly = 1) { directory.delete() }
    }

    @Test
    fun getLongConsent_shouldReturn_anEmptyString_whenLongConsent_isNotAvailable() {
        longConsentLocalDataSource = spyk(LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock)) {
            every { isLongConsentPresentInLocal(DEFAULT_LANGUAGE) } returns false
        }

        val longConsent = longConsentLocalDataSource.getLongConsentText(DEFAULT_LANGUAGE)

        assertThat(longConsent.isEmpty()).isTrue()
    }
}

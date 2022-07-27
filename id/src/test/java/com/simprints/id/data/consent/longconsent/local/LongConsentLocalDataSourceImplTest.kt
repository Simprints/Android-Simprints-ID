package com.simprints.id.data.consent.longconsent.local

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.id.tools.utils.FileUtil
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.After
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
        val actualBaseFilePath = longConsentLocalDataSource.baseFilePath.path

        assertThat(actualBaseFilePath).isEqualTo(longConsentsPath)
    }

    @Test
    fun createLocalFilePath_shouldHaveTheRightPath() {
        val actualFilePathForProject = longConsentLocalDataSource.projectFilePath.path

        assertThat(actualFilePathForProject).contains("${longConsentsPath}${File.separator}${PROJECT_ID_TEST}")
    }

    @Test
    fun createFileForLanguage_shouldHaveTheRightPath() {
        val actualFile = longConsentLocalDataSource.createFileForLanguage(DEFAULT_LANGUAGE).toString()

        assertThat(actualFile).contains("${longConsentsPath}${File.separator}${PROJECT_ID_TEST}${File.separator}${DEFAULT_LANGUAGE}.txt")
    }

    @Test
    fun deleteConsentFolders_shouldRemove_allRelatedFiles_andFolders() {
        val file: File = mockk(relaxed = true) {
            every { delete() } returns true
        }

        val directory: File = mockk(relaxed = true) {
            every { isDirectory } returns true
            every { delete() } returns true
            every { listFiles() } returns arrayOf(file)
        }

        mockkObject(FileUtil)

        val mockDirectory = mockk<File> {
            every { absolutePath } returns "absolute_path"
        }

        every { FileUtil.createDirectory(any()) } returns mockDirectory
        every { FileUtil.createFile(any<String>(), any()) } returns mockk()


        every { mockDirectory.listFiles() } returns arrayOf(directory)

        longConsentLocalDataSource.deleteLongConsents()

        verify(exactly = 1) { file.delete() }
        verify(exactly = 1) { directory.delete() }
    }

    @Test
    fun getLongConsent_shouldReturn_anEmptyString_whenLongConsent_isNotAvailable() {
        longConsentLocalDataSource = LongConsentLocalDataSourceImpl(ABSOLUTE_PATH, loginInfoManagerMock)

        mockkObject(FileUtil)
        every { FileUtil.exists(any(), any()) } returns false

        val longConsent = longConsentLocalDataSource.getLongConsentText(DEFAULT_LANGUAGE)

        assertThat(longConsent.isEmpty()).isTrue()
    }

    @Test
    fun getLongConsent_shouldReadFile_whenLongConsent_isAvailable() {
        val expectedLongConsent = "some long consent"
        mockkObject(FileUtil)

        val mockDirectory = mockk<File> {
            every { absolutePath } returns "absolute_path"
        }

        every { FileUtil.createDirectory(any()) } returns mockDirectory
        every { FileUtil.createFile(any<String>(), any()) } returns mockk()
        every { FileUtil.exists(any(), any()) } returns true
        every { FileUtil.readFile(any()) } returns expectedLongConsent.reader().buffered()

        val longConsent = longConsentLocalDataSource.getLongConsentText(DEFAULT_LANGUAGE)

        print(longConsent)
        assertThat(longConsent).isEqualTo("$expectedLongConsent\n")
    }

    @After
    fun tearDown() {
        unmockkObject(FileUtil)
    }
}

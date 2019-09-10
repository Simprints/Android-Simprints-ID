package com.simprints.id.data.consent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.spy
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class LongConsentManagerImplTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject lateinit var localDbManagerMock: LocalDbManager
    @Inject lateinit var loginInfoManagerMock: LoginInfoManager

    private val module by lazy {
        TestAppModule(
            app,
            localDbManagerRule = DependencyRule.MockRule,
            loginInfoManagerRule = DependencyRule.MockRule
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        whenever { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } thenReturn PROJECT_ID_TEST
    }

    @Test
    fun createBaseFilePath_shouldHaveTheRightPath() {
        val longConsentManager = LongConsentManagerImpl(ABSOLUTE_PATH, mock(), mock())
        assertThat(longConsentManager.baseFilePath.toString()).isEqualTo(LONG_CONSENTS_PATH)
    }

    @Test
    fun createLocalFilePath_shouldHaveTheRightPath() {
        val longConsentManagerSpy = spy(LongConsentManagerImpl(ABSOLUTE_PATH, loginInfoManagerMock, mock()))
        val baseFilePathMock = mock<File>()
        whenever { baseFilePathMock.absolutePath } thenReturn LONG_CONSENTS_PATH
        whenever(longConsentManagerSpy) { baseFilePath } thenReturn baseFilePathMock

        assertThat(longConsentManagerSpy.filePathForProject.toString()).contains("$LONG_CONSENTS_PATH/$PROJECT_ID_TEST")
    }

    @Test
    fun createFileForLanguage_shouldHaveTheRightPath() {
        val longConsentManager = LongConsentManagerImpl(ABSOLUTE_PATH, mock(), mock())
        val filePathForProject = File("$ABSOLUTE_PATH/$PROJECT_ID_TEST")

        assertThat(longConsentManager.createFileForLanguage(filePathForProject, EN).toString()).contains("$ABSOLUTE_PATH/$PROJECT_ID_TEST/$EN.txt")
    }

    companion object {
        private const val EN = "EN"
        private const val PROJECT_ID_TEST = "project_id_test"
        private const val ABSOLUTE_PATH = "app_root_folder"
        private const val LONG_CONSENTS_PATH = "$ABSOLUTE_PATH/long-consents"
    }
}

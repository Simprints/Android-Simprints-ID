package com.simprints.id.data.consent.shortconsent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.domain.modality.Modality.FACE
import com.simprints.id.domain.modality.Modality.FINGER
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.testtools.TestApplication
import com.simprints.id.tools.AndroidResourcesHelperImpl
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ConsentTextRepositoryImplTest {

    val context = ApplicationProvider.getApplicationContext() as TestApplication
    private val androidResourcesHelper = AndroidResourcesHelperImpl(context)

    companion object {
        private const val GENERAL_CONSENT_ENROL_FINGER_TEXT = "I'd like to use your fingerprints to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your fingerprint information and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I use your fingerprints? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PARENTAL_CONSENT_ENROL_FINGER_TEXT = "I'd like to use your child's fingerprints to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their fingerprint information and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I use your child's fingerprints? Please say \"I accept\", \"I decline\", or \"I have questions.\""

        private const val GENERAL_CONSENT_ENROL_FACE_TEXT = "I'd like to take your photographs to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your photographs and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I take your photographs? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PARENTAL_CONSENT_ENROL_FACE_TEXT = "I'd like to take your child's photographs to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their photographs and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I take your child's photographs? Please say \"I accept\", \"I decline\", or \"I have questions.\""

        private const val GENERAL_CONSENT_ENROL_MULTI_TEXT = "I'd like to use your fingerprints and take your photographs to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your fingerprint information, photographs and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I use your fingerprints and take your photographs? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PARENTAL_CONSENT_ENROL_MULTI_TEXT = "I'd like to use your child's fingerprints and take your child's photographs to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their fingerprint information, photographs and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I use your child's fingerprints and take your child's photographs? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
    }

    @Test
    fun getGeneralConsentTextForFinger_shouldReturnCorrectGeneralConsentText() {

        val consentDataSourceMock = mockk<ConsentLocalDataSource>().apply {
            every { this@apply.generalConsentOptionsJson } returns JsonHelper.toJson(GeneralConsentOptions())
        }

        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FINGER))

        val generalConsentText =
            consentTextRepo.getGeneralConsentText(AskConsentRequest(ConsentType.ENROL)).value

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_FINGER_TEXT)
    }

    @Test
    fun getParentalConsentTextForFinger_shouldReturnCorrectParentalConsentText() {

        val consentDataSourceMock = mockk<ConsentLocalDataSource>(relaxed = true).apply {
            every { this@apply.parentalConsentOptionsJson } returns JsonHelper.toJson(ParentalConsentOptions())
        }
        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FINGER))

        val parentalConsentText =
            consentTextRepo.getParentalConsentText(AskConsentRequest(ConsentType.ENROL)).value

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_FINGER_TEXT)
    }

    @Test
    fun getGeneralConsentTextForFace_shouldReturnCorrectGeneralConsentText() {

        val consentDataSourceMock = mockk<ConsentLocalDataSource>(relaxed = true).apply {
            every { this@apply.generalConsentOptionsJson } returns JsonHelper.toJson(GeneralConsentOptions())
        }
        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FACE))

        val generalConsentText =
            consentTextRepo.getGeneralConsentText(AskConsentRequest(ConsentType.ENROL)).value

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_FACE_TEXT)
    }

    @Test
    fun getParentalConsentTextForFace_shouldReturnCorrectParentalConsentText() {

        val consentDataSourceMock = mockk<ConsentLocalDataSource>(relaxed = true).apply {
            every { this@apply.parentalConsentOptionsJson } returns JsonHelper.toJson(ParentalConsentOptions())
        }
        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FACE))

        val parentalConsentText =
            consentTextRepo.getParentalConsentText(AskConsentRequest(ConsentType.ENROL)).value

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_FACE_TEXT)
    }

    @Test
    fun getGeneralConsentTextForMultiModal_shouldReturnCorrectGeneralConsentText() {

        val consentDataSourceMock = mockk<ConsentLocalDataSource>(relaxed = true).apply {
            every { this@apply.generalConsentOptionsJson } returns JsonHelper.toJson(GeneralConsentOptions())
        }
        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FINGER, FACE))

        val generalConsentText =
            consentTextRepo.getGeneralConsentText(AskConsentRequest(ConsentType.ENROL)).value

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_MULTI_TEXT)
    }

    @Test
    fun getParentalConsentTextForMultiModal_shouldReturnCorrectParentalConsentText() {

        val consentDataSourceMock = mockk<ConsentLocalDataSource>(relaxed = true).apply {
            every { this@apply.parentalConsentOptionsJson } returns JsonHelper.toJson(ParentalConsentOptions())
        }
        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FINGER, FACE))

        val parentalConsentText =
            consentTextRepo.getParentalConsentText(AskConsentRequest(ConsentType.ENROL)).value

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_MULTI_TEXT)
    }

    @Test
    fun parentalConsentExists_shouldReturnValueFromConsentDataManager() {
        val consentDataSourceMock = mockk<ConsentLocalDataSource>(relaxed = true).apply {
            every { this@apply.parentalConsentExists } returns true
        }
        val consentTextRepo = ConsentRepositoryImpl(consentDataSourceMock,
            mockk(relaxed = true), PROGRAM_NAME, ORGANIZATION_NAME, androidResourcesHelper, listOf(FINGER))

        val parentalConsentExists = consentTextRepo.parentalConsentExists().value

        assertThat(parentalConsentExists).isEqualTo(true)
    }
}

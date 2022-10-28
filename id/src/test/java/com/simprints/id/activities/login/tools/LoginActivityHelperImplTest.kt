package com.simprints.id.activities.login.tools

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.response.QrCodeResponse
import com.simprints.id.tools.InternalConstants.QrCapture.Companion.QR_SCAN_ERROR_KEY
import com.simprints.id.tools.InternalConstants.QrCapture.Companion.QR_SCAN_RESULT_KEY
import com.simprints.id.tools.InternalConstants.QrCapture.QrCaptureError.*
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityHelperImplTest {

    @MockK lateinit var jsonHelper: JsonHelper

    private lateinit var loginActivityHelper: LoginActivityHelperImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        loginActivityHelper = LoginActivityHelperImpl(jsonHelper)
    }

    @Test
    fun `Invalid scan result throws`() {
        every { jsonHelper.fromJson<QrCodeResponse>(any()) } throws Exception()

        val intent = Intent().putExtra(QR_SCAN_RESULT_KEY, "")
        assertThrows<Throwable> { loginActivityHelper.tryParseQrCodeResponse(intent) }
    }

    @Test
    fun `Valid scan result returns QrCodeResponse object`() {
        val response = QrCodeResponse(
            "projectId",
            "projectSecret"
        )
        every { jsonHelper.fromJson<QrCodeResponse>(any()) } returns response

        val intent = Intent().putExtra(QR_SCAN_RESULT_KEY, "")
        val result = loginActivityHelper.tryParseQrCodeResponse(intent)
        assertEquals(response, result)
    }

    @Test
    fun `Empty error intent returns GENERAL_ERROR`() {
        val intent = Intent()
        val result = loginActivityHelper.tryParseQrCodeError(intent)
        assertEquals(GENERAL_ERROR, result)
    }

    @Test
    fun `GENERAL_ERROR in intent returns GENERAL_ERROR`() {
        val intent = Intent().putExtra(QR_SCAN_ERROR_KEY, GENERAL_ERROR)
        val result = loginActivityHelper.tryParseQrCodeError(intent)
        assertEquals(GENERAL_ERROR, result)
    }

    @Test
    fun `PERMISSION_NOT_GRANTED in intent returns PERMISSION_NOT_GRANTED`() {
        val intent = Intent().putExtra(QR_SCAN_ERROR_KEY, PERMISSION_NOT_GRANTED)
        val result = loginActivityHelper.tryParseQrCodeError(intent)
        assertEquals(PERMISSION_NOT_GRANTED, result)
    }

    @Test
    fun `CAMERA_NOT_AVAILABLE in intent returns CAMERA_NOT_AVAILABLE`() {
        val intent = Intent().putExtra(QR_SCAN_ERROR_KEY,CAMERA_NOT_AVAILABLE)
        val result = loginActivityHelper.tryParseQrCodeError(intent)
        assertEquals(CAMERA_NOT_AVAILABLE, result)
    }
}

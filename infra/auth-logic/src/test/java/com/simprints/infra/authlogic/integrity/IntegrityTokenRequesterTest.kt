package com.simprints.infra.authlogic.integrity

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityServiceException
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.android.play.core.integrity.model.IntegrityErrorCode.CLOUD_PROJECT_NUMBER_IS_INVALID
import com.google.android.play.core.integrity.model.IntegrityErrorCode.GOOGLE_SERVER_UNAVAILABLE
import com.google.android.play.core.integrity.model.IntegrityErrorCode.NETWORK_ERROR
import com.google.android.play.core.integrity.model.IntegrityErrorCode.PLAY_STORE_NOT_FOUND
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authlogic.BuildConfig
import com.simprints.infra.authlogic.integrity.exceptions.IntegrityServiceTemporaryDown
import com.simprints.infra.authlogic.integrity.exceptions.MissingOrOutdatedGooglePlayStoreApp
import com.simprints.infra.authlogic.integrity.exceptions.RequestingIntegrityTokenException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntegrityTokenRequesterTest {
    companion object {
        private const val NONCE = "nonce"
        private const val VALID_INTEGRITY_TOKEN =
            "eyJhbGciOiJSUzI1NiIsIng1YyI6WyJNSUlGa2pDQ0JIcWdBd0lCQWdJUVJYcm9OMFpPZFJrQkFBQUFBQVB1bnpBTkJna3Foa2lHOXcwQkFRc0ZBREJDTVFzd0NRWURWUVFHRXdKVlV6RWVNQndHQTFVRUNoTVZSMjl2WjJ4bElGUnlkWE4wSUZObGNuWnBZMlZ6TVJNd0VRWURWUVFERXdwSFZGTWdRMEVnTVU4eE1CNFhEVEU0TVRBeE1EQTNNVGswTlZvWERURTVNVEF3T1RBM01UazBOVm93YkRFTE1Ba0dBMVVFQmhNQ1ZWTXhFekFSQmdOVkJBZ1RDa05oYkdsbWIzSnVhV0V4RmpBVUJnTlZCQWNURFUxdmRXNTBZV2x1SUZacFpYY3hFekFSQmdOVkJBb1RDa2R2YjJkc1pTQk1URU14R3pBWkJnTlZCQU1URW1GMGRHVnpkQzVoYm1SeWIybGtMbU52YlRDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTmpYa3owZUsxU0U0bSsvRzV3T28rWEdTRUNycWRuODhzQ3BSN2ZzMTRmSzBSaDNaQ1laTEZIcUJrNkFtWlZ3Mks5RkcwTzlyUlBlUURJVlJ5RTMwUXVuUzl1Z0hDNGVnOW92dk9tK1FkWjJwOTNYaHp1blFFaFVXWEN4QURJRUdKSzNTMmFBZnplOTlQTFMyOWhMY1F1WVhIRGFDN09acU5ub3NpT0dpZnM4djFqaTZIL3hobHRDWmUybEorN0d1dHpleEtweHZwRS90WlNmYlk5MDVxU2xCaDlmcGowMTVjam5RRmtVc0FVd21LVkFVdWVVejR0S2NGSzRwZXZOTGF4RUFsK09raWxNdElZRGFjRDVuZWw0eEppeXM0MTNoYWdxVzBXaGg1RlAzOWhHazlFL0J3UVRqYXpTeEdkdlgwbTZ4RlloaC8yVk15WmpUNEt6UEpFQ0F3RUFBYU9DQWxnd2dnSlVNQTRHQTFVZER3RUIvd1FFQXdJRm9EQVRCZ05WSFNVRUREQUtCZ2dyQmdFRkJRY0RBVEFNQmdOVkhSTUJBZjhFQWpBQU1CMEdBMVVkRGdRV0JCUXFCUXdHV29KQmExb1RLcXVwbzRXNnhUNmoyREFmQmdOVkhTTUVHREFXZ0JTWTBmaHVFT3ZQbSt4Z254aVFHNkRyZlFuOUt6QmtCZ2dyQmdFRkJRY0JBUVJZTUZZd0p3WUlLd1lCQlFVSE1BR0dHMmgwZEhBNkx5OXZZM053TG5CcmFTNW5iMjluTDJkMGN6RnZNVEFyQmdnckJnRUZCUWN3QW9ZZmFIUjBjRG92TDNCcmFTNW5iMjluTDJkemNqSXZSMVJUTVU4eExtTnlkREFkQmdOVkhSRUVGakFVZ2hKaGRIUmxjM1F1WVc1a2NtOXBaQzVqYjIwd0lRWURWUjBnQkJvd0dEQUlCZ1puZ1F3QkFnSXdEQVlLS3dZQkJBSFdlUUlGQXpBdkJnTlZIUjhFS0RBbU1DU2dJcUFnaGg1b2RIUndPaTh2WTNKc0xuQnJhUzVuYjI5bkwwZFVVekZQTVM1amNtd3dnZ0VFQmdvckJnRUVBZFo1QWdRQ0JJSDFCSUh5QVBBQWR3Q2t1UW1RdEJoWUZJZTdFNkxNWjNBS1BEV1lCUGtiMzdqamQ4ME95QTNjRUFBQUFXWmREM1BMQUFBRUF3QklNRVlDSVFDU1pDV2VMSnZzaVZXNkNnK2dqLzl3WVRKUnp1NEhpcWU0ZVk0Yy9teXpqZ0loQUxTYmkvVGh6Y3pxdGlqM2RrM3ZiTGNJVzNMbDJCMG83NUdRZGhNaWdiQmdBSFVBVmhRR21pL1h3dXpUOWVHOVJMSSt4MFoydWJ5WkVWekE3NVNZVmRhSjBOMEFBQUZtWFE5ejVBQUFCQU1BUmpCRUFpQmNDd0E5ajdOVEdYUDI3OHo0aHIvdUNIaUFGTHlvQ3EySzAreUxSd0pVYmdJZ2Y4Z0hqdnB3Mm1CMUVTanEyT2YzQTBBRUF3Q2tuQ2FFS0ZVeVo3Zi9RdEl3RFFZSktvWklodmNOQVFFTEJRQURnZ0VCQUk5blRmUktJV2d0bFdsM3dCTDU1RVRWNmthenNwaFcxeUFjNUR1bTZYTzQxa1p6d0o2MXdKbWRSUlQvVXNDSXkxS0V0MmMwRWpnbG5KQ0YyZWF3Y0VXbExRWTJYUEx5RmprV1FOYlNoQjFpNFcyTlJHelBodDNtMWI0OWhic3R1WE02dFg1Q3lFSG5UaDhCb200L1dsRmloemhnbjgxRGxkb2d6L0syVXdNNlM2Q0IvU0V4a2lWZnYremJKMHJqdmc5NEFsZGpVZlV3a0k5Vk5NakVQNWU4eWRCM29MbDZnbHBDZUY1ZGdmU1g0VTl4MzVvai9JSWQzVUUvZFBwYi9xZ0d2c2tmZGV6dG1VdGUvS1Ntcml3Y2dVV1dlWGZUYkkzenNpa3daYmtwbVJZS21qUG1odjRybGl6R0NHdDhQbjhwcThNMktEZi9QM2tWb3QzZTE4UT0iLCJNSUlFU2pDQ0F6S2dBd0lCQWdJTkFlTzBtcUdOaXFtQkpXbFF1REFOQmdrcWhraUc5dzBCQVFzRkFEQk1NU0F3SGdZRFZRUUxFeGRIYkc5aVlXeFRhV2R1SUZKdmIzUWdRMEVnTFNCU01qRVRNQkVHQTFVRUNoTUtSMnh2WW1Gc1UybG5iakVUTUJFR0ExVUVBeE1LUjJ4dlltRnNVMmxuYmpBZUZ3MHhOekEyTVRVd01EQXdOREphRncweU1URXlNVFV3TURBd05ESmFNRUl4Q3pBSkJnTlZCQVlUQWxWVE1SNHdIQVlEVlFRS0V4VkhiMjluYkdVZ1ZISjFjM1FnVTJWeWRtbGpaWE14RXpBUkJnTlZCQU1UQ2tkVVV5QkRRU0F4VHpFd2dnRWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUURRR005RjFJdk4wNXprUU85K3ROMXBJUnZKenp5T1RIVzVEekVaaEQyZVBDbnZVQTBRazI4RmdJQ2ZLcUM5RWtzQzRUMmZXQllrL2pDZkMzUjNWWk1kUy9kTjRaS0NFUFpSckF6RHNpS1VEelJybUJCSjV3dWRnem5kSU1ZY0xlL1JHR0ZsNXlPRElLZ2pFdi9TSkgvVUwrZEVhbHROMTFCbXNLK2VRbU1GKytBY3hHTmhyNTlxTS85aWw3MUkyZE44RkdmY2Rkd3VhZWo0YlhocDBMY1FCYmp4TWNJN0pQMGFNM1Q0SStEc2F4bUtGc2JqemFUTkM5dXpwRmxnT0lnN3JSMjV4b3luVXh2OHZObWtxN3pkUEdIWGt4V1k3b0c5aitKa1J5QkFCazdYckpmb3VjQlpFcUZKSlNQazdYQTBMS1cwWTN6NW96MkQwYzF0Skt3SEFnTUJBQUdqZ2dFek1JSUJMekFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwbEJCWXdGQVlJS3dZQkJRVUhBd0VHQ0NzR0FRVUZCd01DTUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFBd0hRWURWUjBPQkJZRUZKalIrRzRRNjgrYjdHQ2ZHSkFib090OUNmMHJNQjhHQTFVZEl3UVlNQmFBRkp2aUIxZG5IQjdBYWdiZVdiU2FMZC9jR1lZdU1EVUdDQ3NHQVFVRkJ3RUJCQ2t3SnpBbEJnZ3JCZ0VGQlFjd0FZWVphSFIwY0RvdkwyOWpjM0F1Y0d0cExtZHZiMmN2WjNOeU1qQXlCZ05WSFI4RUt6QXBNQ2VnSmFBamhpRm9kSFJ3T2k4dlkzSnNMbkJyYVM1bmIyOW5MMmR6Y2pJdlozTnlNaTVqY213d1B3WURWUjBnQkRnd05qQTBCZ1puZ1F3QkFnSXdLakFvQmdnckJnRUZCUWNDQVJZY2FIUjBjSE02THk5d2Eya3VaMjl2Wnk5eVpYQnZjMmwwYjNKNUx6QU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFHb0ErTm5uNzh5NnBSamQ5WGxRV05hN0hUZ2laL3IzUk5Ha21VbVlIUFFxNlNjdGk5UEVhanZ3UlQyaVdUSFFyMDJmZXNxT3FCWTJFVFV3Z1pRK2xsdG9ORnZoc085dHZCQ09JYXpwc3dXQzlhSjl4anU0dFdEUUg4TlZVNllaWi9YdGVEU0dVOVl6SnFQalk4cTNNRHhyem1xZXBCQ2Y1bzhtdy93SjRhMkc2eHpVcjZGYjZUOE1jRE8yMlBMUkw2dTNNNFR6czNBMk0xajZieWtKWWk4d1dJUmRBdktMV1p1L2F4QlZielltcW13a201ekxTRFc1bklBSmJFTENRQ1p3TUg1NnQyRHZxb2Z4czZCQmNDRklaVVNweHU2eDZ0ZDBWN1N2SkNDb3NpclNtSWF0ai85ZFNTVkRRaWJldDhxLzdVSzR2NFpVTjgwYXRuWnoxeWc9PSJdfQ.eyJub25jZSI6IlpYbEtNR1ZZUVdsUGFVcExWakZSYVV4RFNtaGlSMk5wVDJsS1NWVjZTVEZPYVVvNUxtVjVTblZaYlZscFQycEZNVTVxVFRGT1ZFVjVUVlJaYzBsdFZqUmpRMGsyVFZSVk1rMTZWVEZOVkZWNFRtbDNhV0ZYUmpCSmFtOTRUbFJaZWs1VVZYaE5ha1V5VEVOS01XTXlWbmxSTW5ob1lWY3dhVTlwU1RWUmVtZDNVbXBCTlU1VVZYZFBWVWw2VG1wamVWSnFVWGxSYTBwSFVXdE9SRkpVYkVkUlZVWkVUVlJhUjA1VVZUVlBSRUV4VWtWVmVFOUVWVEZQVkdSR1QwUkJNRkZWVlRKU1ZHTTFVVlJzUTA5VVJUTkpiakF1UW1KS05XNXhVR0ZHYVdNeldXWjZRbGhCUjNOcFZsbHNObWxFV1dWNFNUQkdTREpaZGsxQ1YzVlpadz09IiwidGltZXN0YW1wTXMiOjE1NjM1NTEyMTkyNDksImFwa1BhY2thZ2VOYW1lIjoiY29tLnNpbXByaW50cy5pZCIsImFwa0RpZ2VzdFNoYTI1NiI6Iks2U3lZUGZJTHNkRGVMbnBQY3dEY1o5RFllenRFUENYdkR4Y05CV1c3OU09IiwiY3RzUHJvZmlsZU1hdGNoIjp0cnVlLCJhcGtDZXJ0aWZpY2F0ZURpZ2VzdFNoYTI1NiI6WyIrMkU0ZGFZZjZMUUZrbnB1bGcrZVEvN2xzS1h0OUdoQmFWZ0JrYnI4ZUg0PSJdLCJiYXNpY0ludGVncml0eSI6dHJ1ZX0.YO92ihy-VS00rMYO2xB7V-neU7d9syVvoeBqutsRhEppr2SajGU5U52_sA5OdjuZwR5fCio7ChGwXuW2-oEpA-lwq83O03irGban6MnTGqPj6vczmVL3tBxRrgtgWpu7Kkcqp-p7_wJnsSU6N9zBTjxUkh4YdJ4RtRAGBhqu2SkiqrV0g-Au-fQE4ELsRZZvZy3Xc-7aszi5115HIPKU4Xe5nVxCs9nFhRLxStP9ZzKx2pOSCZU4PHHQFYRDGghX5hUEv_OGoqtwX0TL3NI-duzRZ7QqVCtYQ2X5i-m-F44yiWL4Z1uUd0hRcAQsbkGdHly3lz8qDChy2evR9OGlqg"
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val integrityManager = mockk<IntegrityManager>()
    private val integrityTokenResponseTask = mockk<Task<IntegrityTokenResponse>>()
    private val integrityTokenResponse = mockk<IntegrityTokenResponse>()
    private val integrityTokenRequester = IntegrityTokenRequester(integrityManager, testCoroutineRule.testCoroutineDispatcher)

    @Before
    fun setup() {
        mockkStatic(Tasks::class)
        every { Tasks.await(integrityTokenResponseTask) } returns integrityTokenResponse
    }

    @Test
    fun `should return the integrity token`() = runTest(UnconfinedTestDispatcher()) {
        every {
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest
                    .builder()
                    .setNonce(NONCE)
                    .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                    .build(),
            )
        } returns integrityTokenResponseTask
        every { integrityTokenResponse.token() } returns VALID_INTEGRITY_TOKEN

        val token = integrityTokenRequester.getToken(NONCE)
        assertThat(token).isEqualTo(VALID_INTEGRITY_TOKEN)
    }

    @Test
    fun `should throw a RequestingIntegrityTokenException when failing to retrieve integrity token`() = runTest {
        every {
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest
                    .builder()
                    .setNonce(NONCE)
                    .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                    .build(),
            )
        } throws mockk<IntegrityServiceException> {
            every { errorCode } returns CLOUD_PROJECT_NUMBER_IS_INVALID
            every { cause } returns null
        }
        val exception = assertThrows<RequestingIntegrityTokenException> {
            integrityTokenRequester.getToken(NONCE)
        }
        assertThat(exception.errorCode).isEqualTo(CLOUD_PROJECT_NUMBER_IS_INVALID)
    }

    @Test
    fun `should throw a MissingOrOutdatedGooglePlayStoreApp when google play store not found`() = runTest {
        every {
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest
                    .builder()
                    .setNonce(NONCE)
                    .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                    .build(),
            )
        } throws mockk<IntegrityServiceException> {
            every { errorCode } returns PLAY_STORE_NOT_FOUND
            every { cause } returns null
        }
        val exception = assertThrows<MissingOrOutdatedGooglePlayStoreApp> {
            integrityTokenRequester.getToken(NONCE)
        }
        assertThat(exception.errorCode).isEqualTo(PLAY_STORE_NOT_FOUND)
    }

    @Test
    fun `should throw a IntegrityServiceTemporaryDown when integrity service is down`() = runTest {
        every {
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest
                    .builder()
                    .setNonce(NONCE)
                    .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                    .build(),
            )
        } throws mockk<IntegrityServiceException> {
            every { errorCode } returns GOOGLE_SERVER_UNAVAILABLE
            every { cause } returns null
        }
        val exception = assertThrows<IntegrityServiceTemporaryDown> {
            integrityTokenRequester.getToken(NONCE)
        }
        assertThat(exception.errorCode).isEqualTo(GOOGLE_SERVER_UNAVAILABLE)
    }

    @Test
    fun `should throw a NetworkConnectionException when network error happens`() = runTest {
        every {
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest
                    .builder()
                    .setNonce(NONCE)
                    .setCloudProjectNumber(BuildConfig.CLOUD_PROJECT_ID.toLong())
                    .build(),
            )
        } throws mockk<IntegrityServiceException> {
            every { errorCode } returns NETWORK_ERROR
            every { cause } returns null
        }
        val exception = assertThrows<NetworkConnectionException> {
            integrityTokenRequester.getToken(NONCE)
        }
    }
}

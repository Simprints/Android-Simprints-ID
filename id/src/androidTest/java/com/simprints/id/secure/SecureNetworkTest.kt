package com.simprints.id.secure

import android.support.test.rule.ActivityTestRule
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.network.SimApiClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CompletableFuture

class SecureNetworkTest {

    @Rule
    @JvmField
    val loginTestRule = ActivityTestRule(CheckLoginFromIntentActivity::class.java, false, false)

    @Test
    fun testNonceRequest() {
        val future = CompletableFuture<String>()

        val apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api
        apiClient.nonce("bWOFHInKA2YaQwrxZ7uJ", "the_lone_user")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = {
                    print(it)
                    if (it.isSuccessful) {
                        future.complete("Done")
                    } else {
                        future.complete("Fail")
                    }
                },
                onError = { throwable ->
                    print(throwable)
                    future.complete("Fail")
                })

        assertEquals("Done", future.get())
    }

    @Test
    fun testLegacyProjectRequest() {
        val future = CompletableFuture<String>()

        val apiClient = SimApiClient(SecureApiInterface::class.java, SecureApiInterface.baseUrl).api
        apiClient.legacyProject("a080286a42735eae740b49a4227ab1be")
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = {
                    print(it)
                    if (it.isSuccessful) {
                        future.complete("Done")
                    } else {
                        future.complete("Fail")
                    }
                },
                onError = { throwable ->
                    print(throwable)
                    future.complete("Fail")
                })

        assertEquals("Done", future.get())
    }
}

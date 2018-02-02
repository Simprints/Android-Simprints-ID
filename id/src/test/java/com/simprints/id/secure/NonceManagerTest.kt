package com.simprints.id.secure

import com.simprints.id.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CompletableFuture

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class NonceManagerTest {

    @Test
    fun testFetchNonce() {
        val future = CompletableFuture<String>()

        NonceManager.requestNonce(NonceScope("projectIdTest", "")).subscribe (
            { nonce ->
                print("We got an nonce: $nonce")
                future.complete(nonce)
            },
            { e ->
                future.complete(e.message)
            }
        )

        assertEquals("Hello World!", future.get())
    }
}

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

        assertEquals("Hello World!", future.get())
    }
}

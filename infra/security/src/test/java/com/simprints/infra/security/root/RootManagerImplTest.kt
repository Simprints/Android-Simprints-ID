package com.simprints.infra.security.root

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class RootManagerImplTest {
    private val ctx = mockk<Context>(relaxed = true)
    private var rootManager = RootManagerImpl(ctx)

    @Before
    fun setup() {
        mockkConstructor(RootBeer::class)
    }

    @Test
    @Ignore("PenTest 2025. Root check is temporary removed from 2025.1.0 version for testing purposes")
    // TODO PenTest 2025. Revert addition of the @Ignore annotation
    fun `should throw an exception if the device is rooted`() {
        every { anyConstructed<RootBeer>().isRooted } returns true

        assertThrows<RootedDeviceException> {
            rootManager.checkIfDeviceIsRooted()
        }
    }

    @Test
    @Ignore("PenTest 2025. Root check is temporary removed from 2025.1.0 version for testing purposes")
    // TODO PenTest 2025. Revert addition of the @Ignore annotation
    fun `should not throw an exception if the device is not rooted`() {
        every { anyConstructed<RootBeer>().isRooted } returns false

        rootManager.checkIfDeviceIsRooted()
    }
}

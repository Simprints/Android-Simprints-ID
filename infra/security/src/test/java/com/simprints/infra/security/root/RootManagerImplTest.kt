package com.simprints.infra.security.root

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import com.simprints.infra.security.exceptions.RootedDeviceException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.Before
import org.junit.Test

class RootManagerImplTest {
    private val ctx = mockk<Context>(relaxed = true)
    private var rootManager = RootManagerImpl(ctx)

    @Before
    fun setup() {
        mockkConstructor(RootBeer::class)
    }

    @Test
    fun `should throw an exception if the device is rooted`() {
        every { anyConstructed<RootBeer>().isRooted } returns true

        assertThrows<RootedDeviceException> {
            rootManager.checkIfDeviceIsRooted()
        }
    }

    @Test
    fun `should not throw an exception if the device is not rooted`() {
        every { anyConstructed<RootBeer>().isRooted } returns false

        rootManager.checkIfDeviceIsRooted()
    }
}

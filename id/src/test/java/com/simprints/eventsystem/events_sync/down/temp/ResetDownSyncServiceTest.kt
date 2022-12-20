package com.simprints.eventsystem.events_sync.down.temp

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ResetDownSyncServiceTest {
    @Rule
    val serviceRule = ServiceTestRule()

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val eventDownSyncScopeRepository: EventDownSyncScopeRepository = mockk()

    @BindValue
    @JvmField

    val externalScope = CoroutineScope(Dispatchers.Main + Job())


    @Test
    fun `test bound service`() {
        hiltRule.inject()

        val serviceIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            ResetDownSyncService::class.java
        )
        serviceRule.bindService(serviceIntent)
        coVerify { eventDownSyncScopeRepository.deleteAll() }
    }
}

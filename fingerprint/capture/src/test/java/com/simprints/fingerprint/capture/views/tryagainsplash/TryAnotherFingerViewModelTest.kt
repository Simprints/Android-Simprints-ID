package com.simprints.fingerprint.capture.views.tryagainsplash

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.jraska.livedata.test
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TryAnotherFingerViewModelTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var viewModel: TryAnotherFingerViewModel

    @Before
    fun setUp() {
        viewModel = TryAnotherFingerViewModel()
    }

    @Test
    fun `dismiss value updated after delay`() = runTest {
        val observer = viewModel.dismiss.test()
        assertThat(observer.value()).isFalse()

        advanceTimeBy(3000L)
        assertThat(observer.value()).isTrue()
    }
}

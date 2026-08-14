package com.simprints.feature.setup.location

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class LocationStoreImplTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var collectLocation: CollectLocationUseCase

    private lateinit var appScope: CoroutineScope
    private lateinit var locationStore: LocationStoreImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        appScope = CoroutineScope(testCoroutineRule.testCoroutineDispatcher + Job())
        locationStore = LocationStoreImpl(appScope, collectLocation)
    }

    @Test
    fun `collectLocationInBackground starts a new collection`() {
        coEvery { collectLocation() } returns Unit

        locationStore.collectLocationInBackground()

        coVerify(exactly = 1) { collectLocation() }
    }

    @Test
    fun `collectLocationInBackground cancels a previous collection before starting a new one`() {
        val firstCollectionCancelled = CompletableDeferred<Unit>()
        coEvery { collectLocation() } coAnswers {
            try {
                awaitCancellation()
            } catch (c: CancellationException) {
                firstCollectionCancelled.complete(Unit)
                throw c
            }
        }

        locationStore.collectLocationInBackground()
        locationStore.collectLocationInBackground()

        assertThat(firstCollectionCancelled.isCompleted).isTrue()
    }

    @Test
    fun `cancelLocationCollection cancels the current collection`() {
        val collectionCancelled = CompletableDeferred<Unit>()
        coEvery { collectLocation() } coAnswers {
            try {
                awaitCancellation()
            } catch (c: CancellationException) {
                collectionCancelled.complete(Unit)
                throw c
            }
        }

        locationStore.collectLocationInBackground()
        locationStore.cancelLocationCollection()

        assertThat(collectionCancelled.isCompleted).isTrue()
    }
}

package com.simprints.infra.uibase.viewbinding

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.*
import androidx.viewbinding.ViewBinding
import com.google.common.truth.Truth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.reflect.KProperty

@RunWith(AndroidJUnit4::class)
internal class FragmentViewBindingDelegateTest {
    @MockK
    lateinit var fragment: Fragment

    @MockK
    lateinit var fragmentLifecycle: Lifecycle

    @MockK
    lateinit var viewLifecycleOwner: LifecycleOwner

    @MockK
    lateinit var viewLifecycle: Lifecycle

    @MockK
    lateinit var liveData: LiveData<LifecycleOwner?>

    @MockK
    lateinit var view: View

    @MockK
    lateinit var binding: ViewBinding

    @MockK
    lateinit var property: KProperty<*>

    private lateinit var delegate: FragmentViewBindingDelegate<ViewBinding>
    private val fragmentObserverSlot = slot<DefaultLifecycleObserver>()
    private val liveDataObserverSlot = slot<Observer<LifecycleOwner?>>()

    private var factoryCallCount = 0

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        factoryCallCount = 0

        every { fragment.lifecycle } returns fragmentLifecycle
        every { fragment.viewLifecycleOwnerLiveData } returns liveData
        every { fragment.viewLifecycleOwner } returns viewLifecycleOwner
        every { fragment.requireView() } returns view
        every { viewLifecycleOwner.lifecycle } returns viewLifecycle

        justRun { fragmentLifecycle.addObserver(capture(fragmentObserverSlot)) }
        justRun { liveData.observeForever(capture(liveDataObserverSlot)) }
        justRun { liveData.removeObserver(any()) }

        val factory: (View) -> ViewBinding = {
            factoryCallCount++
            binding
        }

        delegate = FragmentViewBindingDelegate(fragment, factory)
    }

    @Test
    fun `getValue creates and caches binding when view is initialized`() {
        every { viewLifecycle.currentState } returns Lifecycle.State.INITIALIZED

        val result1 = delegate.getValue(fragment, property)
        val result2 = delegate.getValue(fragment, property)

        assertThat(result1).isEqualTo(binding)
        assertThat(result2).isEqualTo(binding)
        assertThat(factoryCallCount).isEqualTo(1)
    }

    @Test
    fun `getValue throws IllegalStateException when view is destroyed`() {
        every { viewLifecycle.currentState } returns Lifecycle.State.DESTROYED

        val exception = assertThrows(IllegalStateException::class.java) {
            delegate.getValue(fragment, property)
        }

        assertThat(exception)
            .hasMessageThat()
            .isEqualTo("Should not attempt to get bindings when Fragment views are destroyed.")

        // Assert: Ensure it didn't try to instantiate a dead view
        assertThat(factoryCallCount).isEqualTo(0)
    }

    @Test
    fun `binding is safely cleared when viewLifecycleOwnerLiveData emits null`() {
        every { viewLifecycle.currentState } returns Lifecycle.State.CREATED

        delegate.getValue(fragment, property)
        assertThat(factoryCallCount).isEqualTo(1)

        fragmentObserverSlot.captured.onCreate(fragment)

        liveDataObserverSlot.captured.onChanged(null)

        delegate.getValue(fragment, property)

        assertThat(factoryCallCount).isEqualTo(2)
    }

    @Test
    fun `observers are correctly registered and unregistered during fragment lifecycle`() {
        val observer = fragmentObserverSlot.captured

        observer.onCreate(fragment)
        verify(exactly = 1) { liveData.observeForever(any()) }

        observer.onDestroy(fragment)
        verify(exactly = 1) { liveData.removeObserver(any()) }
    }
}

package com.simprints.core.tools.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.*
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.core.tools.extentions.onUpdate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SharedPreferencesExtTest {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    private val testKey = "test_string_key"
    private val defaultValue = "default"

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        sharedPreferencesEditor = sharedPreferences.edit()
        // Ensure a clean state before each test
        sharedPreferencesEditor.clear().apply()
    }

    @Test
    fun `onUpdate SHOULD emit default value WHEN inserting null`() = runTest {
        // GIVEN
        val emissions = mutableListOf<String>()
        // WHEN
        val job = launch {
            sharedPreferences.onUpdate(testKey, defaultValue).collect {
                emissions.add(it)
            }
        }
        sharedPreferencesEditor.putString(testKey, "some_value").apply()
        advanceUntilIdle()
        sharedPreferencesEditor.putString(testKey, null).apply()
        advanceUntilIdle()
        // THEN
        assertThat(emissions).containsExactly("some_value", defaultValue).inOrder()

        job.cancel()
    }

    @Test
    fun `onUpdate SHOULD emit existing value immediately WHEN key exists`() = runTest {
        // GIVEN
        val existingValue = "i_exist"
        sharedPreferences.edit().putString(testKey, existingValue).apply()
        val emissions = mutableListOf<String>()

        // WHEN
        val job = launch {
            sharedPreferences.onUpdate(testKey, defaultValue).collect {
                emissions.add(it)
            }
        }
        advanceUntilIdle()
        // THEN
        assertThat(emissions).containsExactly(existingValue)
        job.cancel()
    }

    @Test
    fun `onUpdate SHOULD emit new value WHEN key is updated`() = runTest {
        // GIVEN
        val emissions = mutableListOf<String>()
        val newValue = "new_value"
        val job = launch(testScheduler) {
            sharedPreferences.onUpdate(testKey, defaultValue).collect { emissions.add(it) }
        }

        // THEN
        advanceUntilIdle()
        assertThat(emissions).containsExactly(defaultValue).inOrder()

        // WHEN
        sharedPreferences.edit().putString(testKey, newValue).apply()

        // THEN
        advanceUntilIdle()
        assertThat(emissions).containsExactly(defaultValue, newValue).inOrder()
        job.cancel()
    }

    @Test
    fun `onUpdate SHOULD emit default value WHEN key is removed`() = runTest {
        // GIVEN
        val existingValue = "to_be_removed"
        sharedPreferences.edit().putString(testKey, existingValue).apply()
        val emissions = mutableListOf<String>()
        val job = launch(testScheduler) {
            sharedPreferences.onUpdate(testKey, defaultValue).collect {
                emissions.add(it)
            }
        }

        // THEN
        advanceUntilIdle()
        assertThat(emissions).containsExactly(existingValue).inOrder()

        // WHEN
        sharedPreferences.edit().remove(testKey).apply()

        // THEN
        advanceUntilIdle()
        assertThat(emissions).containsExactly(existingValue, defaultValue).inOrder()
        job.cancel()
    }

    @Test
    fun `onUpdate SHOULD NOT emit WHEN a different key is changed`() = runTest {
        // GIVEN
        val emissions = mutableListOf<String>()
        val job = launch(testScheduler) {
            sharedPreferences.onUpdate(testKey, defaultValue).collect {
                emissions.add(it)
            }
        }

        // THEN
        advanceUntilIdle()
        assertThat(emissions).hasSize(1)
        assertThat(emissions.first()).isEqualTo(defaultValue)

        // WHEN
        sharedPreferences.edit().putString("another_key", "some_other_value").apply()

        // THEN
        advanceUntilIdle()
        assertThat(emissions).hasSize(1)
        job.cancel()
    }
}

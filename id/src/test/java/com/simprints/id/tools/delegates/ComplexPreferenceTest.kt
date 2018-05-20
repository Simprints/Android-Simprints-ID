package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import shared.mock
import shared.whenever
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify

class ComplexPreferenceTest {

    companion object {
        private val aKey = "aKey"
        private val defaultFingerId = FingerIdentifier.LEFT_3RD_FINGER
        private val serializedDefaultFingerId = "left_3rd_finger"
        private val storedFingerId = FingerIdentifier.LEFT_4TH_FINGER
        private val storedSerializedFingerId = "left_4th_finger"
    }

    private val fingerIdSerializer = mockFingerIdentifierSerializer()
    private val improvedEditor = mockImprovedEditor()
    private val improvedPrefs = mockImprovedPreferences(improvedEditor)
    private var fingerPreference by ComplexPreference(improvedPrefs, aKey, defaultFingerId,
            fingerIdSerializer)

    private fun mockFingerIdentifierSerializer(): Serializer<FingerIdentifier> {
        val serializer = mock<Serializer<FingerIdentifier>>()
        whenever(serializer.serialize(defaultFingerId)).thenReturn(serializedDefaultFingerId)
        whenever(serializer.deserialize(serializedDefaultFingerId)).thenReturn(defaultFingerId)
        whenever(serializer.serialize(storedFingerId)).thenReturn(storedSerializedFingerId)
        whenever(serializer.deserialize(storedSerializedFingerId)).thenReturn(storedFingerId)
        return serializer
    }

    private fun mockImprovedEditor(): ImprovedSharedPreferences.Editor {
        val editor = mock<ImprovedSharedPreferences.Editor>()
        whenever(editor.putPrimitive(anyString(), any())).thenReturn(editor)
        return editor
    }

    private fun mockImprovedPreferences(editorToReturn: ImprovedSharedPreferences.Editor): ImprovedSharedPreferences {
        val prefs = mock<ImprovedSharedPreferences>()
        whenever(prefs.getPrimitive(aKey, serializedDefaultFingerId)).thenReturn(storedSerializedFingerId)
        whenever(prefs.edit()).thenReturn(editorToReturn)
        return prefs
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetValueSerializesDefaultValue() {
        val get = fingerPreference
        verify(fingerIdSerializer).serialize(defaultFingerId)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetValueGetsPrimitive() {
        val get = fingerPreference
        verify(improvedPrefs).getPrimitive(aKey, serializedDefaultFingerId)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetValueDeserializesStoredValue() {
        val get = fingerPreference
        verify(fingerIdSerializer).deserialize(storedSerializedFingerId)
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetValueReturnsDeserializedStoredValue() {
        assertEquals(storedFingerId, fingerPreference)
    }

    @Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    @Test
    fun testSetValueSerializesValue() {
        fingerPreference = storedFingerId
        verify(fingerIdSerializer).serialize(storedFingerId)
    }

    @Test
    fun testSetValuePutsSerializedValueWithPutsPrimitive() {
        fingerPreference = storedFingerId
        verify(improvedEditor).putPrimitive(aKey, storedSerializedFingerId)
        verify(improvedEditor).apply()
    }
}

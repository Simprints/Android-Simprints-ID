package com.simprints.id.data.prefs.preferenceType

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.unexpected.MismatchedTypeException
import com.simprints.id.tools.serializers.EnumSerializer
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.testtools.common.syntax.anyNotNull
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify

class ComplexPreferenceTest {

    companion object {
        private const val aKey = "aKey"
        private const val bKey = "bKey"
        private const val cKey = "cKey"
        private val defaultFingerId = FingerIdentifier.LEFT_3RD_FINGER
        private const val serializedDefaultFingerId = "left_3rd_finger"

        private val defaultGroupEnum = GROUP.USER
        private val serializedDefaultGroupEnum = defaultGroupEnum.name

        private val storedFingerId = FingerIdentifier.LEFT_4TH_FINGER
        private const val storedSerializedFingerId = "left_4th_finger"
    }

    private val fingerIdSerializer = mockFingerIdentifierSerializer()
    private val improvedEditor = mockImprovedEditor()
    private val improvedPrefs = mockImprovedPreferences(improvedEditor)
    private var fingerPreference by ComplexPreference(improvedPrefs, aKey, defaultFingerId, fingerIdSerializer)

    private var enumFromIndexInSharedPrefs by ComplexPreference(
        improvedPrefs,
        aKey,
        defaultGroupEnum,
        EnumSerializer(GROUP::class.java))

    private var enumFromNameInSharedPrefs by ComplexPreference(
        improvedPrefs,
        bKey,
        defaultGroupEnum,
        EnumSerializer(GROUP::class.java))

    private var enumFromWrongIndexInSharedPrefs by ComplexPreference(
        improvedPrefs,
        cKey,
        defaultGroupEnum,
        EnumSerializer(GROUP::class.java))

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
        whenever(editor.putPrimitive(anyString(), anyNotNull())).thenReturn(editor)
        return editor
    }

    private fun mockImprovedPreferences(editorToReturn: ImprovedSharedPreferences.Editor): ImprovedSharedPreferences {
        val prefs = mock<ImprovedSharedPreferences>()
        whenever(prefs.getPrimitive(aKey, serializedDefaultFingerId)).thenReturn(storedSerializedFingerId)

        whenever(prefs.getPrimitive(aKey, -1)).thenReturn(2)
        whenever(prefs.getPrimitive(aKey, serializedDefaultGroupEnum))
            .thenThrow(
                MismatchedTypeException("Expecting String, integer stored",
                Throwable("Expecting String, integer stored")))

        whenever(prefs.getPrimitive(bKey, serializedDefaultGroupEnum)).thenReturn(GROUP.MODULE.name)

        whenever(prefs.getPrimitive(cKey, -1)).thenReturn(5)
        whenever(prefs.getPrimitive(cKey, serializedDefaultGroupEnum))
            .thenThrow(
                MismatchedTypeException("Expecting String, integer stored",
                Throwable("Expecting String, integer stored")))

        whenever(prefs.edit()).thenReturn(editorToReturn)
        return prefs
    }

    @Test
    fun testDeserializeEnumWithIndexInSharedPrefs() {
        //SharedPref: 2
        assertEquals(enumFromIndexInSharedPrefs, GROUP.MODULE)
    }

    @Test
    fun testDeserializingEnumWithWrongIndexInSharedPrefsThrowsAnException() {
        //SharedPref: 5
        assertThrows<MismatchedTypeException> { enumFromWrongIndexInSharedPrefs }
    }

    @Test
    fun testDeserializeEnumWithNameInSharedPrefs() {
        //SharedPref: "MODULE"
        assertEquals(enumFromNameInSharedPrefs, GROUP.MODULE)
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

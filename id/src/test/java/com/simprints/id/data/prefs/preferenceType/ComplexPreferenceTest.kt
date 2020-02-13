package com.simprints.id.data.prefs.preferenceType

import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.domain.GROUP
import com.simprints.id.exceptions.unexpected.MismatchedTypeException
import com.simprints.id.tools.serializers.EnumSerializer
import com.simprints.id.tools.serializers.Serializer
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

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
        val serializer = mockk<Serializer<FingerIdentifier>>()
        every { serializer.serialize(defaultFingerId) } returns serializedDefaultFingerId
        every { serializer.deserialize(serializedDefaultFingerId) } returns defaultFingerId
        every { serializer.serialize(storedFingerId) } returns storedSerializedFingerId
        every { serializer.deserialize(storedSerializedFingerId) } returns storedFingerId
        return serializer
    }

    private fun mockImprovedEditor(): ImprovedSharedPreferences.Editor {
        val editor = mockk<ImprovedSharedPreferences.Editor>(relaxed = true)
        every { editor.putPrimitive(any(), any<String>()) } returns editor
        return editor
    }

    private fun mockImprovedPreferences(editorToReturn: ImprovedSharedPreferences.Editor): ImprovedSharedPreferences {
        val prefs = mockk<ImprovedSharedPreferences>(relaxed = true)
        every { prefs.getPrimitive(aKey, serializedDefaultFingerId) } returns storedSerializedFingerId

        every { prefs.getPrimitive(aKey, -1) } returns 2
        every { prefs.getPrimitive(aKey, serializedDefaultGroupEnum) } throws (
            MismatchedTypeException("Expecting String, integer stored",
                Throwable("Expecting String, integer stored")))

        every { prefs.getPrimitive(bKey, serializedDefaultGroupEnum) } returns GROUP.MODULE.name

        every { prefs.getPrimitive(cKey, -1) } returns 5
        every { prefs.getPrimitive(cKey, serializedDefaultGroupEnum) } throws (
            MismatchedTypeException("Expecting String, integer stored",
                Throwable("Expecting String, integer stored")))

        every { prefs.edit() } returns editorToReturn
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
        verify { fingerIdSerializer.serialize(defaultFingerId) }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetValueGetsPrimitive() {
        val get = fingerPreference
        verify { improvedPrefs.getPrimitive(aKey, serializedDefaultFingerId) }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testGetValueDeserializesStoredValue() {
        val get = fingerPreference
        verify { fingerIdSerializer.deserialize(storedSerializedFingerId) }
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
        verify { fingerIdSerializer.serialize(storedFingerId) }
    }

    @Test
    fun testSetValuePutsSerializedValueWithPutsPrimitive() {
        fingerPreference = storedFingerId
        verify { improvedEditor.putPrimitive(aKey, storedSerializedFingerId) }
        verify { improvedEditor.apply() }
    }
}

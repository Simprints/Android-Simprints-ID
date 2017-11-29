package com.simprints.id.tools.delegates

import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.tools.serializers.Serializer
import com.simprints.libsimprints.FingerIdentifier
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever

/**
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class ComplexPreferenceTest {

    companion object {
        private val aKey = "aKey"
        private val aFingerId = FingerIdentifier.LEFT_3RD_FINGER
        private val anotherFingerId = FingerIdentifier.LEFT_4TH_FINGER
        private val aSerializedFingerId = "left_3rd_finger"
        private val anotherSerializedFingerId = "left_4th_finger"
    }

    private lateinit var serializer: Serializer<FingerIdentifier>
    private lateinit var prefs: ImprovedSharedPreferences
    private lateinit var editor: ImprovedSharedPreferences.Editor

    @Suppress("UNCHECKED_CAST")
    @Before
    fun setUp() {
        serializer = Mockito.mock(Serializer::class.java) as Serializer<FingerIdentifier>
        whenever(serializer.serialize(aFingerId)).thenReturn(aSerializedFingerId)
        whenever(serializer.deserialize(aSerializedFingerId)).thenReturn(aFingerId)
        whenever(serializer.serialize(anotherFingerId)).thenReturn(anotherSerializedFingerId)
        whenever(serializer.deserialize(anotherSerializedFingerId)).thenReturn(anotherFingerId)
        editor = Mockito.mock(ImprovedSharedPreferences.Editor::class.java)
        whenever(editor.putAny(Mockito.anyString(), Mockito.any())).thenReturn(editor)
        prefs = Mockito.mock(ImprovedSharedPreferences::class.java)
        whenever(prefs.getAny(aKey, aSerializedFingerId)).thenReturn(anotherSerializedFingerId)
        whenever(prefs.edit()).thenReturn(editor)
    }

    @Test
    fun testGetValue() {
        val fingerPreference by ComplexPreference(prefs, aKey, aFingerId, serializer)
        Assert.assertEquals(anotherFingerId, fingerPreference)
    }

    @Suppress("UNUSED_VALUE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    @Test
    fun testSetValue() {
        var fingerPreference by ComplexPreference(prefs, aKey, aFingerId, serializer)
        fingerPreference = anotherFingerId
        verify(editor).putAny(aKey, anotherSerializedFingerId)
        verify(editor).apply()
    }

}
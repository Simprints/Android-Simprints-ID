package com.simprints.infra.security.keyprovider

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LocalDbKeyTest {
    @Test
    fun `equal should return true if the object is the same`() {
        val key = LocalDbKey("project1", "byte".toByteArray())

        assertThat(key == key).isTrue()
    }

    @Test
    fun `equal should return true if the objects have the same value`() {
        val key1 = LocalDbKey("project1", "byte".toByteArray())
        val key2 = LocalDbKey("project1", "byte".toByteArray())

        assertThat(key1 == key2).isTrue()
    }

    @Test
    fun `equal should return false if the objects don't have the same project id`() {
        val key1 = LocalDbKey("project1", "byte".toByteArray())
        val key2 = LocalDbKey("project2", "byte".toByteArray())

        assertThat(key1 == key2).isFalse()
    }

    @Test
    fun `equal should return false if the objects don't have the same value`() {
        val key1 = LocalDbKey("project1", "other byte".toByteArray())
        val key2 = LocalDbKey("project1", "byte".toByteArray())

        assertThat(key1 == key2).isFalse()
    }

    @Test
    fun `equal should return false if the objects are not the same class`() {
        val key1 = LocalDbKey("project1", "byte".toByteArray())

        assertThat(key1.equals("test")).isFalse()
    }

    @Test
    fun `hashCode should return a different hash code for different keys`() {
        val key1 = LocalDbKey("project1", "byte".toByteArray())
        val key2 = LocalDbKey("project2", "other byte".toByteArray())

        assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode())
    }
}

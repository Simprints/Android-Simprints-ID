package com.simprints.core.domain.common

import org.junit.Assert
import org.junit.Test

class AgeGroupTest {
    @Test
    fun `should return empty when the age group is 0, 0`() {
        val ageGroup = AgeGroup(0, 0)
        Assert.assertTrue(ageGroup.isEmpty())
    }

    @Test
    fun `should return empty when the age group is 0, null`() {
        val ageGroup = AgeGroup(0, null)
        Assert.assertTrue(ageGroup.isEmpty())
    }

    @Test
    fun `should return not empty when the age group is 0, 1`() {
        val ageGroup = AgeGroup(0, 1)
        Assert.assertFalse(ageGroup.isEmpty())
    }

    @Test
    fun `should return not empty when the age group is 1, 0`() {
        val ageGroup = AgeGroup(1, 0)
        Assert.assertFalse(ageGroup.isEmpty())
    }

    @Test
    fun `should return not empty when the age group is 1, null`() {
        val ageGroup = AgeGroup(1, null)
        Assert.assertFalse(ageGroup.isEmpty())
    }

    @Test
    fun `should return true when the age is included in the age group`() {
        val ageGroup = AgeGroup(0, 10)
        Assert.assertTrue(ageGroup.includes(5))
    }

    @Test
    fun `should return false when the age is not included in the age group`() {
        val ageGroup = AgeGroup(0, 10)
        Assert.assertFalse(ageGroup.includes(15))
    }

    @Test
    fun `should return true when endExclusive is null and age is greater than startInclusive`() {
        val ageGroup = AgeGroup(5, null)
        Assert.assertTrue(ageGroup.includes(10))
    }

    @Test
    fun `should return true when endExclusive is null and age is equal to startInclusive`() {
        val ageGroup = AgeGroup(5, null)
        Assert.assertTrue(ageGroup.includes(5))
    }

    @Test
    fun `should return false when endExclusive is null and age is less than startInclusive`() {
        val ageGroup = AgeGroup(5, null)
        Assert.assertFalse(ageGroup.includes(4))
    }

    @Test
    fun `should return false when endExclusive is not null and age is equal to endExclusive`() {
        val ageGroup = AgeGroup(0, 10)
        Assert.assertFalse(ageGroup.includes(10))
    }

    @Test
    fun `should return true when the age group contains the other age group`() {
        val ageGroup = AgeGroup(0, 10)
        val otherAgeGroup = AgeGroup(5, 8)
        Assert.assertTrue(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return true when the age group contains the other age group 2`() {
        val ageGroup = AgeGroup(0, 10)
        val otherAgeGroup = AgeGroup(0, 9)
        Assert.assertTrue(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return true when the age group is the same as the other age group`() {
        val ageGroup = AgeGroup(0, 10)
        val otherAgeGroup = AgeGroup(0, 10)
        Assert.assertTrue(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return false when the age group does not contain the other age group`() {
        val ageGroup = AgeGroup(0, 10)
        val otherAgeGroup = AgeGroup(5, 15)
        Assert.assertFalse(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return false when otherRange endExclusive is null and is contained within ageGroup`() {
        val ageGroup = AgeGroup(5, 10)
        val otherAgeGroup = AgeGroup(7, null)
        Assert.assertFalse(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return true when otherRange endExclusive is null and is contained within ageGroup`() {
        val ageGroup = AgeGroup(0, null)
        val otherAgeGroup = AgeGroup(5, null)
        Assert.assertTrue(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return false when otherRange startInclusive is less than startInclusive and otherRange endExclusive is within ageGroup range`() {
        val ageGroup = AgeGroup(5, 10)
        val otherAgeGroup = AgeGroup(4, 9)
        Assert.assertFalse(ageGroup.contains(otherAgeGroup))
    }

    @Test
    fun `should return false when otherRange startInclusive is within ageGroup range and otherRange endExclusive is greater than endExclusive`() {
        val ageGroup = AgeGroup(5, 10)
        val otherAgeGroup = AgeGroup(6, 11)
        Assert.assertFalse(ageGroup.contains(otherAgeGroup))
    }
}

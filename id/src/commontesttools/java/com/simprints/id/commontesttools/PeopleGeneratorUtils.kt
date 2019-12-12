package com.simprints.id.commontesttools

import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import java.util.*
import kotlin.random.Random

object PeopleGeneratorUtils {

    fun getRandomPeople(nPeople: Int,
                        downSyncOp: DownSyncOperation,
                        toSync: List<Boolean>): MutableList<Person> =
        mutableListOf<Person>().also { fakePeople ->
            repeat(nPeople) {
                fakePeople.add(
                    getRandomPerson(
                        UUID.randomUUID().toString(),
                        downSyncOp.projectId,
                        downSyncOp.userId ?: "",
                        downSyncOp.moduleId ?: "",
                        toSync.takeRandom()))
            }
        }

    fun getRandomPeople(numberOfPeople: Int,
                        projectId: String = UUID.randomUUID().toString(),
                        userId: String = UUID.randomUUID().toString(),
                        moduleId: String = UUID.randomUUID().toString(),
                        toSync: Boolean = false): ArrayList<Person> {

        return arrayListOf<Person>().also { list ->
            repeat(numberOfPeople) {
                list.add(getRandomPerson(
                    UUID.randomUUID().toString(),
                    projectId,
                    userId,
                    moduleId,
                    toSync))
            }
        }.also { people -> people.sortBy { it.updatedAt } }
    }

    fun getRandomPerson(patientId: String = UUID.randomUUID().toString(),
                        projectId: String = UUID.randomUUID().toString(),
                        userId: String = UUID.randomUUID().toString(),
                        moduleId: String = UUID.randomUUID().toString(),
                        toSync: Boolean = false,
                        createdAt: Date = getRandomTime(),
                        updateAt: Date = getRandomTime(),
                        fingerprintSamples: Array<FingerprintSample> = arrayOf(getRandomFingerprintSample(), getRandomFingerprintSample())): Person =
        Person(
            patientId = patientId,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            createdAt = if (!toSync) createdAt else null,
            updatedAt = if (!toSync) updateAt else null,
            toSync = toSync,
            fingerprintSamples = fingerprintSamples.toList()
        )


    fun getRandomFingerprintSample(): FingerprintSample {
        val commonFingerprint = FingerprintGeneratorUtils.generateRandomFingerprint()
        return FingerprintSample(commonFingerprint.fingerIdentifier, commonFingerprint.template, commonFingerprint.templateQualityScore)
    }

    fun getRandomFaceSample() =
        FaceSample(Random.nextBytes(20))


    private fun getRandomTime(minutesOffset: Int = 60): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MINUTE, (Math.random() * minutesOffset).toInt())
        }.time
    }

    private fun <T> List<T>.takeRandom(): T =
        this[Random.nextInt(this.size)]
}

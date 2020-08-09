package com.simprints.id.commontesttools

import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import java.util.*
import kotlin.random.Random

object SubjectsGeneratorUtils {

    fun getRandomPeople(numberOfPeople: Int,
                        projectId: String = UUID.randomUUID().toString(),
                        userId: String = UUID.randomUUID().toString(),
                        moduleId: String = UUID.randomUUID().toString(),
                        toSync: Boolean = false): ArrayList<Subject> {

        return arrayListOf<Subject>().also { list ->
            repeat(numberOfPeople) {
                list.add(getRandomSubject(
                    UUID.randomUUID().toString(),
                    projectId,
                    userId,
                    moduleId,
                    toSync))
            }
        }.also { people -> people.sortBy { it.updatedAt } }
    }

    fun getRandomSubject(patientId: String = UUID.randomUUID().toString(),
                         projectId: String = UUID.randomUUID().toString(),
                         userId: String = UUID.randomUUID().toString(),
                         moduleId: String = UUID.randomUUID().toString(),
                         toSync: Boolean = false,
                         createdAt: Date = getRandomTime(),
                         updateAt: Date = getRandomTime(),
                         fingerprintSamples: Array<FingerprintSample> = arrayOf(getRandomFingerprintSample(), getRandomFingerprintSample())): Subject =
        Subject(
            subjectId = patientId,
            projectId = projectId,
            attendantId = userId,
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

    fun getRandomFaceSample() = FaceSample(Random.nextBytes(64))

    private fun getRandomTime(minutesOffset: Int = 60): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MINUTE, (Math.random() * minutesOffset).toInt())
        }.time
    }

    private fun <T> List<T>.takeRandom(): T =
        this[Random.nextInt(this.size)]
}

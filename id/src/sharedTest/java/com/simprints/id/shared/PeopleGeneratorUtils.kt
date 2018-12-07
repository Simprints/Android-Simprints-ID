package com.simprints.id.shared

import com.simprints.id.domain.Fingerprint
import com.simprints.id.domain.Person
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import java.util.*

object PeopleGeneratorUtils {

    private val random = Random()

    fun getRandomPeople(numberOfPeopleForEachSubScope: Int,
                        syncScope: SyncScope,
                        toSync: List<Boolean>): MutableList<Person> =
        mutableListOf<Person>().also { fakePeople ->
            syncScope.toSubSyncScopes().forEach { subScope ->
                repeat(numberOfPeopleForEachSubScope) {
                    fakePeople.add(
                        getRandomPerson(
                            UUID.randomUUID().toString(),
                            subScope.projectId,
                            subScope.userId ?: "",
                            subScope.moduleId ?: "",
                            toSync.takeRandom()))
                }
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
        }.also { it.sortBy { it.updatedAt } }
    }

    fun getRandomPerson(patientId: String = UUID.randomUUID().toString(),
                        projectId: String = UUID.randomUUID().toString(),
                        userId: String = UUID.randomUUID().toString(),
                        moduleId: String = UUID.randomUUID().toString(),
                        toSync: Boolean = false,
                        fingerprints: Array<Fingerprint> = arrayOf(getRandomFingerprint(), getRandomFingerprint())): Person =
        Person(
            patientId = patientId,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            createdAt = if (!toSync) getRandomTime() else null,
            updatedAt = if (!toSync) getRandomTime() else null,
            toSync = toSync,
            fingerprints = fingerprints.toList()
        )


    fun getRandomFingerprint(): Fingerprint {
        val commonFingerprint = com.simprints.libcommon.Fingerprint.generateRandomFingerprint()
        return Fingerprint(commonFingerprint.fingerId.ordinal, commonFingerprint.templateBytes, 50)
    }

    private fun getRandomTime(minutesOffset: Int = 60): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MINUTE, (Math.random() * minutesOffset).toInt())
        }.time
    }

    private fun <T> List<T>.takeRandom(): T =
        this[random.nextInt(this.size)]
}

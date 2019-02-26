package com.simprints.id.commontesttools

import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.Person
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import java.util.*

object PeopleGeneratorUtils {

    private val random = Random()

    fun getRandomPeople(nPeople: Int,
                        subSyncScope: SubSyncScope,
                        toSync: List<Boolean>): MutableList<Person> =
        mutableListOf<Person>().also { fakePeople ->
            repeat(nPeople) {
                fakePeople.add(
                    getRandomPerson(
                        UUID.randomUUID().toString(),
                        subSyncScope.projectId,
                        subSyncScope.userId ?: "",
                        subSyncScope.moduleId ?: "",
                        toSync.takeRandom()))
            }
        }


    fun getRandomPeople(numberOfPeopleForEachSubScope: Int,
                        syncScope: SyncScope,
                        toSync: List<Boolean>): MutableList<Person> =
        mutableListOf<Person>().also { fakePeople ->
            syncScope.toSubSyncScopes().forEach { subScope ->
                fakePeople.addAll(getRandomPeople(numberOfPeopleForEachSubScope, subScope, toSync))
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
                        fingerprints: Array<Fingerprint> = arrayOf(getRandomFingerprint(), getRandomFingerprint())): Person =
        Person(
            patientId = patientId,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            createdAt = if (!toSync) createdAt else null,
            updatedAt = if (!toSync) updateAt else null,
            toSync = toSync,
            fingerprints = fingerprints.toList()
        )


    fun getRandomFingerprint(): Fingerprint {
        val commonFingerprint = Fingerprint.generateRandomFingerprint()
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

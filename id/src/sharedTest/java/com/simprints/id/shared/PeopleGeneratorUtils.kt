package com.simprints.id.shared

import com.simprints.id.data.db.local.realm.models.rl_Fingerprint
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.tools.extensions.toRealmList
import com.simprints.libcommon.Fingerprint
import java.util.*

object PeopleGeneratorUtils {

    fun getRandomPeople(numberOfPeople: Int,
                        projectId: String = UUID.randomUUID().toString(),
                        userId: String = UUID.randomUUID().toString(),
                        moduleId: String = UUID.randomUUID().toString(),
                        toSync: Boolean = false): ArrayList<rl_Person> {

        return arrayListOf<rl_Person>().also { list ->
            (0 until numberOfPeople).forEach {
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
                        fingerprints: Array<rl_Fingerprint> = arrayOf(getRandomFingerprint(), getRandomFingerprint())): rl_Person =
        rl_Person(
            patientId = patientId,
            projectId = projectId,
            userId = userId,
            moduleId = moduleId,
            createdAt = if (!toSync) getRandomTime() else null,
            updatedAt = if (!toSync) getRandomTime() else null,
            toSync = toSync,
            fingerprints = fingerprints.toList().toRealmList()
        )


    fun getRandomFingerprint(): rl_Fingerprint {
        val fingerprint: Fingerprint = Fingerprint.generateRandomFingerprint()
        return rl_Fingerprint(
            fingerprint.fingerId.ordinal,
            fingerprint.templateBytes,
            50
        )
    }

    private fun getRandomTime(minutesOffset: Int = 60): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MINUTE, (Math.random() * minutesOffset).toInt())
        }.time
    }
}

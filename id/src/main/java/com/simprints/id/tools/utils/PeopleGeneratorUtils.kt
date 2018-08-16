package com.simprints.id.tools.utils

import com.simprints.id.data.db.local.realm.models.rl_Fingerprint
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.libcommon.Fingerprint
import io.realm.RealmList
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
        rl_Person().apply {
            this.patientId = patientId
            this.projectId = projectId
            this.userId = userId
            this.moduleId = moduleId
            createdAt = if (!toSync) getRandomTime() else null
            updatedAt = if (!toSync) getRandomTime() else null
            this.toSync = toSync
            this.fingerprints = RealmList<rl_Fingerprint>().apply { addAll(fingerprints) }
        }


    fun getRandomFingerprint(): rl_Fingerprint {
        val fingerprint: Fingerprint = Fingerprint.generateRandomFingerprint()
        val print = rl_Fingerprint()
        print.template = fingerprint.templateBytes
        print.qualityScore = 50
        print.fingerId = fingerprint.fingerId.ordinal

        return print
    }

    private fun getRandomTime(minutesOffset: Int = 60): Date {
        return Calendar.getInstance().apply {
            add(Calendar.MINUTE, (Math.random() * minutesOffset).toInt())
        }.time
    }
}

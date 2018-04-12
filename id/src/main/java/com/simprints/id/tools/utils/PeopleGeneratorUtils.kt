package com.simprints.id.tools.utils

import com.simprints.id.data.db.local.models.rl_Fingerprint
import com.simprints.id.data.db.local.models.rl_Person
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
                list.add(getRandomPerson(projectId, userId, moduleId, toSync))
            }
        }
    }

    fun getRandomPerson(projectId: String = UUID.randomUUID().toString(),
                        userId: String = UUID.randomUUID().toString(),
                        moduleId: String = UUID.randomUUID().toString(),
                        toSync: Boolean = false): rl_Person {

        val prints: RealmList<rl_Fingerprint> = RealmList()
        prints.add(getRandomFingerprint())
        prints.add(getRandomFingerprint())

        return rl_Person().apply {
            patientId = UUID.randomUUID().toString()
            this.projectId = projectId
            this.userId = userId
            this.moduleId = moduleId
            createdAt = if(!toSync) getRandomTime() else null
            updatedAt =  if(!toSync) getRandomTime() else null
            this.toSync = toSync
            fingerprints = prints
        }
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

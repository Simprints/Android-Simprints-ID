package com.simprints.id.tools.utils

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.libdata.models.realm.rl_Fingerprint
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.libcommon.Fingerprint
import io.realm.Realm
import io.realm.RealmList
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.DateFormat
import java.util.*

object FirestoreMigationUtils {

    fun getRandomPerson(): rl_Person {

        val prints: RealmList<rl_Fingerprint> = RealmList()
        prints.add(getRandomFingerprint())
        prints.add(getRandomFingerprint())

        return rl_Person().apply {
            patientId = UUID.randomUUID().toString()
            userId = UUID.randomUUID().toString()
            moduleId = UUID.randomUUID().toString()
            createdAt = Calendar.getInstance().time
            toSync = true
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
}

@Throws(IOException::class)
fun readJsonStream(`in`: InputStream, realm: Realm) {
    val reader = JsonReader(InputStreamReader(`in`))
    val gson = GsonBuilder()
        .setDateFormat(DateFormat.FULL, DateFormat.FULL).create()

    reader.beginArray()
    var counter = 0

    while (reader.hasNext()) {
        counter++

        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }

        val person = gson.fromJson<fb_Person>(reader, fb_Person::class.java)
        realm.copyToRealmOrUpdate(rl_Person(person))
        //Log.d("TEST", "Saved a new person: " + counter);

        if (counter % 500 == 0 || !reader.hasNext() && realm.isInTransaction) {
            realm.commitTransaction()
        }
    }

    reader.endArray()
    reader.close()
}

package com.simprints.id.data.db.local.models

import com.simprints.id.BuildConfig
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.roboletric.TestApplication
import com.simprints.id.tools.utils.FirestoreMigationUtils
import io.realm.RealmList
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class rlPersonTest : RxJavaTest() {

    @Test
    fun createlibPersonFromRlPerson() {

        val rlPerson = FirestoreMigationUtils.getRandomPerson()
        val prints: RealmList<rl_Fingerprint> = RealmList()
        prints.add(FirestoreMigationUtils.getRandomFingerprint())
        prints.add(FirestoreMigationUtils.getRandomFingerprint())
        prints.add(FirestoreMigationUtils.getRandomFingerprint())
        rlPerson.fingerprints = prints

        val libPerson = rlPerson.libPerson

        Assert.assertTrue(prints.any { it.fingerId == libPerson.fingerprints[0].fingerId.ordinal })
        Assert.assertTrue(prints.any { it.fingerId == libPerson.fingerprints[1].fingerId.ordinal })

        val personFromJson = JsonHelper.gson.fromJson(rlPerson.jsonPerson.toString(), rl_Person::class.java)
        Assert.assertEquals(JsonHelper.toJson(rlPerson), JsonHelper.toJson(personFromJson))
    }
}

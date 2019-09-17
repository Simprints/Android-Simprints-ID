package com.simprints.id.data.db.common.realm

import android.content.Context
import android.content.res.AssetManager
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream


@RunWith(AndroidJUnit4::class)
class PeopleRealmMigrationTest {

    private val dbV6fileName = "test_project_v6.realm"
    private val dbV6Project = "9mEsrdXAsfSbOqOYa5Tk"
    private val dbV6Key = "KEWsuk0UdOawjyBtjSTMAv6D8o126b6zB+uDR8okr7UVpIe1+btEsZ/KtFRkkVlLp9SgsdC5P8VF\nfLvNHeiE0g==\n"

    val packageName = "com.simprints.id.test"

    @Test
    fun migrateFrom6To7() {

        val app = ApplicationProvider.getApplicationContext<Application>()
        val resources = app.packageManager.getResourcesForApplication(packageName)
        val dbFileV6 = copyRealmFromAssets(app, resources.assets, dbV6fileName)

        Realm.init(app)

        val config = RealmConfiguration
            .Builder()
            .directory(dbFileV6.parentFile)
            .name(dbFileV6.name)
            .schemaVersion(PeopleRealmMigration.REALM_SCHEMA_VERSION)
            .encryptionKey(Base64.decode(dbV6Key, Base64.DEFAULT))
            .modules(PeopleRealmMigration.PeopleModule())
            .migration(PeopleRealmMigration(dbV6Project))
            .build()

        Realm.getInstance(config).close()
    }

    private fun copyRealmFromAssets(context: Context, assetManager: AssetManager, realmName: String): File {
        // Delete the existing file before copy
        val originalDb = assetManager.open(realmName)
        val tmpDb = File(context.filesDir, realmName)
        if (tmpDb.exists()) {
            tmpDb.delete()
        }

        return tmpDb.also {
            val outputStream = FileOutputStream(tmpDb)
            originalDb.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

}

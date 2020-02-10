package com.simprints.id.data.db.common.realm

import android.content.Context
import android.content.res.AssetManager
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream


@RunWith(AndroidJUnit4::class)
class PeopleRealmMigrationTest {

    companion object {
        private const val ASSET_FOLDER_FOR_REALM_DB = "realm"
        private const val DB_V6_FILENAME = "db_v6.realm"
        private const val DB_V7_FILENAME = "db_v7.realm"
        private const val DB_V6_KEY = "KEWsuk0UdOawjyBtjSTMAv6D8o126b6zB+uDR8okr7UVpIe1+btEsZ/KtFRkkVlLp9SgsdC5P8VF\nfLvNHeiE0g==\n"
        private const val DB_V7_KEY = "j1kkhzjFKEHlBnHO3AkijxaT03Fii6avZAcig0PfQbVlK8kRnHSNKrzRwdaLzpxzFaQIYoJ0naA5\n4yGAywAutg==\n"
        private const val PACKAGE_NAME = "com.simprints.id.test"
    }

    @Test
    fun migrateFromV6ToV7() {

        val app = ApplicationProvider.getApplicationContext<Application>()
        val resources = app.packageManager.getResourcesForApplication(PACKAGE_NAME)
        val dbFileV6 = copyRealmFromAssets(app, resources.assets, DB_V6_FILENAME)

        Realm.init(app)

        val config = RealmConfiguration
            .Builder()
            .directory(dbFileV6.parentFile)
            .name(dbFileV6.name)
            .schemaVersion(7)
            .encryptionKey(Base64.decode(DB_V6_KEY, Base64.DEFAULT))
            .modules(PeopleRealmMigration.PeopleModule())
            .migration(PeopleRealmMigration(DEFAULT_PROJECT_ID))
            .build()

        Realm.getInstance(config).close()
    }

    @Test
    fun migrateFromV7ToV8() {

        val app = ApplicationProvider.getApplicationContext<Application>()
        val resources = app.packageManager.getResourcesForApplication(PACKAGE_NAME)
        val dbFileV7 = copyRealmFromAssets(app, resources.assets, DB_V7_FILENAME)

        Realm.init(app)

        val config = RealmConfiguration
            .Builder()
            .directory(dbFileV7.parentFile)
            .name(dbFileV7.name)
            .schemaVersion(8)
            .encryptionKey(Base64.decode(DB_V7_KEY, Base64.DEFAULT))
            .modules(PeopleRealmMigration.PeopleModule())
            .migration(PeopleRealmMigration(DEFAULT_PROJECT_ID))
            .build()

        Realm.getInstance(config).close()
    }

    private fun copyRealmFromAssets(context: Context, assetManager: AssetManager, dbFile: String): File {
        val originalDb = assetManager.open("$ASSET_FOLDER_FOR_REALM_DB/$dbFile")
        val tmpDb = File("${context.filesDir}", DB_V6_FILENAME)
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

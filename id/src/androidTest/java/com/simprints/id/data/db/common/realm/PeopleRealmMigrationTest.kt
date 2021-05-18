package com.simprints.eventsystem.common.realm

/*
 * FIXME: These tests are failing due to all Realm objects being always at the latest version
 */

/*class PeopleRealmMigrationTest {

    @Test
    fun migrateFromV6ToV7() {
        testMigration(DB_V6_FILENAME, DB_V6_KEY, targetVersion = 7)
    }

    @Test
    fun migrateFromV7ToV8() {
        testMigration(DB_V7_FILENAME, DB_V7_KEY, targetVersion = 8)
    }

    private fun testMigration(dbFileName: String, key: String, targetVersion: Long) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val resources = app.packageManager.getResourcesForApplication(PACKAGE_NAME)
        val dbFile = copyRealmFromAssets(app, resources.assets, dbFileName)

        Realm.init(app)

        val config = RealmConfiguration
            .Builder()
            .directory(dbFile.parentFile)
            .name(dbFile.name)
            .schemaVersion(targetVersion)
            .encryptionKey(Base64.decode(key, Base64.DEFAULT))
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
            val outputStream = FileOutputStream(it)
            originalDb.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private companion object {
        const val ASSET_FOLDER_FOR_REALM_DB = "realm"

        const val PACKAGE_NAME = "com.simprints.id.test"

        const val DB_V6_FILENAME = "db_v6.realm"
        const val DB_V7_FILENAME = "db_v7.realm"

        const val DB_V6_KEY = "KEWsuk0UdOawjyBtjSTMAv6D8o126b6zB+uDR8okr7UVpIe1+btEsZ/KtFRkkVlLp9SgsdC5P8VF\nfLvNHeiE0g==\n"
        const val DB_V7_KEY = "j1kkhzjFKEHlBnHO3AkijxaT03Fii6avZAcig0PfQbVlK8kRnHSNKrzRwdaLzpxzFaQIYoJ0naA5\n4yGAywAutg==\n"
    }

}*/

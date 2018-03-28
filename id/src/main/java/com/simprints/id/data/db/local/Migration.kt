package com.simprints.id.data.db.local

import com.simprints.id.domain.Constants

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema
import java.util.*


internal class Migration : RealmMigration {

    companion object {
        const val REALM_SCHEMA_VERSION: Long = 2

        const val PERSON_TABLE: String = "rl_Person"
        const val USER_TABLE: String = "rl_User"
        const val API_KEY_TABLE: String = "rl_ApiKey"

        const val MODULE_FIELD: String = "moduleId"
        const val UPDATE_FIELD: String = "updatedAt"
        const val SYNC_FIELD: String = "toSync"
        const val ANDROID_ID_FIELD: String = "androidId"
    }

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        for (i in oldVersion..newVersion) {
            when (i.toInt()) {
                0 -> migrateTo1(realm.schema)
                1 -> migrateTo2(realm.schema)
            }
        }

    }

    private fun migrateTo1(schema: RealmSchema) {
        schema.get(PERSON_TABLE)?.addField(MODULE_FIELD, String::class.java)?.transform {
            it.set(MODULE_FIELD, Constants.GLOBAL_ID)
        }

        schema.remove(USER_TABLE)
    }

    private fun migrateTo2(schema: RealmSchema) {
        schema.get(PERSON_TABLE)?.addField(UPDATE_FIELD, Date::class.java)
        schema.get(PERSON_TABLE)?.addField(SYNC_FIELD, Boolean::class.java)?.transform {
            it.set(SYNC_FIELD, false)
        }

        schema.get(PERSON_TABLE)?.removeField(ANDROID_ID_FIELD)
        schema.remove(API_KEY_TABLE)
    }

    override fun hashCode(): Int {
        return 37
    }

    override fun equals(other: Any?): Boolean {
        return other is Migration
    }
}

package com.simprints.id.data.db.local

import com.simprints.id.domain.Constants

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmSchema


internal class Migration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        // Access the Realm schema in order to create, modify or delete classes and their fields.
        val schema = realm.schema

        // Migrate from version 0 to version 1
        if (oldVersion == 0L) handleUpgradeFromVersion0(schema)
    }

    private fun handleUpgradeFromVersion0(schema: RealmSchema) {
        //Add moduleId
        val personSchema = schema.get("rl_Person")
        personSchema?.addField("moduleId", String::class.java)
            ?.transform { obj -> obj.set("moduleId", Constants.GLOBAL_ID) }

        //Drop userId
        schema.remove("rl_User")
    }

    override fun hashCode(): Int {
        return 37
    }

    override fun equals(other: Any?): Boolean {
        return other is Migration
    }
}

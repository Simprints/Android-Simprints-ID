package com.simprints.libdata.models.realm;

import com.simprints.libdata.tools.Constants;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;


@SuppressWarnings("UnusedAssignment")
class Migration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        // Access the Realm schema in order to create, modify or delete classes and their fields.
        RealmSchema schema = realm.getSchema();

        // Migrate from version 0 to version 1
        if (oldVersion == 0) {

            //Add moduleId
            RealmObjectSchema personSchema = schema.get("rl_Person");
            personSchema.addField("moduleId", String.class)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("moduleId", Constants.GLOBAL_ID);
                        }
                    });

            //Drop userId
            schema.remove("rl_User");

            oldVersion++;
        }
    }

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Migration);
    }
}

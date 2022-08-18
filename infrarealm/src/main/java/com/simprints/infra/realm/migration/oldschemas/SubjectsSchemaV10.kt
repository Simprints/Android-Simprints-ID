package com.simprints.infra.realm.migration.oldschemas

//Starting from V10, we would referring Person/People as Subject/Subjects
internal object SubjectsSchemaV10 {
    const val SUBJECT_TABLE: String = "DbSubject"
    const val SUBJECT_ID = "subjectId"
    const val ATTENDANT_ID = "attendantId"
}

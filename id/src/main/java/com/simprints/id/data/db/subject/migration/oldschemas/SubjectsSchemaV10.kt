package com.simprints.eventsystem.subject.migration.oldschemas

//Starting from V10, we would referring Person/People as Subject/Subjects
object SubjectsSchemaV10 {
    const val SUBJECT_TABLE: String = "DbSubject"
    const val SUBJECT_ID = "subjectId"
    const val ATTENDANT_ID = "attendantId"
}

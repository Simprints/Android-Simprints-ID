package com.simprints.id.services.scheduledSync.peopleDownSync

enum class PeopleDownSyncOption {
    OFF,
    BACKGROUND,
    ACTIVE
}

fun PeopleDownSyncOption.shouldDownSyncScheduleInBackground() = this != PeopleDownSyncOption.OFF
fun PeopleDownSyncOption.shouldDownSyncScheduleInForeground() = this == PeopleDownSyncOption.ACTIVE

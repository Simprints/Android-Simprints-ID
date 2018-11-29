package com.simprints.id.services.scheduledSync.peopleDownSync

enum class PeopleDownSyncOption {
    OFF,
    BACKGROUND,
    ACTIVE
}

fun PeopleDownSyncOption.isPeopleDownSyncOff() = this == PeopleDownSyncOption.OFF
fun PeopleDownSyncOption.shouldDownSyncScheduleInForeground() = this == PeopleDownSyncOption.ACTIVE

package com.simprints.id.data.db.local

import com.simprints.libcommon.Person
import com.simprints.libdata.DataCallback
import com.simprints.libdata.DatabaseContext
import com.simprints.libdata.tools.Constants

interface LocalDbManager {

    fun getPeopleCount(dbContext: DatabaseContext, group: Constants.GROUP): Long
    fun loadPeople(dbContext: DatabaseContext, destinationList: MutableList<Person>,
                   group: Constants.GROUP, callback: DataCallback?)
}
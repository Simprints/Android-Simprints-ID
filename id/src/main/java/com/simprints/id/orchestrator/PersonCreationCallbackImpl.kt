package com.simprints.id.orchestrator

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.orchestrator.responsebuilders.AppResponseBuilderForEnrol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonCreationCallbackImpl(
    private val repository: PersonRepository
) : AppResponseBuilderForEnrol.PersonCreationCallback {

    override fun onPersonCreated(person: Person?) {
        CoroutineScope(Dispatchers.Default).launch {
            if (person != null)
                repository.saveAndUpload(person)
        }
    }

}

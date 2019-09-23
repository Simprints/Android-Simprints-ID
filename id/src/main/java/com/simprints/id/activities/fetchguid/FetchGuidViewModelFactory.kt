package com.simprints.id.activities.fetchguid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.tools.utils.SimNetworkUtils

class FetchGuidViewModelFactory(private val personRepository: PersonRepository,
                                private val simNetworkUtils: SimNetworkUtils) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FetchGuidViewModel::class.java)) {
            FetchGuidViewModel(personRepository, simNetworkUtils) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }


}

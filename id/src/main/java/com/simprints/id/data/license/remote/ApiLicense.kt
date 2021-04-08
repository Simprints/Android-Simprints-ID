package com.simprints.id.data.license.remote

import com.fasterxml.jackson.annotation.JsonProperty

data class ApiLicense(@JsonProperty("RANK_ONE_FACE") val rankOneLicense: RankOneLicense?)

data class RankOneLicense(val vendor: String, val expiration: String, val data: String)

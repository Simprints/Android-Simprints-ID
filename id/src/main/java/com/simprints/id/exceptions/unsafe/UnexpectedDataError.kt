package com.simprints.id.exceptions.unsafe

import com.simprints.libdata.DATA_ERROR


class UnexpectedDataError(message: String) : Error(message) {

    companion object {

        @JvmStatic
        fun forDataError(dataError: DATA_ERROR, where: String) =
                UnexpectedDataError("Uncaught or invalid data error in $where : ${dataError.details()}")
    }

}

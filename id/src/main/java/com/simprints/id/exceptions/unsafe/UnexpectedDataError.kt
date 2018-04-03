package com.simprints.id.exceptions.unsafe

import com.simprints.id.data.db.DATA_ERROR


class UnexpectedDataError(message: String = "UnexpectedDataError") : SimprintsError(message) {

    companion object {

        @JvmStatic
        fun forDataError(dataError: DATA_ERROR, where: String) =
                UnexpectedDataError("Uncaught or invalid data error in $where : ${dataError.details()}")
    }

}

package com.simprints.id.exceptions.unexpected

import com.simprints.id.data.db.DATA_ERROR


class UnexpectedDataError(message: String = "UnexpectedDataError") : UnexpectedException(message) {

    companion object {

        @JvmStatic
        fun forDataError(dataError: DATA_ERROR, where: String) =
                UnexpectedDataError("Uncaught or invalid data error in $where : ${dataError.details()}")
    }

}

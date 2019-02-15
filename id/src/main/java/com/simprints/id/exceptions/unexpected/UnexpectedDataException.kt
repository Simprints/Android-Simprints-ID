package com.simprints.id.exceptions.unexpected

import com.simprints.id.data.db.DATA_ERROR


class UnexpectedDataException(message: String = "UnexpectedDataException") : UnexpectedException(message) {

    companion object {

        @JvmStatic
        fun forDataError(dataError: DATA_ERROR, where: String) =
                UnexpectedDataException("Uncaught or invalid data error in $where : ${dataError.details()}")
    }

}

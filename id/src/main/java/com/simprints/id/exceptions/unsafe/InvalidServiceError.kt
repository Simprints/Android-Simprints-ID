package com.simprints.id.exceptions.unsafe


class InvalidServiceError(msg: String): SimprintsError(msg) {

    companion object {

        fun <T> forService(serviceClass: Class<T>) =
                InvalidServiceError("Cannot resolve service ${serviceClass.simpleName}.")

    }

}

fun Any.isPrimitive(): Boolean =
        this is Byte || this is Short || this is Int || this is Long ||
                this is Float || this is Double || this is String || this is Boolean
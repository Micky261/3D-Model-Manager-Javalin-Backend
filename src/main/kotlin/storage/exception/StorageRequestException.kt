package storage.exception

import java.io.IOException

class StorageRequestException(val statusCode: Int) : IOException()

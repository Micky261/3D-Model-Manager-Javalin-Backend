package data.services

import com.google.inject.Inject
import data.dao.ModelDao
import data.dao.ModelFileDao
import data.dao.ModelTagsDao

class ImportService @Inject constructor(
    private val modelDao: ModelDao,
    private val modelFileDao: ModelFileDao,
    private val modelTagsDao: ModelTagsDao,
)

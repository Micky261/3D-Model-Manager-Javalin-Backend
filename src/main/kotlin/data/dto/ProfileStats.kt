package data.dto

import data.bean.Model

data class ProfileStats(
    val modelCount: Long,
    val modelImportedCount: Long,
    val authorCount: Long,
    val tagCount: Long,
    val favoriteCount: Long,
    val linkCount: Long,
    val collectionCount: Long,
    val modelsInCollectionsCount: Long,
    val imageFileCount: Long,
    val threedModelFileCount: Long,
    val fileCount: Long,
    val mostCommonImportSource: String,
    val largestFileModel: Model?,
    val largestFileSize: String,
    val mostFrequentAuthor: String,
    val usedSpace: String,
    val modelWithMostFiles: Model?,
    val modelWithMostFilesCount: Long,
    val modelWithMost3DModels: Model?,
    val modelWithMost3DModelsCount: Long,
//    val modelWithMostTags: Model?,
//    val modelWithMostTagsCount: Long,
    val firstModel: Model?,
    val latestModel: Model?,
    val lastEditedModel: Model?,
)

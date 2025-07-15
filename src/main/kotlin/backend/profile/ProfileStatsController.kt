package backend.profile

import com.google.inject.Inject
import core.javalin.userId
import data.bean.ModelFileType
import data.dto.ProfileStats
import data.services.CollectionService
import data.services.ModelFileService
import data.services.ModelLinkService
import data.services.ModelService
import data.services.ModelTagsService
import io.javalin.http.Context
import utils.formatFileSize

class ProfileStatsController @Inject constructor(
    private val modelService: ModelService,
    private val modelTagsService: ModelTagsService,
    private val modelLinkService: ModelLinkService,
    private val collectionService: CollectionService,
    private val modelFileService: ModelFileService,
) {
    fun getProfileStatistics(ctx: Context) {
        val models = modelService.getAllByUser(ctx.userId())
        val tags = modelTagsService.getWithCount(ctx.userId())
        val links = modelLinkService.getByUser(ctx.userId())
        val collections = collectionService.getCollections(ctx.userId())
        val modelsInCollections = collections.map { collectionService.getModelIdsInCollection(it.id) }
        val files = modelFileService.getFilesByUser(ctx.userId())

        ctx.json(
            ProfileStats(
                modelCount = models.size.toLong(),
                modelImportedCount = models.filter { it.importSource != null }.size.toLong(),
                authorCount = models.map { it.author }.toSet().size.toLong(),
                tagCount = tags.size.toLong(),
                favoriteCount = models.filter { it.favorite }.size.toLong(),
                linkCount = links.size.toLong(),
                collectionCount = collections.size.toLong(),
                modelsInCollectionsCount = modelsInCollections.sumOf { it.size.toLong() },
                imageFileCount = files.filter { it.type == ModelFileType.image }.size.toLong(),
                threedModelFileCount = files.filter { it.type == ModelFileType.model }.size.toLong(),
                fileCount = files.size.toLong(),
                mostCommonImportSource = models.mapNotNull { it.importSource }
                    .map { it.removePrefix("https://").split("/")[0].removePrefix("www.") }
                    .groupingBy { it }
                    .eachCount().maxByOrNull { it.value }?.key ?: "",
                largestFileModel = modelService.get(files.maxByOrNull { it.size }?.modelId ?: 0),
                largestFileSize = files.maxByOrNull { it.size }?.size?.formatFileSize() ?: "",
                mostFrequentAuthor = models.groupingBy { it.author }.eachCount().maxByOrNull { it.value }?.key ?: "",
                usedSpace = files.sumOf { it.size }.formatFileSize(),
                modelWithMostFiles = files.groupBy { it.modelId }.maxByOrNull { it.value.size }
                    ?.let { modelService.get(it.key) },
                modelWithMostFilesCount = files.groupBy { it.modelId }
                    .maxByOrNull { it.value.size }?.value?.size?.toLong() ?: 0,
                modelWithMost3DModels = files.filter { it.type == ModelFileType.model }.groupBy { it.modelId }
                    .maxByOrNull { it.value.size }
                    ?.let { modelService.get(it.key) },
                modelWithMost3DModelsCount = files.filter { it.type == ModelFileType.model }.groupBy { it.modelId }
                    .maxByOrNull { it.value.size }?.value?.size?.toLong() ?: 0,
                firstModel = models.minByOrNull { it.createdAt },
                latestModel = models.maxByOrNull { it.createdAt },
                lastEditedModel = models.maxByOrNull { it.updatedAt },
            ),
        )
    }
}

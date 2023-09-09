package backend.search

import com.google.inject.Inject
import core.javalin.userId
import data.dto.ServerMessage
import data.services.ModelService
import io.javalin.http.Context
import io.javalin.http.pathParamAsClass

class SearchController @Inject constructor(
    private val modelService: ModelService,
) {
    fun search(ctx: Context) {
        val searchTerm = ctx.pathParamAsClass<String>("term").get()
        val searchFields = ctx.pathParamAsClass<String>("fields").get().split(31.toChar()).toSet()

        if (searchFields.isNotEmpty()) {
            ctx.json(modelService.search(ctx.userId(), searchTerm, searchFields))
        } else {
            ServerMessage("SEARCH_ERROR_NO_FIELDS", "Error searching")
                .send(ctx, 400)
        }
    }
}

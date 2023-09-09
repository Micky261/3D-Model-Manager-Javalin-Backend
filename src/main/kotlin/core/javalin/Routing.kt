package core.javalin

import com.google.inject.Inject
import com.google.inject.Injector
import kotlin.reflect.KClass

abstract class Routing<T : Any>(
    private val controller: KClass<T>,
) {
    @Inject
    val injector: Injector? = null

    /**
     * Create your api config in here.
     * Use io.javalin.apibuilder.ApiBuilder.
     */
    abstract fun route(controller: T)

    /**
     * Called by the EntryModule, do not override or call.
     */
    fun call() {
        val instance = injector?.getInstance(controller.java)
        route(
            instance ?: error(
                """
                Could not build routing for controller ${controller.java}, 
                injector field is null
                """.trimIndent(),
            ),
        )
    }
}

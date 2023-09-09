package core.javalin

import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMultibinder

abstract class ControllerModule : KotlinModule() {
    /**
     * Create a route, later injected into Javalin.
     */
    protected inline fun <reified C : Any> route(crossinline router: (C) -> Unit) {
        KotlinMultibinder.newSetBinder<Routing<*>>(binder())
            .addBinding()
            .toInstance(object : Routing<C>(C::class) {
                override fun route(controller: C): Unit = router(controller)
            })
    }
}

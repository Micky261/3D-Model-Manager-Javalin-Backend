package core.javalin

import io.javalin.security.RouteRole

enum class JavalinRole : RouteRole {
    // Every user on internet (includes authorized users)
    Unauthorized,

    // Authorized users only
    Authorized,

    // Authorized user, additional check that the requested model information/file is owned by the user
    // Path must contain "modelId" parameter
    ModelOwnerOnly,
}

package data.dto

data class Register(
    val name: String,
    val email: String,
    val password: String,
    val invitationToken: String? = null,
)

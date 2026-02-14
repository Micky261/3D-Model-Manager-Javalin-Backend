package data.dto

data class ResetPasswordRequest(val token: String, val password: String)

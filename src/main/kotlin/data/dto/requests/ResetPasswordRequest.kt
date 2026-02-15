package data.dto.requests

data class ResetPasswordRequest(val token: String, val password: String)

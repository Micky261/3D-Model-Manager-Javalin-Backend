package core.config.bean

import com.fasterxml.jackson.annotation.JsonValue

enum class RegistrationMode(@JsonValue val value: String) {
    Open("open"),
    Token("token"),
    Disabled("disabled"),
}

package core.email

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.Singleton

@Singleton
class EmailTranslationService @Inject constructor(
    private val objectMapper: ObjectMapper,
) {
    private val translations: Map<String, Map<String, Map<String, String>>> by lazy {
        mapOf(
            "en" to loadTranslations("en"),
            "de" to loadTranslations("de"),
        )
    }

    private val htmlLayout: String by lazy {
        loadResource("i18n/email/layout.html")
    }

    private val textLayout: String by lazy {
        loadResource("i18n/email/layout.txt")
    }

    fun get(locale: String, template: String, key: String): String {
        val lang = resolveLanguage(locale)
        return translations[lang]?.get(template)?.get(key) ?: translations["en"]!![template]!![key]!!
    }

    fun resolve(text: String, params: Map<String, String>): String {
        var result = text
        for ((key, value) in params) {
            result = result.replace("{{$key}}", value)
        }
        return result
    }

    fun renderHtml(template: String, locale: String, params: Map<String, String>): String {
        val allParams = buildTemplateParams(template, locale, params)
        return resolve(htmlLayout, allParams)
    }

    fun renderText(template: String, locale: String, params: Map<String, String>): String {
        val allParams = buildTemplateParams(template, locale, params)
        return resolve(textLayout, allParams)
    }

    private fun buildTemplateParams(
        template: String,
        locale: String,
        params: Map<String, String>,
    ): Map<String, String> {
        val keys = listOf("title", "greeting", "body", "button", "linkHint", "expiry", "footer")
        val translatedParams = keys.associateWith { key -> resolve(get(locale, template, key), params) }
        return translatedParams + params
    }

    private fun resolveLanguage(locale: String): String = when {
        locale.startsWith("de") -> "de"
        else -> "en"
    }

    private fun loadTranslations(lang: String): Map<String, Map<String, String>> {
        val json = loadResource("i18n/email/$lang.json")
        return objectMapper.readValue(json)
    }

    private fun loadResource(path: String): String =
        javaClass.classLoader.getResourceAsStream(path)?.bufferedReader()?.readText()
            ?: throw IllegalStateException("Resource not found: $path")
}

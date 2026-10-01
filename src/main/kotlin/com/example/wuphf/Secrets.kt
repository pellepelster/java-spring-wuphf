package com.example.wuphf

internal object Secrets {
    const val MASK = "********"

    private val SECRET_PARTS =
        listOf(
            "PASSWORD",
            "PASSWD",
            "SECRET",
            "TOKEN",
            "KEY",
            "CREDENTIAL",
            "PRIVATE",
            "AUTH",
            "SIGNATURE",
            "SALT",
            "CERT",
        )

    private val USER_INFO = Regex("(//[^/@\\s]*:)[^/@\\s]*@")

    private val SECRET_PARAMETER =
        Regex(
            "([?&][^=&\\s]*(?:password|passwd|secret|token|key|credential)[^=&\\s]*=)[^&\\s]*",
            RegexOption.IGNORE_CASE,
        )

    fun isSecretName(name: String?): Boolean {
        if (name.isNullOrBlank()) {
            return false
        }

        val upper = name.uppercase()
        return SECRET_PARTS.any { upper.contains(it) }
    }

    fun maskIfSecret(name: String?, value: String?): String =
        if (isSecretName(name)) MASK else value ?: ""

    fun maskUrl(url: String?): String {
        if (url.isNullOrBlank()) {
            return ""
        }

        val withoutUserInfo = USER_INFO.replace(url, "$1$MASK@")
        return SECRET_PARAMETER.replace(withoutUserInfo, "$1$MASK")
    }
}

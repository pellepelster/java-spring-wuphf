package com.example.wuphf

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SecretsTest {
    @ParameterizedTest
    @ValueSource(
        strings =
            [
                "DATABASE_PASSWORD",
                "password",
                "PgPassWd",
                "MY_SECRET",
                "GITHUB_TOKEN",
                "AWS_ACCESS_KEY_ID",
                "SOME_CREDENTIALS",
                "PRIVATE_THING",
                "SSH_AUTH_SOCK",
                "SIGNATURE",
                "PASSWORD_SALT",
                "CERT_PATH",
            ]
    )
    fun `names that must not show a value`(name: String) {
        Secrets.isSecretName(name).shouldBeTrue()
        Secrets.maskIfSecret(name, "hunter2") shouldBe Secrets.MASK
    }

    @ParameterizedTest
    @ValueSource(
        strings =
            [
                "PATH",
                "PWD",
                "HOME",
                "LANG",
                "JAVA_HOME",
                "DATABASE_HOST",
                "DATABASE_PORT",
                "DATABASE_USER",
            ]
    )
    fun `names that may show a value`(name: String) {
        Secrets.isSecretName(name).shouldBeFalse()
        Secrets.maskIfSecret(name, "/usr/bin") shouldBe "/usr/bin"
    }

    @Test
    fun `a name that is not there is not a secret`() {
        Secrets.isSecretName(null).shouldBeFalse()
        Secrets.isSecretName("").shouldBeFalse()
    }

    @Test
    fun `a value that is not there becomes an empty one`() {
        Secrets.maskIfSecret("PATH", null).shouldBeEmpty()
    }

    @Test
    fun `a url keeps its address`() {
        Secrets.maskUrl("jdbc:postgresql://10.0.0.42:5432/wuphf") shouldBe
            "jdbc:postgresql://10.0.0.42:5432/wuphf"
    }

    @Test
    fun `a url loses the credentials in it`() {
        val masked = Secrets.maskUrl("jdbc:postgresql://user:hunter2@db:5432/orders")

        masked shouldBe "jdbc:postgresql://user:${Secrets.MASK}@db:5432/orders"
        masked shouldNotContain "hunter2"
    }

    @Test
    fun `a url loses a secret parameter`() {
        val masked =
            Secrets.maskUrl("jdbc:postgresql://db:5432/orders?user=a&password=hunter2&ssl=true")

        masked shouldBe "jdbc:postgresql://db:5432/orders?user=a&password=${Secrets.MASK}&ssl=true"
        masked shouldNotContain "hunter2"
    }

    @Test
    fun `a url that is not there becomes an empty one`() {
        Secrets.maskUrl(null).shouldBeEmpty()
        Secrets.maskUrl("  ").shouldBeEmpty()
    }
}

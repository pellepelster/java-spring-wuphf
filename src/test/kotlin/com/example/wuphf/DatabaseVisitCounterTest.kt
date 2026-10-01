package com.example.wuphf

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties

class DatabaseVisitCounterTest {
    private fun properties(url: String) =
        DataSourceProperties().apply {
            this.url = url
            username = "wuphf_admin"
            password = "hunter2"
        }

    @Test
    fun `the description reads the address out of the url`() {
        val backend =
            DatabaseVisitCounter.describe(properties("jdbc:postgresql://10.0.0.42:5432/wuphf"))

        backend.kind shouldBe "database"
        backend.persistent.shouldBeTrue()
        backend.shared.shouldBeTrue()
        backend.details shouldContainAll
            mapOf(
                "host" to "10.0.0.42",
                "port" to "5432",
                "database" to "wuphf",
                "user" to "wuphf_admin",
                "table" to DatabaseVisitCounter.TABLE,
            )
    }

    @Test
    fun `the description holds no password`() {
        val backend =
            DatabaseVisitCounter.describe(
                properties("jdbc:postgresql://db:5432/orders?password=hunter2")
            )

        backend.details.getValue("url") shouldContain Secrets.MASK
        backend.details.getValue("url") shouldNotContain "hunter2"
        backend.toString() shouldNotContain "hunter2"
    }

    @Test
    fun `a url that cannot be read still gives a description`() {
        val backend = DatabaseVisitCounter.describe(properties("jdbc:not a url"))

        backend.kind shouldBe "database"
        backend.details shouldContainKey "url"
        backend.details shouldNotContainKey "host"
    }
}

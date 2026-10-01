package com.example.wuphf

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import javax.sql.DataSource
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.exchange
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles(ExampleApplication.MEMORY_PROFILE)
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MemoryIntegrationTest(
    @Autowired private val http: TestRestTemplate,
    @Autowired private val context: ApplicationContext,
) {
    private fun visitAsJson(): VisitReport {
        val headers = HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_JSON) }
        val response = http.exchange<VisitReport>("/", HttpMethod.GET, HttpEntity<Any>(headers))

        response.statusCode.is2xxSuccessful.shouldBeTrue()
        response.headers.contentType shouldBe MediaType.APPLICATION_JSON
        return response.body.shouldNotBeNull()
    }

    @Test
    fun `the application starts without a database`() {
        context.getBeansOfType(DataSource::class.java).shouldBeEmpty()
        context.getBean(VisitCounter::class.java).shouldBeInstanceOf<InMemoryVisitCounter>()
    }

    @Test
    fun `a json request gets the count kept in memory`() {
        val before = visitAsJson().visited
        val report = visitAsJson()

        report.visited shouldBe before + 1
        report.storage.kind shouldBe "memory"
        report.storage.persistent.shouldBeFalse()
    }

    @Test
    fun `a browser gets the page`() {
        val headers =
            HttpHeaders().apply {
                set(
                    HttpHeaders.ACCEPT,
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                )
            }
        val response = http.exchange<String>("/", HttpMethod.GET, HttpEntity<Any>(headers))

        response.headers.contentType.shouldNotBeNull().isCompatibleWith(MediaType.TEXT_HTML).shouldBeTrue()
        val page = response.body.shouldNotBeNull()
        page shouldStartWith "<!doctype html>"
        page shouldContain "<span class=\"badge badge-memory\">In memory</span>"
    }
}

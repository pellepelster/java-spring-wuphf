package com.example.wuphf

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType

class VisitCounterControllerTest {
    private val controller =
        VisitCounterController(
            StubVisitCounter(),
            ProcessEnvironment(mapOf("PATH" to "/usr/bin")),
            wuphfPage(),
        )

    private class StubVisitCounter : VisitCounter {
        private var visits = 0L

        override fun recordVisit() = ++visits

        override fun storage() = BACKEND
    }

    @Test
    fun `an explicit json request gets the report`() {
        val response = controller.visit(MediaType.APPLICATION_JSON_VALUE)

        response.headers.contentType shouldBe MediaType.APPLICATION_JSON
        val report = response.body.shouldBeInstanceOf<VisitReport>()
        report.visited shouldBe 1
        report.storage shouldBe BACKEND
        report.environment.other.shouldNotBeEmpty()
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings =
            [
                "*/*",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "text/html",
                "application",
            ]
    )
    fun `everything else gets the page`(accept: String?) {
        val response = controller.visit(accept)

        response.headers.contentType shouldBe MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)
        response.body.shouldBeInstanceOf<String>() shouldStartWith "<!doctype html>"
    }

    @Test
    fun `a json request that also takes anything else gets json`() {
        controller.visit("application/json, */*").body.shouldBeInstanceOf<VisitReport>()
    }

    @Test
    fun `every request counts one visit`() {
        controller.visit(null)
        controller.visit(null)

        controller.visit(MediaType.APPLICATION_JSON_VALUE).body.shouldBeInstanceOf<VisitReport>()
            .visited shouldBe 3
    }

    private companion object {
        val BACKEND = StorageBackend("memory", "In memory", "kept here", false, false, emptyMap())
    }
}

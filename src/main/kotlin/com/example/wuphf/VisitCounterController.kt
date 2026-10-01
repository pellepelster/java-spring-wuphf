package com.example.wuphf

import java.nio.charset.StandardCharsets
import org.springframework.http.HttpHeaders
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class VisitCounterController(
    private val visits: VisitCounter,
    private val environment: ProcessEnvironment,
    private val page: WuphfPage,
) {
    @GetMapping("/")
    fun visit(@RequestHeader(value = HttpHeaders.ACCEPT, required = false) accept: String?): ResponseEntity<Any> {
        val report = VisitReport(visits.recordVisit(), visits.storage(), environment.report())

        if (wantsJson(accept)) {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(report)
        }

        return ResponseEntity.ok()
            .contentType(MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
            .body(page.render(report))
    }

    private companion object {
        fun wantsJson(accept: String?): Boolean {
            if (accept.isNullOrBlank()) {
                return false
            }

            val accepted =
                try {
                    MediaType.parseMediaTypes(accept)
                } catch (e: InvalidMediaTypeException) {
                    return false
                }

            return accepted
                .filter { !it.isWildcardType && !it.isWildcardSubtype }
                .any { MediaType.APPLICATION_JSON.equalsTypeAndSubtype(it) }
        }
    }
}

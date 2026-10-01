package com.example.wuphf

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import java.util.concurrent.Executors
import org.junit.jupiter.api.Test

class InMemoryVisitCounterTest {
    @Test
    fun `every visit counts one`() {
        val counter = InMemoryVisitCounter()

        counter.recordVisit() shouldBe 1
        counter.recordVisit() shouldBe 2
    }

    @Test
    fun `visits at the same time are all counted`() {
        val counter = InMemoryVisitCounter()

        Executors.newFixedThreadPool(8).use { pool -> repeat(1000) { pool.submit { counter.recordVisit() } } }

        counter.recordVisit() shouldBe 1001
    }

    @Test
    fun `the backend says the count is neither kept nor shared`() {
        val backend = InMemoryVisitCounter().storage()

        backend.kind shouldBe "memory"
        backend.persistent.shouldBeFalse()
        backend.shared.shouldBeFalse()
        backend.details.shouldBeEmpty()
    }
}

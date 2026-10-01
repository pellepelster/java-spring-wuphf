package com.example.wuphf

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class ExampleApplicationTest {
    @Test
    fun `a linked database keeps the visits`() {
        ExampleApplication.profile("10.0.0.42") shouldBe ExampleApplication.DATABASE_PROFILE
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["  ", "\t"])
    fun `without a database the visits stay in the process`(host: String?) {
        ExampleApplication.profile(host) shouldBe ExampleApplication.MEMORY_PROFILE
    }
}

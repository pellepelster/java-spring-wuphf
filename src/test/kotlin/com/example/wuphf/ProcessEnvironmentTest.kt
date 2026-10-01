package com.example.wuphf

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

class ProcessEnvironmentTest {
    private fun report(environment: Map<String, String>) = ProcessEnvironment(environment).report()

    @Test
    fun `the database variables are one group and the rest another`() {
        val report =
            report(
                mapOf(
                    "DATABASE_HOST" to "10.0.0.42",
                    "DATABASE_PORT" to "5432",
                    "PATH" to "/usr/bin",
                    "HOME" to "/opt/visits",
                )
            )

        report.database.map { it.name } shouldContainExactly
            listOf("DATABASE_HOST", "DATABASE_PORT")
        report.other.map { it.name } shouldContainExactly listOf("HOME", "PATH")
    }

    @Test
    fun `each group is sorted whatever order the environment had`() {
        val environment = linkedMapOf("zebra" to "1", "Apple" to "2", "mango" to "3")

        report(environment).other.map { it.name } shouldContainExactly
            listOf("Apple", "mango", "zebra")
    }

    @Test
    fun `a secret value is replaced and the name stays`() {
        val report = report(mapOf(PASSWORD to "hunter2"))

        report.database shouldHaveSize 1
        val variable = report.database.single()
        variable.name shouldBe PASSWORD
        variable.value shouldBe Secrets.MASK
        variable.secret.shouldBeTrue()
    }

    @Test
    fun `a secret value is nowhere in the report`() {
        report(mapOf(PASSWORD to "hunter2", "PATH" to "/usr/bin")).toString() shouldNotContain
            "hunter2"
    }

    @Test
    fun `an empty environment gives two empty groups`() {
        val report = report(emptyMap())

        report.database.shouldBeEmpty()
        report.other.shouldBeEmpty()
    }

    private companion object {
        const val PASSWORD = "DATABASE_PASSWORD"
    }
}

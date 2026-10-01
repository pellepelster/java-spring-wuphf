package com.example.wuphf

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@ActiveProfiles(ExampleApplication.DATABASE_PROFILE)
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DatabaseIntegrationTest(
    @Autowired private val http: TestRestTemplate,
    @Autowired private val jdbc: JdbcTemplate,
    @Autowired private val counter: VisitCounter,
) {
    @BeforeEach
    fun `no visits yet`() {
        jdbc.update("DELETE FROM ${DatabaseVisitCounter.TABLE}")
    }

    private fun visit(accept: String): String {
        val headers = HttpHeaders().apply { set(HttpHeaders.ACCEPT, accept) }
        return http.exchange<String>("/", HttpMethod.GET, HttpEntity<Any>(headers)).body.shouldNotBeNull()
    }

    private fun visitAsJson(): VisitReport {
        val headers = HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_JSON) }
        val response = http.exchange<VisitReport>("/", HttpMethod.GET, HttpEntity<Any>(headers))

        response.statusCode.is2xxSuccessful.shouldBeTrue()
        return response.body.shouldNotBeNull()
    }

    private fun rows() = jdbc.queryForObject("SELECT count(*) FROM visits", Long::class.java)

    @Test
    fun `the linked database keeps the visits`() {
        counter.shouldBeInstanceOf<DatabaseVisitCounter>()
    }

    @Test
    fun `liquibase made the table the visits go to`() {
        jdbc.queryForObject(
            "SELECT count(*) FROM information_schema.tables WHERE table_name = ?",
            Long::class.java,
            DatabaseVisitCounter.TABLE,
        ) shouldBe 1
    }

    @Test
    fun `every visit is a row in the database`() {
        visitAsJson()
        visitAsJson()

        visitAsJson().visited shouldBe 3
        rows() shouldBe 3
    }

    @Test
    fun `the count is what the table has and not what this process counted`() {
        jdbc.update("INSERT INTO visits (visited_at) VALUES (now()), (now())")

        visitAsJson().visited shouldBe 3
    }

    @Test
    fun `the report describes the linked database`() {
        val storage = visitAsJson().storage

        storage.kind shouldBe "database"
        storage.persistent.shouldBeTrue()
        storage.shared.shouldBeTrue()
        storage.details shouldContainAll
            mapOf(
                "host" to postgres.host,
                "port" to postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT).toString(),
                "database" to DATABASE,
                "user" to USER,
                "table" to DatabaseVisitCounter.TABLE,
            )
    }

    @Test
    fun `neither the json nor the page shows the password`() {
        visit(MediaType.APPLICATION_JSON_VALUE) shouldNotContain PASSWORD

        val page = visit(MediaType.TEXT_HTML_VALUE)
        page shouldContain "<span class=\"badge badge-database\">PostgreSQL</span>"
        page shouldNotContain PASSWORD
    }

    companion object {
        private const val DATABASE = "wuphf"
        private const val USER = "wuphf_admin"
        private const val PASSWORD = "hunter2"

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer =
            PostgreSQLContainer("postgres:17-alpine")
                .withDatabaseName(DATABASE)
                .withUsername(USER)
                .withPassword(PASSWORD)

        @DynamicPropertySource
        @JvmStatic
        fun database(registry: DynamicPropertyRegistry) {
            registry.add("DATABASE_HOST", postgres::getHost)
            registry.add("DATABASE_PORT") {
                postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)
            }
            registry.add("DATABASE_NAME") { DATABASE }
            registry.add("DATABASE_USER") { USER }
            registry.add("DATABASE_PASSWORD") { PASSWORD }
        }
    }
}

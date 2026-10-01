package com.example.wuphf

import java.net.URI
import java.sql.Timestamp
import java.time.Instant
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile(ExampleApplication.DATABASE_PROFILE)
class DatabaseVisitCounter(private val jdbc: JdbcTemplate, dataSource: DataSourceProperties) :
    VisitCounter {
    private val storage = describe(dataSource)

    @Transactional
    override fun recordVisit(): Long {
        jdbc.update("INSERT INTO visits (visited_at) VALUES (?)", Timestamp.from(Instant.now()))
        return jdbc.queryForObject("SELECT count(*) FROM visits", Long::class.java) ?: 0
    }

    override fun storage() = storage

    companion object {
        const val TABLE = "visits"

        fun describe(properties: DataSourceProperties): StorageBackend {
            val url = properties.url
            val details = linkedMapOf<String, String>()

            address(url)?.let { address ->
                details["host"] = address.host
                if (address.port != -1) {
                    details["port"] = address.port.toString()
                }
                details["database"] = address.path.removePrefix("/")
            }

            details["user"] = properties.username ?: ""
            details["url"] = Secrets.maskUrl(url)
            details["table"] = TABLE

            return StorageBackend(
                kind = "database",
                name = "PostgreSQL",
                description =
                    "One row for each visit in the linked postgres database, thus the count stays" +
                        " after a restart and all instances count together.",
                persistent = true,
                shared = true,
                details = details.toMap(),
            )
        }

        private fun address(url: String?): URI? {
            if (url == null || !url.startsWith("jdbc:")) {
                return null
            }

            return try {
                URI.create(url.removePrefix("jdbc:")).takeIf { it.host != null && it.path != null }
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

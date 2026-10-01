package com.example.wuphf

import java.util.concurrent.atomic.AtomicLong
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ExampleApplication.MEMORY_PROFILE)
class InMemoryVisitCounter : VisitCounter {
    private val visits = AtomicLong()

    override fun recordVisit() = visits.incrementAndGet()

    override fun storage() = STORAGE

    private companion object {
        val STORAGE =
            StorageBackend(
                kind = "memory",
                name = "In memory",
                description =
                    "No database is linked, thus the count stays in this process. A restart starts" +
                        " it again at zero, and two instances count separately.",
                persistent = false,
                shared = false,
                details = emptyMap(),
            )
    }
}

package com.example.wuphf

data class VisitReport(
    val visited: Long,
    val storage: StorageBackend,
    val environment: EnvironmentReport,
)

package com.example.wuphf

interface VisitCounter {
    fun recordVisit(): Long

    fun storage(): StorageBackend
}

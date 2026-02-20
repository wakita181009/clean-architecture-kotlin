package com.wakita181009.cleanarchitecture.domain.valueobject

data class Page<T>(
    val totalCount: Int,
    val items: List<T>,
)

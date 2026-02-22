package com.wakita181009.cleanarchitecture.application.query.dto

data class PageDto<T>(
    val totalCount: Int,
    val items: List<T>,
)

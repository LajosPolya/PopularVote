package com.github.lajospolya.popularVote.dto

data class PageDto<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val pageNumber: Int,
    val pageSize: Int,
)

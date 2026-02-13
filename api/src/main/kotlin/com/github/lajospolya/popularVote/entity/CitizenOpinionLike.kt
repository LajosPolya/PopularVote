package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("citizen_opinion_like")
data class CitizenOpinionLike(
    val citizenId: Long,
    val opinionId: Long,
) : Persistable<String> {
    @Transient
    private var isNew: Boolean = true

    override fun getId(): String = "${citizenId}_${opinionId}"
    override fun isNew(): Boolean = isNew

    fun markNotNew(): CitizenOpinionLike {
        this.isNew = false
        return this
    }
}

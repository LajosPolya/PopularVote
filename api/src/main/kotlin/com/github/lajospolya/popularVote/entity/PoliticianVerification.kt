package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table

/**
 * Must implement Persistable interface to be used with Spring Data R2DBC because the table only contains one ID column
 * which is not a primary key
 */
@Table("politician_verification")
data class PoliticianVerification(
    @Id
    private val citizenId: Long,
    @Transient
    private val isNewRecord: Boolean = true,
) : org.springframework.data.domain.Persistable<Long> {
    override fun getId(): Long = citizenId

    override fun isNew(): Boolean = isNewRecord
}

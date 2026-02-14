package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table

@Table("policy_co_author_citizen")
data class PolicyCoAuthorCitizen(
    val policyId: Long,
    val citizenId: Long,
) : Persistable<String> {
    @Transient
    private var isNew: Boolean = true

    override fun getId(): String = "${policyId}_$citizenId"

    override fun isNew(): Boolean = isNew

    fun markNotNew(): PolicyCoAuthorCitizen {
        this.isNew = false
        return this
    }
}

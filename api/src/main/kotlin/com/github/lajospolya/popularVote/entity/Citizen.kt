package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id

data class Citizen(
    @Id
    val id: Long? = null,
    val givenName: String,
    val surname: String,
    val middleName: String?,
    val politicalPartyId: Int,
    val role: Role? = null,
    val authId: String,
) {
    val fullName: String
        get() = "$givenName $surname"
}

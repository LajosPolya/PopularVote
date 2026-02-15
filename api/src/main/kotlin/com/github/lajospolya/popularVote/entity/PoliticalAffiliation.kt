package com.github.lajospolya.popularVote.entity

enum class PoliticalAffiliation(
    val id: Int,
) {
    LIBERAL_PARTY_OF_CANADA(1),
    CONSERVATIVE_PARTY_OF_CANADA(2),
    BLOC_QUEBECOIS(3),
    NEW_DEMOCRATIC_PARTY(4),
    GREEN_PARTY_OF_CANADA(5),
    INDEPENDENT(6),
    PROGRESSIVE_CONSERVATIVE_OF_CANADA(7),
    ONTARIO_NEW_DEMOCRATIC_PARTY(8),
    ONTARIO_LIBERAL_PARTY(9),
    GREEN_PARTY_OF_ONTARIO(10),
    INDEPENDENT_PROVINCIAL(11),
    ;

    companion object {
        fun fromId(id: Int): PoliticalAffiliation =
            entries.find { it.id == id } ?: throw IllegalArgumentException("Unknown PoliticalAffiliation id: $id")
    }
}

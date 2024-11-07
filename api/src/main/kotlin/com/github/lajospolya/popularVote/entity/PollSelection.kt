package com.github.lajospolya.popularVote.entity

import org.springframework.data.annotation.Id

data class PollSelection(
    @Id
    var id: Long,
    var selection: String,
)

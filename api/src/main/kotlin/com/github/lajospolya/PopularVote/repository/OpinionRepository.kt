package com.github.lajospolya.PopularVote.repository

import com.github.lajospolya.PopularVote.entity.Opinion
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OpinionRepository: ReactiveCrudRepository<Opinion, Long>
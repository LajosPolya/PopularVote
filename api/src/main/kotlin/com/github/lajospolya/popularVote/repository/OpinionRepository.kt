package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Opinion
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface OpinionRepository : ReactiveCrudRepository<Opinion, Long>

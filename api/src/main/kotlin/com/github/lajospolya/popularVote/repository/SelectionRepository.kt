package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PollSelection
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface SelectionRepository: ReactiveCrudRepository<PollSelection, Long>
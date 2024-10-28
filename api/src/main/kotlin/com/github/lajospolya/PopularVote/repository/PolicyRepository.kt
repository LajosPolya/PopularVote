package com.github.lajospolya.PopularVote.repository

import com.github.lajospolya.PopularVote.entity.Policy
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PolicyRepository: ReactiveCrudRepository<Policy, Long>
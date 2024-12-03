package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.Citizen
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CitizenRepository : ReactiveCrudRepository<Citizen, Long>

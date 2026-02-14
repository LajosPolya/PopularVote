package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.CitizenPoliticalDetails
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CitizenPoliticalDetailsRepository : ReactiveCrudRepository<CitizenPoliticalDetails, Long>

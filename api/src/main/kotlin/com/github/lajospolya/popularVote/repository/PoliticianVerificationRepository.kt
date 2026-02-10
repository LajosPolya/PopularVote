package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PoliticianVerification
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PoliticianVerificationRepository : ReactiveCrudRepository<PoliticianVerification, Long>

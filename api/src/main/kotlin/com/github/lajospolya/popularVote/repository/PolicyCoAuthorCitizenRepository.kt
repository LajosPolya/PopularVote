package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.PolicyCoAuthorCitizen
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface PolicyCoAuthorCitizenRepository : ReactiveCrudRepository<PolicyCoAuthorCitizen, String> {
    fun findByPolicyId(policyId: Long): Flux<PolicyCoAuthorCitizen>
    fun findByCitizenId(citizenId: Long): Flux<PolicyCoAuthorCitizen>
}

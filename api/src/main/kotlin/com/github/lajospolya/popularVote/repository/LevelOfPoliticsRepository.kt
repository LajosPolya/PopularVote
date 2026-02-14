package com.github.lajospolya.popularVote.repository

import com.github.lajospolya.popularVote.entity.LevelOfPolitics
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LevelOfPoliticsRepository : ReactiveCrudRepository<LevelOfPolitics, Int>

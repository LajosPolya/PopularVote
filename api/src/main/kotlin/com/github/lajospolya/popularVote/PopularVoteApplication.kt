package com.github.lajospolya.popularVote

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PopularVoteApplication

fun main(args: Array<String>) {
    runApplication<PopularVoteApplication>(*args)
}

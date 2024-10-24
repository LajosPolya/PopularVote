package com.github.lajospolya.PopularVote

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PopularVoteApplication

fun main(args: Array<String>) {
	runApplication<PopularVoteApplication>(*args)
}

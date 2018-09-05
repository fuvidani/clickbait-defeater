package com.clickbait.defeater.clickbaitservice.update.service

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ClickBaitVoteService {

    fun submitVote(vote: ClickBaitVote): Mono<Void>

    fun findVote(vote: ClickBaitVote): Mono<ClickBaitVote>

    fun findAllUserVotes(userId: String, pageable: Pageable): Flux<ClickBaitVote>
}
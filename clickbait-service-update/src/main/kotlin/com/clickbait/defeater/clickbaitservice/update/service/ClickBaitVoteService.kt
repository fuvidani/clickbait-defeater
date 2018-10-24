package com.clickbait.defeater.clickbaitservice.update.service

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for the operations concerning the clickbait voting.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ClickBaitVoteService {

    /**
     * Handles a new vote or updates the existing one.
     *
     * @param vote a user vote on the clickbait feature of a
     * particular web page
     * @return an empty Mono
     */
    fun submitVote(vote: ClickBaitVote): Mono<Void>

    /**
     * Tries to retrieve an existing clickbait vote that
     * is equal to the parameter `vote`.
     *
     * @param vote the user vote to look for
     * @return a Mono emitting the vote or an empty Mono if
     * it could not be found
     */
    fun findVote(vote: ClickBaitVote): Mono<ClickBaitVote>

    /**
     * Retrieves all the clickbait votes of a user and returns them
     * in a Flux.
     *
     * @param userId unique ID of the user for which all votes should
     * be found
     * @param pageable the pagination information; use [Pageable.unpaged]
     * if no pagination should be applied
     * @return Flux emitting all the [ClickBaitVote] objects with the
     * provided `userId`. Flux may be empty if no votes for the user exist.
     */
    fun findAllUserVotes(userId: String, pageable: Pageable): Flux<ClickBaitVote>
}
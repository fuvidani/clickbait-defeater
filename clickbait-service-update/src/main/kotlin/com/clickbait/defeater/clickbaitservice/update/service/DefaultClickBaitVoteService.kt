/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.clickbaitservice.update.service

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.persistence.ClickBaitVoteRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.PostInstanceService
import mu.KLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Concrete implementation of the [ClickBaitVoteService] interface,
 * fulfilling its contracts.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property voteRepository a repository for [ClickBaitVoteEntity]
 * @property postInstanceService an implementation of [PostInstanceService]
 * to manage [PostInstance] objects corresponding to votes
 */
@Component
class DefaultClickBaitVoteService(
    private val voteRepository: ClickBaitVoteRepository,
    private val postInstanceService: PostInstanceService
) : ClickBaitVoteService {

    /**
     * Handles a new vote or updates the existing one.
     *
     * @param vote a user vote on the clickbait feature of a
     * particular web page
     * @return an empty Mono
     */
    override fun submitVote(vote: ClickBaitVote): Mono<Void> {
        return voteRepository
            .save(vote.toEntity())
            .then(postInstanceService.ensurePersistedPostInstance(vote))
            .then()
    }

    /**
     * Tries to retrieve an existing clickbait vote that
     * is equal to the parameter `vote`.
     *
     * @param vote the user vote to look for
     * @return a Mono emitting the vote or an empty Mono if
     * it could not be found
     */
    override fun findVote(vote: ClickBaitVote): Mono<ClickBaitVote> {
        return voteRepository
            .findById(ClickBaitVoteKey(vote.userId, vote.url))
            .map { it.toModel() }
    }

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
    override fun findAllUserVotes(userId: String, pageable: Pageable): Flux<ClickBaitVote> {
        return voteRepository
            .findByIdUserId(userId, pageable)
            .map { it.toModel() }
    }

    companion object : KLogging()
}

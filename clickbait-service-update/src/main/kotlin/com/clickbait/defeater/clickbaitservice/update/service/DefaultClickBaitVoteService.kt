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
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DefaultClickBaitVoteService(
    private val voteRepository: ClickBaitVoteRepository,
    private val postInstanceService: PostInstanceService
) : ClickBaitVoteService {

    override fun submitVote(vote: ClickBaitVote): Mono<Void> {
        return voteRepository
            .save(vote.toEntity())
            .then(postInstanceService.ensurePersistedPostInstance(vote))
            .then()
    }

    override fun findVote(vote: ClickBaitVote): Mono<ClickBaitVote> {
        return voteRepository
            .findById(ClickBaitVoteKey(vote.userId, vote.url))
            .map { it.toModel() }
    }

    override fun findAllUserVotes(userId: String, pageable: Pageable): Flux<ClickBaitVote> {
        return voteRepository
            .findByIdUserId(userId, pageable)
            .map { it.toModel() }
    }

    companion object : KLogging()
}

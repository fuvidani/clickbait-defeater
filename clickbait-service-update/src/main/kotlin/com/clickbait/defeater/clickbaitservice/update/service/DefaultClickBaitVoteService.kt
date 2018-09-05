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

    /*@Scheduled(cron = "\${service.relay.votes.cron}")
    override fun relayGatheredVotes() {
        val yesterday = Instant.now().minus(Duration.ofHours(24))
        voteRepository
            .findByLastUpdateAfter(yesterday)
            .publishOn(Schedulers.parallel())
            .groupBy { it.id.url }
            .flatMap { group -> group.collectList() }
            .filter { it.size >= 5 }
            .flatMap { postInstanceService.findById(it[0].id.url).zipWith(Mono.just(it)) }
            .map {
                val votes = it.t2
                val post = it.t1
                val stats = PostInstanceJudgmentStats(
                    post.id,
                    votes.map { vote -> vote.vote },
                    0.0,
                    0.0,
                    0.0,
                    CLASS_NO_CLICKBAIT
                )
                val wrapper = PostInstanceJudgments(post, stats)
                logger.info("Post Instance judgement stats: $wrapper")
                wrapper
            }
            .collectList()
            .map { MultiplePostInstanceJudgments(it) }
            .subscribeOn(Schedulers.parallel())
            .subscribe {
                if (it.judgments.isEmpty()) {
                    logger.info("No judgment stats to store")
                } else {
                    logger.info("${it.judgments.size} judgments to store. \n${it.judgments}")
                    judgmentsRepository
                        .saveAll(it)
                        .doOnError { error -> logger.error(error) { "Judgments could not be stored" } }
                        .subscribe()
                }
            }
    }*/

    companion object : KLogging()
}

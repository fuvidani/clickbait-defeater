package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.persistence.ClickBaitVoteRepository
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components.JudgmentsAggregator
import com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components.JudgmentsPersistenceHandler
import com.clickbait.defeater.clickbaitservice.update.service.post.PostInstanceService
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.function.Tuple2
import java.time.Duration
import java.time.Instant

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
class JudgmentsPersistenceScheduler(
    private val voteRepository: ClickBaitVoteRepository,
    private val postInstanceService: PostInstanceService,
    private val schedulerProperties: SchedulerProperties,
    judgmentsRepository: JudgmentsRepository
) {

    private val judgmentsAggregator = JudgmentsAggregator()
    private val judgmentsPersistenceHandler = JudgmentsPersistenceHandler(judgmentsRepository)

    @Scheduled(cron = "\${service.relay.votes.cron}")
    fun persistVotesToJudgmentRepository() {
        logger.info("Begin scheduled persist vote task")
        val yesterday = Instant.now().minus(Duration.ofHours(schedulerProperties.hoursToConsiderUntilNow.toLong()))
        voteRepository
            .findByLastUpdateAfter(yesterday)
            .publishOn(Schedulers.elastic())
            .map { entity -> entity.toModel() }
            .groupBy { it.url }
            .flatMap { group -> group.collectList() }
            .filter { votes -> votes.size >= schedulerProperties.minNumberOfVotes }
            .flatMap { votes -> getCorrespondingPostInstanceOf(votes[0]).zipWith(Mono.just(votes)) }
            .filter { it.t1.language.contains("en") }
            .map { obtainPostInstanceJudgments(it) }
            .collectList()
            .map { listOfAggregatedJudgments -> MultiplePostInstanceJudgments(listOfAggregatedJudgments) }
            .subscribeOn(Schedulers.elastic())
            .subscribe { judgmentsPersistenceHandler.persist(it) }
    }

    private fun getCorrespondingPostInstanceOf(vote: ClickBaitVote): Mono<PostInstance> {
        return postInstanceService.findById(vote.url)
    }

    private fun obtainPostInstanceJudgments(tuple: Tuple2<PostInstance, List<ClickBaitVote>>): PostInstanceJudgments {
        val post = tuple.t1
        val votes = tuple.t2
        val stats = judgmentsAggregator.aggregate(votes)
        val result = PostInstanceJudgments(post, mapAggregatorResultToObject(post, stats))
        logger.info("Post Instance judgement stats: $result")
        return result
    }

    private fun mapAggregatorResultToObject(
        post: PostInstance,
        stats: JudgmentsAggregator.JudgmentStats
    ): PostInstanceJudgmentStats {
        return PostInstanceJudgmentStats(
            post.id,
            stats.truthJudgments,
            stats.truthMean,
            stats.truthMedian,
            stats.truthMode,
            stats.truthClass
        )
    }

    companion object : KLogging()
}
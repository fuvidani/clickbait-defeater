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

package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.persistence.ClickBaitVoteRepository
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
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
 * Component for time-scheduled tasks. This scheduler has the responsibility
 * (i) to periodically aggregate the votes for a certain time window,
 * (ii) to process them and (iii) to delegate them to the their adequate
 * repository.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property voteRepository a repository for [ClickBaitVoteEntity]
 * @property postInstanceService an implementation of the [PostInstanceService]
 * interface
 * @property schedulerProperties properties specific to the scheduled task
 * @property judgmentsPersistenceHandler handler for the persistence of
 * aggregated votes
 * @param judgmentsRepository an implementation of [JudgmentsRepository]
 */
@Component
class JudgmentsPersistenceScheduler(
    private val voteRepository: ClickBaitVoteRepository,
    private val postInstanceService: PostInstanceService,
    private val schedulerProperties: SchedulerProperties,
    judgmentsRepository: JudgmentsRepository
) {

    private val judgmentsPersistenceHandler = JudgmentsPersistenceHandler(judgmentsRepository)

    /**
     * Collects all recent votes for which it holds:
     * - its corresponding [PostInstance] object has English language
     * - there are at least `minNumberOfVotes`(specified by [schedulerProperties])
     * for an occurring URL
     * The votes are then aggregated and delegated to the [judgmentsPersistenceHandler].
     *
     * This method is periodically called by the framework, therefore it is neither needed
     * nor intended for manual invocations (except for test cases of course). The time
     * between invocations is specified by a cron pattern injected from the application's
     * properties.
     */
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
        val result = PostInstanceJudgments(post, PostInstanceJudgmentStats(post.id, votes.map { it.vote }))
        logger.info("Post Instance judgement stats: $result")
        return result
    }

    companion object : KLogging()
}
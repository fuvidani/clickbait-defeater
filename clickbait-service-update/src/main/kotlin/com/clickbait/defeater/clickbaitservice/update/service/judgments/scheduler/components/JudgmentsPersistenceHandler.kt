package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components

import com.clickbait.defeater.clickbaitservice.update.model.MultiplePostInstanceJudgments
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import mu.KLogging
import reactor.core.scheduler.Schedulers

/**
 * Handler for persisting [MultiplePostInstanceJudgments] objects during
 * a scheduled task. This is strictly an internal component of the
 * [com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.JudgmentsPersistenceScheduler]
 * and is therefore not intended for outside use.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property judgmentsRepository an implementation of [JudgmentsRepository]
 */
internal class JudgmentsPersistenceHandler(private val judgmentsRepository: JudgmentsRepository) {

    /**
     * Tries to persist the provided `judgmentsWrapper` in the background.
     * If `judgmentsWrapper` contains an empty list of judgments, the
     * operation doesn't do anything.
     *
     * @param judgmentsWrapper wrapper object around multiple
     * [com.clickbait.defeater.clickbaitservice.update.model.PostInstanceJudgments]
     */
    internal fun persist(judgmentsWrapper: MultiplePostInstanceJudgments) {
        if (judgmentsWrapper.judgments.isEmpty()) {
            logger.info("No judgment stats to store")
        } else {
            logger.info("${judgmentsWrapper.judgments.size} judgmentsWrapper to store.")
            judgmentsRepository
                .saveAll(judgmentsWrapper)
                .doOnError { error -> logger.error(error) { "Judgments could not be stored" } }
                .subscribeOn(Schedulers.elastic())
                .subscribe()
        }
    }

    companion object : KLogging()
}
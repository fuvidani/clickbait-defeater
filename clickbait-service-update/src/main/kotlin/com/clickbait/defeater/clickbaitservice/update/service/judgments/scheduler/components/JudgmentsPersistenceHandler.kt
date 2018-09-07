package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components

import com.clickbait.defeater.clickbaitservice.update.model.MultiplePostInstanceJudgments
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import mu.KLogging
import reactor.core.scheduler.Schedulers

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JudgmentsPersistenceHandler(private val judgmentsRepository: JudgmentsRepository) {

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
package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

/**
 * Class encapsulating the configurable properties of
 * the [JudgmentsPersistenceScheduler] component.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property hoursToConsiderUntilNow the size of the considered time-window
 * in hours; For example the value 5 means the time-window from the last
 * 5 hours up until now will be considered; the value 24 implies the last
 * 24 hours
 * @property minNumberOfVotes the minimum number of votes a web page's URL
 * should have before making it into the judgments persistence
 */
data class SchedulerProperties(
    val hoursToConsiderUntilNow: Int,
    val minNumberOfVotes: Int
)
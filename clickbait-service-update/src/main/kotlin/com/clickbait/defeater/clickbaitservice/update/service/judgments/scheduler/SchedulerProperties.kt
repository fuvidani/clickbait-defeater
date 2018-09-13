package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
data class SchedulerProperties(
    val hoursToConsiderUntilNow: Int,
    val minNumberOfVotes: Int
)
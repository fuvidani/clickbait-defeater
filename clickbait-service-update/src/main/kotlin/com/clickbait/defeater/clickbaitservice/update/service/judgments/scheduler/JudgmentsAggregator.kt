package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

import com.clickbait.defeater.clickbaitservice.update.model.CLASS_NO_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstanceJudgmentStats

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
internal class JudgmentsAggregator {

    internal fun aggregate(votes: List<ClickBaitVote>): PostInstanceJudgmentStats {
        return PostInstanceJudgmentStats(votes[0].url, votes.map { it.vote }, 0.0, 0.0, 0.0, CLASS_NO_CLICKBAIT)
    }
}
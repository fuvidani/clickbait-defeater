package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

import com.clickbait.defeater.clickbaitservice.update.model.CLASS_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.CLASS_NO_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstanceJudgmentStats
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.descriptive.rank.Median

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
        val voteValues = votes.map { it.vote }
        val array = voteValues.toDoubleArray()
        val median = getMedian(array)
        val label = getLabel(median)
        return PostInstanceJudgmentStats(votes[0].url, voteValues, getMean(array), median, getMode(array), label)
    }

    private fun getMean(values: DoubleArray): Double {
        return DescriptiveStatistics(values).mean
    }

    private fun getMedian(values: DoubleArray): Double {
        return Median().evaluate(values)
    }

    private fun getMode(values: DoubleArray): Double {
        return StatUtils.mode(values)[0]
    }

    private fun getLabel(median: Double): String {
        return if (median > 0.5) {
            CLASS_CLICKBAIT
        } else {
            CLASS_NO_CLICKBAIT
        }
    }
}
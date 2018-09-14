package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components

import com.clickbait.defeater.clickbaitservice.update.model.CLASS_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.CLASS_NO_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner
import kotlin.Double.Companion.NaN

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringRunner::class)
class JudgmentsAggregatorTest {

    private val aggregator = JudgmentsAggregator()

    @Test
    fun `GIVEN an empty list of votes, THEN the aggregator returns NaN stats object`() {
        val stats = aggregator.aggregate(emptyList())
        Assert.assertEquals(JudgmentsAggregator.JudgmentStats(emptyList(), NaN, NaN, NaN, CLASS_NO_CLICKBAIT), stats)
    }

    @Test
    fun `GIVEN a list of invalid values, THEN aggregator returns NaN stats object`() {
        val votes = getListOfVotes(NaN, NaN, NaN, NaN, NaN)
        val expectedStats = JudgmentsAggregator.JudgmentStats(votes.map { it.vote }, NaN, NaN, NaN, CLASS_NO_CLICKBAIT)
        val actualStats = aggregator.aggregate(votes)

        assertStatsEqual(expectedStats, actualStats)
    }

    @Test
    fun `GIVEN a list of genuine votes, THEN aggregator returns correctly calculated results`() {
        val votes = getListOfVotes(0.0, 0.0, 0.0, 0.0, 0.0)
        val expectedStats = JudgmentsAggregator.JudgmentStats(votes.map { it.vote }, 0.0, 0.0, 0.0, CLASS_NO_CLICKBAIT)
        val actualStats = aggregator.aggregate(votes)

        assertStatsEqual(expectedStats, actualStats)
    }

    @Test
    fun `GIVEN a list of genuine votes, THEN aggregator returns correctly calculated results 2`() {
        val votes = getListOfVotes(0.0, 0.6666667, 0.0, 0.33333334, 0.0)
        val expectedStats = JudgmentsAggregator.JudgmentStats(votes.map { it.vote }, 0.2, 0.0, 0.0, CLASS_NO_CLICKBAIT)
        val actualStats = aggregator.aggregate(votes)

        assertStatsEqual(expectedStats, actualStats)
    }

    @Test
    fun `GIVEN a list of genuine votes, THEN aggregator returns correctly calculated results 3`() {
        val votes = getListOfVotes(0.0, 1.0, 0.33333334, 0.6666667, 0.33333334)
        val expectedStats = JudgmentsAggregator.JudgmentStats(
            votes.map { it.vote },
            0.46666667,
            0.33333334,
            0.33333334,
            CLASS_NO_CLICKBAIT
        )
        val actualStats = aggregator.aggregate(votes)

        assertStatsEqual(expectedStats, actualStats)
    }

    @Test
    fun `GIVEN a list of genuine votes, THEN aggregator returns correctly calculated results 4`() {
        val votes = getListOfVotes(1.0, 0.6666667, 1.0, 0.0, 1.0)
        val expectedStats =
            JudgmentsAggregator.JudgmentStats(votes.map { it.vote }, 0.73333335, 1.0, 1.0, CLASS_CLICKBAIT)
        val actualStats = aggregator.aggregate(votes)

        assertStatsEqual(expectedStats, actualStats)
    }

    @Test
    fun `GIVEN a list of genuine votes, THEN aggregator returns correctly calculated results 5`() {
        val votes = getListOfVotes(0.6666667, 1.0, 0.6666667, 0.6666667, 0.6666667)
        val expectedStats =
            JudgmentsAggregator.JudgmentStats(votes.map { it.vote }, 0.73333335, 0.6666667, 0.6666667, CLASS_CLICKBAIT)
        val actualStats = aggregator.aggregate(votes)

        assertStatsEqual(expectedStats, actualStats)
    }

    private fun getListOfVotes(vararg vote: Double): List<ClickBaitVote> {
        val result = mutableListOf<ClickBaitVote>()
        vote.forEach {
            val dummyVote = ClickBaitVote("userId", "url", it)
            result.add(dummyVote)
        }
        return result
    }

    private fun assertStatsEqual(
        expectedStats: JudgmentsAggregator.JudgmentStats,
        actualStats: JudgmentsAggregator.JudgmentStats
    ) {
        Assert.assertEquals(expectedStats.truthJudgments, actualStats.truthJudgments)
        Assert.assertEquals(expectedStats.truthMean, actualStats.truthMean, 0.001)
        Assert.assertEquals(expectedStats.truthMedian, actualStats.truthMedian, 0.001)
        Assert.assertEquals(expectedStats.truthMode, actualStats.truthMode, 0.001)
        Assert.assertEquals(expectedStats.truthClass, actualStats.truthClass)
    }
}
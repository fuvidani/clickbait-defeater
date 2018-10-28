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

package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components

import com.clickbait.defeater.clickbaitservice.update.model.CLASS_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.CLASS_NO_CLICKBAIT
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.descriptive.rank.Median

/**
 * @deprecated This class is not in use anymore nor is intended for further use,
 * therefore it is subject to deletion.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Deprecated(
    "This class is not in use anymore nor is intended for further use, \n" +
            " therefore it is subject to deletion."
)
internal class JudgmentsAggregator {

    @Deprecated("Not to be used anymore; will be removed")
    internal fun aggregate(votes: List<ClickBaitVote>): JudgmentStats {
        val voteValues = votes.map { it.vote }
        val array = voteValues.toDoubleArray()
        val median = getMedian(array)
        val label = getLabel(median)
        return JudgmentStats(voteValues, getMean(array), median, getMode(array), label)
    }

    private fun getMean(values: DoubleArray): Double {
        return DescriptiveStatistics(values).mean
    }

    private fun getMedian(values: DoubleArray): Double {
        return Median().evaluate(values)
    }

    private fun getMode(values: DoubleArray): Double {
        val result = StatUtils.mode(values)
        return if (result.isEmpty()) {
            Double.NaN
        } else {
            StatUtils.mode(values)[0]
        }
    }

    private fun getLabel(median: Double): String {
        return if (median > 0.5) {
            CLASS_CLICKBAIT
        } else {
            CLASS_NO_CLICKBAIT
        }
    }

    @Deprecated("Not to be used anymore; will be removed")
    data class JudgmentStats(
        val truthJudgments: List<Double>,
        val truthMean: Double,
        val truthMedian: Double,
        val truthMode: Double,
        val truthClass: String
    )
}
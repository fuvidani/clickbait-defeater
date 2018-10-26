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
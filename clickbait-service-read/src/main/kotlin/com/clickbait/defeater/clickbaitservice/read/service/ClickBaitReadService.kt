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

package com.clickbait.defeater.clickbaitservice.read.service

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for top-level functionality of the Read-Service.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface ClickBaitReadService {

    /**
     * Determines the clickbait score of the provided social media post instance.
     *
     * This operation guarantees a valid [ClickBaitScore] object or an appropriate
     * exception if an error occurs.
     *
     * @param instance a valid social media post instance
     * @return a Mono publisher with the clickbait score object corresponding to the
     * provided instance
     */
    fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore>
}
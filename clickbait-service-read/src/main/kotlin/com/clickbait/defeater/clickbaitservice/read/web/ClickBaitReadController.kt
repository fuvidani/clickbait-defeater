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

package com.clickbait.defeater.clickbaitservice.read.web

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.ClickBaitReadService
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

/**
 * Reactive, non-blocking REST controller for the ClickBait Read-Service.
 *
 * @property clickBaitReadService an implementation of the [ClickBaitReadService] interface
 * supporting all its operations
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/clickbait")
class ClickBaitReadController(private val clickBaitReadService: ClickBaitReadService) {

    /**
     * Accepts a valid [PostInstance] object in the body and returns the
     * corresponding [ClickBaitScore] object emitted by a Mono.
     *
     * @return a Mono emitting the [ClickBaitScore] object or an error if the service
     * failed
     */
    @PostMapping("/score")
    fun scoreMediaPost(@RequestBody instance: PostInstance): Mono<ClickBaitScore> {
        return clickBaitReadService
            .scorePostInstance(instance)
            .onErrorMap(ClickBaitReadServiceException::class.java) {
                ResponseStatusException(
                    it.statusMapping,
                    it.message
                )
            }
    }
}
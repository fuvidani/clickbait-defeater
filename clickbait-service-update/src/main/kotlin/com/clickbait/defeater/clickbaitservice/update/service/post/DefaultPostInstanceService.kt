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

package com.clickbait.defeater.clickbaitservice.update.service.post

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import com.clickbait.defeater.clickbaitservice.update.model.content.toPostInstance
import com.clickbait.defeater.clickbaitservice.update.persistence.PostInstanceRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import mu.KLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * Concrete implementation of the [PostInstanceService] interface.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property postInstanceRepository a repository for persisting
 * and retrieving [PostInstance] objects
 * @property contentExtractionServiceClient a client interface
 * implementation for obtaining extracted contents
 */
@Component
class DefaultPostInstanceService(
    private val postInstanceRepository: PostInstanceRepository,
    private val contentExtractionServiceClient: ContentExtractionServiceClient
) : PostInstanceService {

    /**
     * Tries to find a [PostInstance] instance with the given `id`.
     *
     * @param id unique ID of a possible [PostInstance]
     * @return a Mono emitting the required [PostInstance] or an empty
     * Mono if it has not been found
     */
    override fun findById(id: String): Mono<PostInstance> {
        return postInstanceRepository.findById(id)
    }

    /**
     * Ensures that a semantically equivalent [PostInstance] to
     * the `vote` parameter is persisted and therefore can be
     * retrieved by the [findById] operation.
     *
     * @param vote valid clickbait vote
     * @return a Mono emitting `true` if the operation was successful,
     * otherwise `false`
     */
    override fun ensurePersistedPostInstance(vote: ClickBaitVote): Mono<Boolean> {
        return findById(vote.url)
            .map { true }
            .switchIfEmpty(
                Mono.defer {
                    contentExtractionServiceClient
                        .extractContent(vote.url)
                        .publishOn(Schedulers.elastic())
                        .subscribeOn(Schedulers.elastic())
                        .map { it.toPostInstance(vote.postText) }
                        .flatMap {
                            logger.info("Post Instance arrived, persisting...")
                            postInstanceRepository.save(it)
                        }
                        .doOnError {
                            logger.error(it) { "Could not successfully invoke Content-Extraction-Service or persist PostInstance in the repository" }
                        }
                        .subscribe()
                    Mono.just(true)
                }
            )
    }

    companion object : KLogging()
}
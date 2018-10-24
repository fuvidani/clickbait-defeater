package com.clickbait.defeater.clickbaitservice.update.service.post

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import reactor.core.publisher.Mono

/**
 * Interface for operations that deal with [PostInstance] objects
 * in the context of clickbait votes.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
interface PostInstanceService {

    /**
     * Tries to find a [PostInstance] instance with the given `id`.
     *
     * @param id unique ID of a possible [PostInstance]
     * @return a Mono emitting the required [PostInstance] or an empty
     * Mono if it has not been found
     */
    fun findById(id: String): Mono<PostInstance>

    /**
     * Ensures that a semantically equivalent [PostInstance] to
     * the `vote` parameter is persisted and therefore can be
     * retrieved by the [findById] operation.
     *
     * @param vote valid clickbait vote
     * @return a Mono emitting `true` if the operation was successful,
     * otherwise `false`
     */
    fun ensurePersistedPostInstance(vote: ClickBaitVote): Mono<Boolean>
}
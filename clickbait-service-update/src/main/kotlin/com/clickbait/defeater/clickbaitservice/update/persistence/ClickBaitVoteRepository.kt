package com.clickbait.defeater.clickbaitservice.update.persistence

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVoteEntity
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVoteKey
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant

/**
 * Reactive Repository for [ClickBaitVoteEntity] objects. This interface extends the
 * [ReactiveMongoRepository] from Spring s.t. the implementation is provided by the
 * framework as well.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
interface ClickBaitVoteRepository : ReactiveMongoRepository<ClickBaitVoteEntity, ClickBaitVoteKey> {

    /**
     * Finds all vote entities of a specific `userId` and returns them as a Flux.
     * Additionally, the query can be paginated using the `page` parameter.
     *
     * @param userId the unique ID of a user
     * @param page pagination information
     * @return a Flux emitting all [ClickBaitVoteEntity] objects in the repository
     * with the specified `userId`. Flux may be empty if no entities for the given
     * ID have been found.
     */
    fun findByIdUserId(userId: String, page: Pageable): Flux<ClickBaitVoteEntity>

    /**
     * Finds all vote entities where the `lastUpdate` attribute is temporally after
     * the parameter `after` and returns them as a Flux.
     *
     * @param after a valid [Instant] object
     * @return Flux emitting all [ClickBaitVoteEntity] objects in the repository
     * that have been last update after the `after` parameter. Flux may be empty
     * if no such entity is persisted.
     */
    fun findByLastUpdateAfter(after: Instant): Flux<ClickBaitVoteEntity>
}
package com.clickbait.defeater.clickbaitservice.update.persistence

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVoteEntity
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVoteKey
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.Instant

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
interface ClickBaitVoteRepository : ReactiveMongoRepository<ClickBaitVoteEntity, ClickBaitVoteKey> {

    fun findByIdUserId(userId: String, page: Pageable): Flux<ClickBaitVoteEntity>

    fun findByLastUpdateAfter(after: Instant): Flux<ClickBaitVoteEntity>
}
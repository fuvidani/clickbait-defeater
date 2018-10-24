package com.clickbait.defeater.clickbaitservice.update.persistence

import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

/**
 * Reactive Repository for [PostInstance] objects. This interface extends the
 * [ReactiveMongoRepository] from Spring s.t. the implementation is provided by the
 * framework as well.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
interface PostInstanceRepository : ReactiveMongoRepository<PostInstance, String>
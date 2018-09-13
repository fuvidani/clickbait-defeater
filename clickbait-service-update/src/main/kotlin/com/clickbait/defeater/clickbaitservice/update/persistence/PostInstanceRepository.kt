package com.clickbait.defeater.clickbaitservice.update.persistence

import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

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
interface PostInstanceRepository : ReactiveMongoRepository<PostInstance, String>
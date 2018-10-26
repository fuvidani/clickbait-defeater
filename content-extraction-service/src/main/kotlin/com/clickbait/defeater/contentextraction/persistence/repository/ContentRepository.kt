package com.clickbait.defeater.contentextraction.persistence.repository

import com.clickbait.defeater.contentextraction.model.ContentWrapper
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

/**
 * Reactive Repository for [ContentWrapper] objects. This interface extends the
 * [ReactiveMongoRepository] from Spring s.t. the implementation is provided by the
 * framework as well.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Repository
interface ContentRepository : ReactiveMongoRepository<ContentWrapper, String>
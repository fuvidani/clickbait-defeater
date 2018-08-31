package com.clickbait.defeater.contentextraction.persistence.repository

import com.clickbait.defeater.contentextraction.model.ContentWrapper
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
interface ContentRepository : ReactiveMongoRepository<ContentWrapper, String>
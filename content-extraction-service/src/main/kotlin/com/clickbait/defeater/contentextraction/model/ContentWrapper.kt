package com.clickbait.defeater.contentextraction.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Document(collection = "contents")
data class ContentWrapper(
    @Id
    val url: String,
    val contents: List<Content>
)
package com.clickbait.defeater.clickbaitservice.update.model.content

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
data class ContentWrapper(
    val redirectUrl: String,
    val sourceUrl: String,
    val contents: List<Content>
)
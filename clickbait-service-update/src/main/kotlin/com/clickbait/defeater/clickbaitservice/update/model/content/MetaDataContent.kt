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
data class MetaDataContent(
    val type: MetaDataType,
    val data: String
) : Content {
    override val contentType = ContentType.META_DATA
}

enum class MetaDataType {
    TITLE,
    DESCRIPTION,
    KEYWORDS,
    LANGUAGE,
    IMAGE,
    VIDEO,
    TIMESTAMP
}
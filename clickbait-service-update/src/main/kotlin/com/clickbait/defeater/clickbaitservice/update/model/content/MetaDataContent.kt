package com.clickbait.defeater.clickbaitservice.update.model.content

/**
 * A meta-data [Content] with a `data` attribute containing
 * the actual data.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property type the type of meta-data
 * @property data the data itself; can be word, sentence,
 * list of words or even an URL
 */
data class MetaDataContent(
    val type: MetaDataType,
    val data: String
) : Content {
    override val contentType = ContentType.META_DATA
}

/**
 * Enumeration of the meta-data types.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
enum class MetaDataType {
    TITLE,
    DESCRIPTION,
    KEYWORDS,
    LANGUAGE,
    IMAGE,
    VIDEO,
    TIMESTAMP
}
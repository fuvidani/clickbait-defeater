package com.clickbait.defeater.contentextraction.model

/**
 * A text [Content] with a `text` attribute representing
 * that particular textual content.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property text the text this [Content] encapsulates; can
 * be characters, words, sentences, paragraphs...
 */
data class TextContent(val text: String) : Content {
    override val contentType = ContentType.TEXT
}
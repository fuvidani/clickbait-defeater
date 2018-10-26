package com.clickbait.defeater.contentextraction.model

/**
 * An HTML [Content] containing plain HTML code that is
 * ready to be embedded.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property html valid HTML code in a String representation
 */
data class HtmlContent(val html: String) : Content {
    override val contentType = ContentType.HTML
}
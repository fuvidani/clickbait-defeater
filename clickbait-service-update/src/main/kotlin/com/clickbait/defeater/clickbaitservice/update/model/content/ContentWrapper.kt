/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.clickbait.defeater.clickbaitservice.update.model.content

import com.clickbait.defeater.clickbaitservice.update.model.PostInstance

/**
 * A wrapper object encapsulating the redirect- and source-URL of a
 * certain web page (they aren't necessarily different) along with
 * a list of [Content] objects that this particular web page contains.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property redirectUrl the originally obtained URL which may be a
 * statically or dynamically redirected one. In this case the `redirectUrl`
 * differs from the `sourceUrl`.
 * @property sourceUrl the actual URL of the web page, i.e. it is not a
 * redirect one.
 * @property contents list of [Content] objects describing the web page's
 * content
 */
data class ContentWrapper(
    val redirectUrl: String,
    val sourceUrl: String,
    val contents: List<Content>
)

/**
 * Transforms this [ContentWrapper] instance to a valid [PostInstance]
 * instance with the provided `postText` list.
 *
 * @param postText a list of strings where each string can be a word,
 * sentence or a paragraph. Since `postText` is the only information
 * which cannot be deducted from a [ContentWrapper] object, it needs
 * to be provided.
 * @receiver valid [ContentWrapper] instance
 * @return a valid [PostInstance] instance reflecting this [ContentWrapper]
 * and the `postText`
 */
fun ContentWrapper.toPostInstance(postText: List<String>): PostInstance {
    var timeStamp = ""
    var targetTitle = ""
    var targetDescription = ""
    var targetKeyWords = ""
    var language: String? = null
    val targetParagraphs: MutableList<String> = mutableListOf()
    this.contents
        .stream()
        .forEach {
            if (it is TextContent) {
                targetParagraphs.add(it.text)
            } else if (it is MetaDataContent) {
                when {
                    it.type == MetaDataType.TIMESTAMP -> timeStamp = it.data
                    it.type == MetaDataType.DESCRIPTION -> targetDescription = it.data
                    it.type == MetaDataType.TITLE -> targetTitle = it.data
                    it.type == MetaDataType.KEYWORDS -> targetKeyWords = it.data
                    it.type == MetaDataType.LANGUAGE -> language = it.data
                }
            }
        }
    return PostInstance(
        this.redirectUrl,
        language ?: "unknown",
        postText,
        timeStamp, emptyList(),
        targetTitle,
        targetDescription,
        emptyList(),
        targetKeyWords,
        targetParagraphs
    )
}
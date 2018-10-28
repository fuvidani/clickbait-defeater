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

package com.clickbait.defeater.clickbaitservice.read.model

/**
 * Model of a social media post instance
 *
 * @property id the unique ID of the instance, usually the URL of the social media content this
 * instance represents
 * @property language the language of the content this instance consists of in ISO-639-1 format. This
 * is optional and defaults to "unknown".
 * @property postText the text as a list of strings which this social media instance has been posted with
 * @property postTimestamp optional timestamp when the instance has been published
 * @property postMedia optional list of strings, where each string points to the location of a media file
 * associated with this instance
 * @property targetTitle optional title of the shared (targeted) link/content
 * @property targetDescription optional description of the shared (targeted) link/content
 * @property targetKeywords optional comma-separated keywords of the shared (targeted) link/content
 * @property targetParagraphs optional list of strings as the paragraphs of the shared (targeted) link/content
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
data class PostInstance(
    val id: String,
    val language: String = "unknown",
    val postText: List<String>,
    val postTimestamp: String = "",
    val postMedia: List<String> = emptyList(),
    val targetTitle: String = "",
    val targetDescription: String = "",
    val targetKeywords: String = "",
    val targetParagraphs: List<String> = emptyList()
)

/**
 * Model representing a clickbait score
 *
 * @property id the unique ID of the score, usually the URL of the social media content the score
 * refers to (but it could be some other ID depending on the usage)
 * @property clickbaitScore the actual clickbait score ranging between 0.0 and 1.0
 * @property language the language of the content this score refers to in ISO-639-1 format. This
 * is optional and defaults to "en".
 * @property message an optional attribute reserved to allow propagation of certain messages.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
data class ClickBaitScore(
    val id: String,
    val clickbaitScore: Double,
    val language: String = "en",
    val message: String = ""
)

/**
 * Extension function, which generates a deep copy of this post instance with the difference being
 * in the language attribute.
 *
 * @param language the language the new instance should have preferably in ISO-639-1 format.
 * @receiver a valid post instance
 * @return a valid deep copy of the receiver with the specified language
 */
fun PostInstance.withLanguage(language: String): PostInstance {
    return PostInstance(
        this.id,
        language,
        this.postText,
        this.postTimestamp,
        this.postMedia,
        this.targetTitle,
        this.targetDescription,
        this.targetKeywords,
        this.targetParagraphs
    )
}
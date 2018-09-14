package com.clickbait.defeater.clickbaitservice.update.model.content

import com.clickbait.defeater.clickbaitservice.update.model.PostInstance

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
    return PostInstance(this.redirectUrl,
        language ?: "unknown",
        postText,
        timeStamp, emptyList(),
        targetTitle,
        targetDescription,
        emptyList(),
        targetKeyWords,
        targetParagraphs)
}
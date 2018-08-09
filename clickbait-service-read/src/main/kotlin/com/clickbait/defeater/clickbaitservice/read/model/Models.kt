package com.clickbait.defeater.clickbaitservice.read.model

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
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

data class ClickBaitScore(
    val id: String,
    val clickbaitScore: Double,
    val language: String = "en",
    val message: String = ""
)

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
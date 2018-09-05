package com.clickbait.defeater.clickbaitservice.update.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Document(collection = "posts")
data class PostInstance(
    @Id
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

/*@Document(collection = "judgments")
data class PostInstanceJudgments(
    @Id
    val id: String,
    val truthJudgments: List<Double>
)

fun PostInstanceJudgments.toStats() : PostInstanceJudgmentStats {
    return PostInstanceJudgmentStats(this.id, this.truthJudgments,0.0,0.0,0.0, CLASS_NO_CLICKBAIT)
}*/

data class PostInstanceJudgmentStats(
    val id: String,
    val truthJudgments: List<Double>,
    val truthMean: Double,
    val truthMedian: Double,
    val truthMode: Double,
    val truthClass: String
)

const val CLASS_CLICKBAIT = "clickbait"
const val CLASS_NO_CLICKBAIT = "no-clickbait"

data class PostInstanceJudgments(
    val postInstance: PostInstance,
    val stats: PostInstanceJudgmentStats
)

data class MultiplePostInstanceJudgments(
    val judgments: List<PostInstanceJudgments>
)

data class ClickBaitVote(
    val userId: String,
    val url: String,
    val vote: Double = 0.0,
    val postText: List<String> = emptyList(),
    val lastUpdate: Instant = Instant.MIN
)

fun ClickBaitVote.toEntity(): ClickBaitVoteEntity {
    val key = ClickBaitVoteKey(this.userId, this.url)
    return ClickBaitVoteEntity(key, this.vote, Instant.now())
}

fun ClickBaitVote.toEntity(lastUpdate: Instant): ClickBaitVoteEntity {
    val key = ClickBaitVoteKey(this.userId, this.url)
    return ClickBaitVoteEntity(key, this.vote, lastUpdate)
}

fun ClickBaitVote.toDecoded(): ClickBaitVote {
    val decodedUrl = URLDecoder.decode(this.url, StandardCharsets.UTF_8.name())
    return ClickBaitVote(this.userId, decodedUrl, this.vote, this.postText, this.lastUpdate)
}

@Document(collection = "votes")
data class ClickBaitVoteEntity(
    @Id
    val id: ClickBaitVoteKey,
    val vote: Double,
    val lastUpdate: Instant
)

data class ClickBaitVoteKey(
    val userId: String,
    val url: String
) : Serializable

fun ClickBaitVoteEntity.toModel(): ClickBaitVote {
    return ClickBaitVote(this.id.userId, this.id.url, this.vote, lastUpdate = this.lastUpdate)
}

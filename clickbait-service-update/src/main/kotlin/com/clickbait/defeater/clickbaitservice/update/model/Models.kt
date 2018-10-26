package com.clickbait.defeater.clickbaitservice.update.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Model of a social media post instance
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
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
    val targetCaptions: List<String> = emptyList(),
    val targetKeywords: String = "",
    val targetParagraphs: List<String> = emptyList()
)

/**
 * Object containing a list of votes corresponding to a single
 * social media post.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property id the unique ID of the [PostInstance] object these votes
 * refer to
 * @property truthJudgments a valid list of [Double], where each element is
 * a vote between 0.0 and 1.0
 */
data class PostInstanceJudgmentStats(
    val id: String,
    val truthJudgments: List<Double>
)

const val CLASS_CLICKBAIT = "clickbait"
const val CLASS_NO_CLICKBAIT = "no-clickbait"
const val SERVICE_ZONE_ID = "Europe/Vienna"

/**
 * Wrapper object around a [PostInstance] and its corresponding
 * [PostInstanceJudgmentStats] for better encapsulation.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property postInstance valid social media post instance
 * @property stats valid vote stats referring to the `postInstance`
 */
data class PostInstanceJudgments(
    val postInstance: PostInstance,
    val stats: PostInstanceJudgmentStats
)

/**
 * Wrapper object encapsulating multiple instances of
 * [PostInstanceJudgments].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property judgments a valid list of [PostInstanceJudgments] elements
 */
data class MultiplePostInstanceJudgments(
    val judgments: List<PostInstanceJudgments>
)

/**
 * Domain object representing a specific vote, i.e. a vote which
 * belongs to exactly one `userId` and `url`.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property userId unique ID of a client who this particular vote "belongs" to.
 * It can be of any schema or structure.
 * @property url the absolute URL of the web page for which this vote is casted
 * @property vote the vote of the user ranging between 0.0 (no-clickbait) and
 * 1.0 (clickbait)
 * @property postText optional list of strings representing the sentences/paragraphs
 * of the text which the web page (identified via the `url`) has been posted with on
 * a social media platform
 * @property lastUpdate optional date and time of the last update of this
 * particular vote. Defaults to the date and time of object instantiation.
 */
data class ClickBaitVote(
    val userId: String,
    val url: String,
    val vote: Double = 0.0,
    val postText: List<String> = emptyList(),
    val lastUpdate: ZonedDateTime = ZonedDateTime.now()
)

/**
 * Transforms this [ClickBaitVote] to an equivalent [ClickBaitVoteEntity]
 *
 * @receiver a valid [ClickBaitVote] instance
 * @return equivalent [ClickBaitVoteEntity] instance to this object
 */
fun ClickBaitVote.toEntity(): ClickBaitVoteEntity {
    val key = ClickBaitVoteKey(this.userId, this.url)
    return ClickBaitVoteEntity(key, this.vote, Instant.now())
}

/**
 * Overloaded variant of [toEntity] s.t the `lastUpdate` attribute
 * can be provided. Mainly used in testing scenarios where dates and
 * times must always match.
 *
 * @param lastUpdate a valid [Instant] instance
 * @receiver valid [ClickBaitVote] instance
 * @return a valid [ClickBaitVoteEntity] equivalent to this [ClickBaitVote]
 * instance with the difference being the `lastUpdate` attribute
 */
fun ClickBaitVote.toEntity(lastUpdate: Instant): ClickBaitVoteEntity {
    val key = ClickBaitVoteKey(this.userId, this.url)
    return ClickBaitVoteEntity(key, this.vote, lastUpdate)
}

/**
 * Replaces this instance's `url` attribute with a UTF-8 decoded one.
 * Sometimes an arbitrary input URL might be encoded which makes it
 * difficult to process. This function merely does a safety decoding of
 * it.
 *
 * @receiver a valid [ClickBaitVote] instance
 * @return same [ClickBaitVote] instance with a decoded `url` attribute
 */
fun ClickBaitVote.toDecoded(): ClickBaitVote {
    val decodedUrl = URLDecoder.decode(this.url, StandardCharsets.UTF_8.name())
    return ClickBaitVote(this.userId, decodedUrl, this.vote, this.postText, this.lastUpdate)
}

/**
 * A semantically equivalent representation of a [ClickBaitVote] instance,
 * however optimized for data persistence. Since a `userId` can be paired
 * with multiple `url`s and vice versa, only a composite key can
 * unambiguously identify a single vote.
 *
 * This object uses [Instant] instead of [ZonedDateTime] due to the limitations
 * of persistence technology. However, since only the zone of the service itself
 * matters, the time-zone can manually be added when converting an [Instant] to
 * a [ZonedDateTime].
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property id a valid key consisting of a `userId` and `url`
 * @property vote the vote of the user ranging between 0.0 (no-clickbait) and
 * 1.0 (clickbait)
 * @property lastUpdate a zone-less date and time of the last update of this vote
 */
@Document(collection = "votes")
data class ClickBaitVoteEntity(
    @Id
    val id: ClickBaitVoteKey,
    val vote: Double,
    val lastUpdate: Instant
)

/**
 * Object representing the composite key of a [ClickBaitVoteEntity] instance.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 *
 * @property userId unique ID of a client who this particular vote "belongs" to.
 * It can be of any schema or structure.
 * @property url the absolute URL of the webpage for which this vote is casted
 */
data class ClickBaitVoteKey(
    val userId: String,
    val url: String
) : Serializable

/**
 * Transforms this [ClickBaitVoteEntity] to an equivalent [ClickBaitVote] instance.
 * The `lastUpdate` attribute is converted into a valid [ZonedDateTime] using the
 * defined [SERVICE_ZONE_ID].
 *
 * @receiver a valid [ClickBaitVoteEntity] instance
 * @return a valid [ClickBaitVote] instance with an empty `postText` attribute
 */
fun ClickBaitVoteEntity.toModel(): ClickBaitVote {
    return ClickBaitVote(
        this.id.userId, this.id.url, this.vote, lastUpdate = this.lastUpdate.atZone(
            ZoneId.of(
                SERVICE_ZONE_ID
            )
        )
    )
}

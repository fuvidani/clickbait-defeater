package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.persistence.ClickBaitVoteRepository
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.PostInstanceService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.after
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringRunner::class)
class JudgmentsPersistenceSchedulerTest {

    private lateinit var persistenceScheduler: JudgmentsPersistenceScheduler
    @MockBean
    private lateinit var voteRepository: ClickBaitVoteRepository
    @MockBean
    private lateinit var judgmentsRepository: JudgmentsRepository
    @MockBean
    private lateinit var postInstanceService: PostInstanceService
    private val schedulerProperties = SchedulerProperties(24, 5)

    @Before
    fun setUp() {
        persistenceScheduler = JudgmentsPersistenceScheduler(
            voteRepository,
            postInstanceService,
            schedulerProperties,
            judgmentsRepository
        )
    }

    @Test
    fun `GIVEN multiple available vote data, THEN only groups of votes larger than 5 AND with supported language are processed`() {
        val englishVotes1 = getDummyVoteEntitiesForUrl("someUrl", 0.0, 1.0, 0.33333334, 0.6666667, 0.33333334)
        val englishVotes2 = getDummyVoteEntitiesForUrl("someOtherUrl", 1.0, 0.6666667, 1.0, 0.0, 1.0)
        val spanishVotes = getDummyVoteEntitiesForUrl("someOtherUrl2", 1.0, 0.6666667, 1.0, 0.0, 1.0)
        val notEnoughVotes = getDummyVoteEntitiesForUrl("someOtherUrl3", 1.0, 0.6666667, 1.0, 0.0)
        val entities = englishVotes1 + englishVotes2 + spanishVotes + notEnoughVotes
        Mockito.`when`(voteRepository.findByLastUpdateAfter(any(Instant::class.java)))
            .thenReturn(Flux.fromIterable(entities))

        val englishPost1 = PostInstance("someUrl", "en", emptyList())
        val englishPost2 = PostInstance("someOtherUrl", "en", emptyList())
        val spanishPost = PostInstance("someOtherUrl2", "es", emptyList())
        Mockito.`when`(postInstanceService.findById(englishPost1.id)).thenReturn(Mono.just(englishPost1))
        Mockito.`when`(postInstanceService.findById(englishPost2.id)).thenReturn(Mono.just(englishPost2))
        Mockito.`when`(postInstanceService.findById(spanishPost.id)).thenReturn(Mono.just(spanishPost))

        val englishPost1Stats = PostInstanceJudgmentStats(englishPost1.id, englishVotes1.map { it.vote })
        val englishPost1Judgments = PostInstanceJudgments(englishPost1, englishPost1Stats)
        val englishPost2Stats = PostInstanceJudgmentStats(englishPost2.id, englishVotes2.map { it.vote })
        val englishPost2Judgments = PostInstanceJudgments(englishPost2, englishPost2Stats)
        val expectedJudgmentsWrapper =
            MultiplePostInstanceJudgments(listOf(englishPost2Judgments, englishPost1Judgments))
        Mockito.`when`(judgmentsRepository.saveAll(any(MultiplePostInstanceJudgments::class.java)))
            .thenReturn(Mono.empty())

        persistenceScheduler.persistVotesToJudgmentRepository()

        Mockito.verify(judgmentsRepository, after(500)).saveAll(expectedJudgmentsWrapper)
    }

    @Test
    fun `GIVEN multiple English vote data, THEN each one is processed`() {
        val random = Random(123456)
        val englishVotes = getDummyVoteEntitiesForUrl("someUrl", 0.0, 1.0, 0.33333334, 0.6666667, 0.33333334)
        val englishUsVotes = getDummyVoteEntitiesForUrl("someOtherUrl", 1.0, 0.6666667, 1.0, 0.0, 1.0)
        val englishUKVotes = getDummyVoteEntitiesForUrl("completelyNewUrl", 0.0, 0.0, 0.0, 0.0, 0.0)
        val shuffledVoteEntities = (englishVotes + englishUsVotes + englishUKVotes).shuffled(random)
        Mockito.`when`(voteRepository.findByLastUpdateAfter(any(Instant::class.java)))
            .thenReturn(Flux.fromIterable(shuffledVoteEntities))

        val englishPost = PostInstance("someUrl", "en", emptyList())
        val englishUsPost = PostInstance("someOtherUrl", "en-US", emptyList())
        val englishUkPost = PostInstance("completelyNewUrl", "en-UK", emptyList())
        Mockito.`when`(postInstanceService.findById(englishPost.id)).thenReturn(Mono.just(englishPost))
        Mockito.`when`(postInstanceService.findById(englishUsPost.id)).thenReturn(Mono.just(englishUsPost))
        Mockito.`when`(postInstanceService.findById(englishUkPost.id)).thenReturn(Mono.just(englishUkPost))
        Mockito.`when`(judgmentsRepository.saveAll(any(MultiplePostInstanceJudgments::class.java)))
            .thenReturn(Mono.empty())

        val englishPostStats = PostInstanceJudgmentStats(
            englishPost.id, listOf(0.33333334, 0.0, 0.33333334, 1.0, 0.6666667)
        )
        val englishPostJudgments = PostInstanceJudgments(englishPost, englishPostStats)

        val englishUsPostStats = PostInstanceJudgmentStats(
            englishUsPost.id,
            listOf(1.0, 1.0, 0.0, 1.0, 0.6666667)
        )
        val englishUsPostJudgments = PostInstanceJudgments(englishUsPost, englishUsPostStats)

        val englishUkPostStats = PostInstanceJudgmentStats(
            englishUkPost.id, listOf(0.0, 0.0, 0.0, 0.0, 0.0)
        )
        val englishUkPostJudgments = PostInstanceJudgments(englishUkPost, englishUkPostStats)

        val expectedJudgmentsWrapper =
            MultiplePostInstanceJudgments(listOf(englishUsPostJudgments, englishUkPostJudgments, englishPostJudgments))

        persistenceScheduler.persistVotesToJudgmentRepository()

        Mockito.verify(judgmentsRepository, after(500)).saveAll(expectedJudgmentsWrapper)
    }

    @Test
    fun `GIVEN votes AND a not persisted corresponding PostInstance, THEN scheduler is not storing anything in JudgmentsRepository`() {
        val englishVotes = getDummyVoteEntitiesForUrl("someUrl", 0.0, 1.0, 0.33333334, 0.6666667, 0.33333334)
        Mockito.`when`(voteRepository.findByLastUpdateAfter(any(Instant::class.java)))
            .thenReturn(Flux.fromIterable(englishVotes))

        val englishPost = PostInstance("someUrl", "en", emptyList())
        Mockito.`when`(postInstanceService.findById(englishPost.id)).thenReturn(Mono.empty())
        Mockito.`when`(judgmentsRepository.saveAll(any(MultiplePostInstanceJudgments::class.java)))
            .thenReturn(Mono.empty())

        persistenceScheduler.persistVotesToJudgmentRepository()

        // repository's saveAll should not have been invoked
        Mockito.verify(judgmentsRepository, after(500).never()).saveAll(any(MultiplePostInstanceJudgments::class.java))
    }

    private fun getDummyVoteEntitiesForUrl(url: String, vararg vote: Double): List<ClickBaitVoteEntity> {
        val result = mutableListOf<ClickBaitVoteEntity>()
        vote.forEach {
            val entity = ClickBaitVoteEntity(ClickBaitVoteKey("userId", url), it, Instant.now())
            result.add(entity)
        }
        return result
    }

    // Kotlin<->Java Mockito type inference workaround
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
}
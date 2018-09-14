package com.clickbait.defeater.clickbaitservice.update.service

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.ClickBaitServiceUpdateApplication
import com.clickbait.defeater.clickbaitservice.update.TestData
import com.clickbait.defeater.clickbaitservice.update.config.DbIntegrationTest
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.model.content.MetaDataContent
import com.clickbait.defeater.clickbaitservice.update.model.content.TextContent
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.after
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
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
@Category(DbIntegrationTest::class)
@SpringBootTest(value = ["application.yml"], classes = [ClickBaitServiceUpdateApplication::class])
class ClickBaitVoteServiceIntegrationTest {

    @Autowired
    private lateinit var clickBaitVoteService: ClickBaitVoteService
    @MockBean
    private lateinit var contentExtractionServiceClient: ContentExtractionServiceClient
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Before
    fun setUp() {
        mongoTemplate.dropCollection("posts")
        mongoTemplate.dropCollection("votes")
    }

    @Test
    fun `GIVEN a valid new vote, THEN service's submitVote() successfully persists it`() {
        val vote = ClickBaitVote("userId", "someUrl", 0.7, listOf("Hello World"))
        val mockedExtractedContent = TestData.getSampleContentWrapper(vote.url)
        Mockito.`when`(contentExtractionServiceClient.extractContent(vote.url)).thenReturn(Mono.just(mockedExtractedContent))

        val expectedPostInstance = getExpectedPostInstance(vote.url, vote.postText)

        val publisher = clickBaitVoteService.submitVote(vote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()

        Thread.sleep(500)
        val storedPostInstances = mongoTemplate.findAll(PostInstance::class.java)
        Assert.assertEquals(1, storedPostInstances.size)
        Assert.assertEquals(expectedPostInstance, storedPostInstances[0])

        val storedVotes = mongoTemplate.findAll(ClickBaitVoteEntity::class.java)
        Assert.assertEquals(1, storedVotes.size)
        assertExpectedVote(vote, storedVotes[0])
    }

    @Test
    fun `GIVEN an already existing vote AND its corresponding PostInstance, THEN service's submitVote() only performs update`() {
        val timeOfOriginalVote = Instant.now().minus(Duration.ofHours(2))
        val alreadyStoredVote = ClickBaitVote("userId", "someUrl", 0.7, listOf("Hello World"))
        val alreadyStoredPostInstance = getExpectedPostInstance(alreadyStoredVote.url, alreadyStoredVote.postText)
        mongoTemplate.save(alreadyStoredVote.toEntity(timeOfOriginalVote), "votes")
        mongoTemplate.save(alreadyStoredPostInstance, "posts")
        val updatedVote = ClickBaitVote(alreadyStoredVote.userId, alreadyStoredVote.url, 0.2, alreadyStoredVote.postText)

        val publisher = clickBaitVoteService.submitVote(updatedVote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(contentExtractionServiceClient, after(500).never()).extractContent(ArgumentMatchers.anyString())

        val updatedVoteEntity = mongoTemplate.findAll(ClickBaitVoteEntity::class.java)[0]
        assertExpectedVote(updatedVote, updatedVoteEntity)
        Assert.assertTrue(updatedVoteEntity.lastUpdate.isAfter(timeOfOriginalVote))
    }

    @Test
    fun `GIVEN a userId AND an URL for which a vote already exists, THEN service's findVote() returns it`() {
        val timeOfUpdate = Instant.now()
        val alreadyStoredVote = ClickBaitVote("userId", "someUrl", 0.7, lastUpdate = ZonedDateTime.ofInstant(timeOfUpdate,
            ZoneId.of(SERVICE_ZONE_ID)))
        mongoTemplate.save(alreadyStoredVote.toEntity(timeOfUpdate), "votes")

        val publisher = clickBaitVoteService.findVote(alreadyStoredVote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(alreadyStoredVote)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId AND an URL for which NO vote exist, THEN service's findVote() returns empty Mono`() {
        val vote = ClickBaitVote("userId", "someUrl")

        val publisher = clickBaitVoteService.findVote(vote)

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN findAllUserVotes() returns correctly paged votes`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userCList = getDummyListOfVotes("userC", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList + userCList)

        val publisher = clickBaitVoteService.findAllUserVotes("userA", Pageable.unpaged())

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNextSequence(userAList)
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN findAllUserVotes() returns correctly paged votes 2`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = clickBaitVoteService.findAllUserVotes("userA", PageRequest.of(0, 2))

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(userAList[0])
            .expectNext(userAList[1])
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN findAllUserVotes() returns correctly paged votes 3`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = clickBaitVoteService.findAllUserVotes("userA", PageRequest.of(1, 3))

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(userAList[3])
            .expectNext(userAList[4])
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN findAllUserVotes() returns correctly paged votes 4`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = clickBaitVoteService.findAllUserVotes("userA", PageRequest.of(2, 4))

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `GIVEN a userId for which multiple votes already exist, THEN findAllUserVotes() returns correctly paged votes 5`() {
        val timeOfInsertion = Instant.now()
        val userAList = getDummyListOfVotes("userA", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        val userBList = getDummyListOfVotes("userB", timeOfInsertion, "url1", "url2", "url3", "url4", "url5")
        saveClickBaitVotes(timeOfInsertion, userAList + userBList)

        val publisher = clickBaitVoteService.findAllUserVotes("userB", PageRequest.of(4, 1))

        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(userBList[4])
            .expectComplete()
            .log()
            .verify()
    }

    private fun getExpectedPostInstance(url: String, postText: List<String>): PostInstance {
        return PostInstance(
            url,
            (TestData.getSampleLanguageMetaDataContent() as MetaDataContent).data,
            postText,
            (TestData.getSampleTimestampMetaDataContent() as MetaDataContent).data, emptyList(),
            (TestData.getSampleTitleMetaDataContent() as MetaDataContent).data,
            (TestData.getSampleDescriptionMetaDataContent() as MetaDataContent).data,
            emptyList(),
            (TestData.getSampleKeywordsMetaDataContent() as MetaDataContent).data,
            listOf((TestData.getSampleTextContent() as TextContent).text)
        )
    }

    private fun assertExpectedVote(reference: ClickBaitVote, entity: ClickBaitVoteEntity) {
        Assert.assertEquals(ClickBaitVoteKey(reference.userId, reference.url), entity.id)
        Assert.assertEquals(reference.vote, entity.vote, 0.0)
    }

    private fun getDummyListOfVotes(userId: String, timeOfInsertion: Instant, vararg urls: String): List<ClickBaitVote> {
        val result = mutableListOf<ClickBaitVote>()
        urls.forEach {
            val vote = ClickBaitVote(userId, it, Random().nextDouble(), emptyList(), ZonedDateTime.ofInstant(timeOfInsertion,
                ZoneId.of(SERVICE_ZONE_ID)))
            result.add(vote)
        }
        return result
    }

    private fun saveClickBaitVotes(timeOfInsertion: Instant, votes: List<ClickBaitVote>) {
        votes.forEach {
            mongoTemplate.save(it.toEntity(timeOfInsertion), "votes")
        }
    }
}
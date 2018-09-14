package com.clickbait.defeater.clickbaitservice.update.service.post

import com.clickbait.defeater.clickbaitservice.update.TestData
import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import com.clickbait.defeater.clickbaitservice.update.model.content.MetaDataContent
import com.clickbait.defeater.clickbaitservice.update.model.content.TextContent
import com.clickbait.defeater.clickbaitservice.update.persistence.PostInstanceRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.after
import org.mockito.Mockito.never
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

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
class PostInstanceServiceTest {

    private lateinit var postInstanceService: PostInstanceService
    @MockBean
    private lateinit var postInstanceRepository: PostInstanceRepository
    @MockBean
    private lateinit var contentExtractionServiceClient: ContentExtractionServiceClient

    @Before
    fun setUp() {
        postInstanceService = DefaultPostInstanceService(postInstanceRepository, contentExtractionServiceClient)
    }

    @Test
    fun `GIVEN an ID of a PostInstance that is already persisted, THEN findById() method returns persisted object`() {
        val expectedPostInstance = PostInstance("https://www.google.at/", "en", listOf("Hello"))
        Mockito.`when`(postInstanceRepository.findById(expectedPostInstance.id))
            .thenReturn(Mono.just(expectedPostInstance))

        val publisher = postInstanceService.findById(expectedPostInstance.id)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(expectedPostInstance)
            .expectComplete()
            .verify()
    }

    @Test
    fun `GIVEN an ID of a PostInstance that is not yet persisted, THEN findById() method returns empty Mono`() {
        Mockito.`when`(postInstanceRepository.findById("someUrlNotYetInDb")).thenReturn(Mono.empty())

        val publisher = postInstanceService.findById("someUrlNotYetInDb")
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectComplete()
            .verify()
    }

    @Test
    fun `GIVEN a vote whose corresponding PostInstance is already persisted, THEN method returns true AND ContentExtractionService is NOT invoked AND PostInstanceRepository save() is NOT invoked`() {
        val vote = ClickBaitVote("userId", "alreadyPersistedObjId", 1.0, emptyList())
        Mockito.`when`(postInstanceRepository.findById(vote.url)).thenReturn(
            Mono.just(
                PostInstance(
                    "", "",
                    emptyList()
                )
            )
        )

        val publisher = postInstanceService.ensurePersistedPostInstance(vote)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .verify()

        Mockito.verifyZeroInteractions(contentExtractionServiceClient) // no external API call
        Mockito.verify(postInstanceRepository, never()).save(Mockito.any(PostInstance::class.java)) // no "save" to DB
    }

    @Test
    fun `GIVEN a vote whose corresponding PostInstance is not yet persisted, THEN method returns true AND ContentExtractionService is invoked AND PostInstanceRepository save() is invoked`() {
        val vote = ClickBaitVote("userId", "someUrlNotYetPersisted", 1.0, emptyList())
        val contentExtractionResult = TestData.getSampleContentWrapper(vote.url)
        val expectedPostInstance = PostInstance(
            vote.url,
            (TestData.getSampleLanguageMetaDataContent() as MetaDataContent).data,
            listOf(),
            (TestData.getSampleTimestampMetaDataContent() as MetaDataContent).data,
            emptyList(),
            (TestData.getSampleTitleMetaDataContent() as MetaDataContent).data,
            (TestData.getSampleDescriptionMetaDataContent() as MetaDataContent).data,
            emptyList(),
            (TestData.getSampleKeywordsMetaDataContent() as MetaDataContent).data,
            listOf((TestData.getSampleTextContent() as TextContent).text)
        )
        Mockito.`when`(postInstanceRepository.findById(vote.url)).thenReturn(Mono.empty())
        Mockito.`when`(contentExtractionServiceClient.extractContent(vote.url))
            .thenReturn(Mono.just(contentExtractionResult))
        Mockito.`when`(postInstanceRepository.save(expectedPostInstance)).thenReturn(Mono.just(expectedPostInstance))

        val publisher = postInstanceService.ensurePersistedPostInstance(vote)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(contentExtractionServiceClient, after(500)).extractContent(vote.url)
        Mockito.verify(postInstanceRepository, after(500)).save(expectedPostInstance)
    }

    @Test
    fun `GIVEN an arbitrary error during invocation of ContentExtractionService, THEN method fails gracefully without influencing the client AND repository is not invoked`() {
        val vote = ClickBaitVote("userId", "someUrlNotYetPersisted", 1.0, emptyList())
        Mockito.`when`(postInstanceRepository.findById(vote.url)).thenReturn(Mono.empty())
        Mockito.`when`(contentExtractionServiceClient.extractContent(vote.url)).thenReturn(Mono.error(RuntimeException("Service threw an exception")))

        val publisher = postInstanceService.ensurePersistedPostInstance(vote)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(contentExtractionServiceClient, after(500)).extractContent(vote.url)
        Mockito.verify(postInstanceRepository, after(500).never()).save(Mockito.any(PostInstance::class.java))
    }

    @Test
    fun `GIVEN an arbitrary error in save() method of the repository, THEN method fails gracefully without influencing the client`() {
        val vote = ClickBaitVote("userId", "someUrlNotYetPersisted", 1.0, emptyList())
        val contentExtractionResult = TestData.getSampleContentWrapper(vote.url)
        val expectedPostInstance = PostInstance(
            vote.url,
            (TestData.getSampleLanguageMetaDataContent() as MetaDataContent).data,
            listOf(),
            (TestData.getSampleTimestampMetaDataContent() as MetaDataContent).data,
            emptyList(),
            (TestData.getSampleTitleMetaDataContent() as MetaDataContent).data,
            (TestData.getSampleDescriptionMetaDataContent() as MetaDataContent).data,
            emptyList(),
            (TestData.getSampleKeywordsMetaDataContent() as MetaDataContent).data,
            listOf((TestData.getSampleTextContent() as TextContent).text)
        )
        Mockito.`when`(postInstanceRepository.findById(vote.url)).thenReturn(Mono.empty())
        Mockito.`when`(contentExtractionServiceClient.extractContent(vote.url))
            .thenReturn(Mono.just(contentExtractionResult))
        Mockito.`when`(postInstanceRepository.save(expectedPostInstance)).thenReturn(Mono.error(RuntimeException("An error occurred in the repository")))

        val publisher = postInstanceService.ensurePersistedPostInstance(vote)
        StepVerifier.create(publisher)
            .expectSubscription()
            .expectNext(true)
            .expectComplete()
            .log()
            .verify()

        Mockito.verify(contentExtractionServiceClient, after(500)).extractContent(vote.url)
        Mockito.verify(postInstanceRepository, after(500)).save(expectedPostInstance)
    }
}

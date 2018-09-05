package com.clickbait.defeater.clickbaitservice.update.service.post

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import com.clickbait.defeater.clickbaitservice.update.model.content.toPostInstance
import com.clickbait.defeater.clickbaitservice.update.persistence.PostInstanceRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import mu.KLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DefaultPostInstanceService(
    private val postInstanceRepository: PostInstanceRepository,
    private val contentExtractionServiceClient: ContentExtractionServiceClient
) : PostInstanceService {

    override fun findById(id: String): Mono<PostInstance> {
        return postInstanceRepository.findById(id)
    }

    override fun ensurePersistedPostInstance(vote: ClickBaitVote): Mono<Boolean> {
        return findById(vote.url)
            .map { true }
            .switchIfEmpty(
                Mono.defer {
                    contentExtractionServiceClient
                        .extractContent(vote.url)
                        .publishOn(Schedulers.elastic())
                        .subscribeOn(Schedulers.elastic())
                        .map { it.toPostInstance(vote.postText) }
                        .flatMap {
                            logger.info("Post Instance arrived, persisting...")
                            postInstanceRepository.save(it)
                        }
                        .subscribe()
                    Mono.just(true)
                }
            )
    }

    companion object : KLogging()
}
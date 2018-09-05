package com.clickbait.defeater.clickbaitservice.update.service.post

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import com.clickbait.defeater.clickbaitservice.update.persistence.PostInstanceRepository
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentExtractionServiceClient
import com.clickbait.defeater.clickbaitservice.update.service.post.client.ContentMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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

    override fun ensurePersistedPostInstance(vote: ClickBaitVote): Mono<PostInstance> {
        return findById(vote.url)
            .switchIfEmpty(
                Mono.defer {
                    contentExtractionServiceClient
                        .extractContent(vote.url)
                        .map {
                            ContentMapper.toCompletePostInstance(
                                PostInstance(vote.url, postText = vote.postText),
                                it
                            )
                        }
                        .flatMap { postInstanceRepository.save(it) }
                }
            )
    }
}
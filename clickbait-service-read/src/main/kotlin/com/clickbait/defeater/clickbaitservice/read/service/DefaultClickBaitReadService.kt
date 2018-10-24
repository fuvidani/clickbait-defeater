package com.clickbait.defeater.clickbaitservice.read.service

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.language.checker.LanguageChecker
import com.clickbait.defeater.clickbaitservice.read.service.score.ScoreService
import com.clickbait.defeater.clickbaitservice.read.service.score.cache.ScoreCache
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Implementation of the [ClickBaitReadService] interface, fulfilling its contracts.
 *
 * @property scoreService a [ScoreService] responsible for determining the clickbait
 * score of a post instance
 * @property scoreCache in-memory cache of already obtained [ClickBaitScore] objects
 * @property languageChecker a language checker for post instances
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
class DefaultClickBaitReadService(
    private val scoreService: ScoreService,
    private val scoreCache: ScoreCache,
    private val languageChecker: LanguageChecker
) : ClickBaitReadService {

    /**
     * Determines the clickbait score of the provided social media post instance.
     *
     * This operation guarantees a valid [ClickBaitScore] object or an appropriate
     * exception if an error occurs.
     *
     * @param instance a valid social media post instance
     * @return a Mono publisher with the clickbait score object corresponding to the
     * provided instance
     */
    override fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore> {
        return scoreCache
            .tryAndGet(instance)
            .switchIfEmpty(
                Mono.defer {
                    languageChecker
                        .check(instance)
                        .flatMap { postInstance ->
                            scoreService
                                .scorePostInstance(postInstance)
                                .doOnNext { scoreCache.put(it).subscribe() }
                        }
                }
            )
    }
}
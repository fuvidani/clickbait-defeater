package com.clickbait.defeater.clickbaitservice.read.service

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.language.checker.LanguageChecker
import com.clickbait.defeater.clickbaitservice.read.service.score.ScoreService
import com.clickbait.defeater.clickbaitservice.read.service.score.cache.ScoreCache
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
class DefaultClickBaitReadService(
    private val scoreService: ScoreService,
    private val scoreCache: ScoreCache,
    private val languageChecker: LanguageChecker
) : ClickBaitReadService {

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
package com.clickbait.defeater.clickbaitservice.read.service

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.language.detector.ILanguageDetector
import com.clickbait.defeater.clickbaitservice.read.service.score.IScoreService
import com.clickbait.defeater.clickbaitservice.read.service.score.cache.IScoreCache
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
class ClickBaitReadService(
    private val scoreService: IScoreService,
    private val scoreCache: IScoreCache,
    private val languageDetector: ILanguageDetector
) : IClickBaitReadService {

    override fun scorePostInstance(instance: PostInstance): Mono<ClickBaitScore> {
        return scoreCache
            .tryAndGet(instance)
            .switchIfEmpty(
                Mono.defer {
                    languageDetector.detect(instance)
                        .flatMap {
                            scoreService.scorePostInstance(it)
                                .doOnNext { scoreCache.put(it).subscribe() }
                        }
                }
            )
    }
}
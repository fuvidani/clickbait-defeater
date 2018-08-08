package com.clickbait.defeater.clickbaitservice.read.web

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.IClickBaitReadService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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
@RestController("/clickbait")
class ClickBaitReadController(private val clickBaitReadService: IClickBaitReadService) {

    @PostMapping("/score")
    fun scoreMediaPost(@RequestBody instance: PostInstance): Mono<ClickBaitScore> {
        return clickBaitReadService.scorePostInstance(instance)
    }
}
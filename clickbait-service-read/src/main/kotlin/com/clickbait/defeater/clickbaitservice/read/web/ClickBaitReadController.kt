package com.clickbait.defeater.clickbaitservice.read.web

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import com.clickbait.defeater.clickbaitservice.read.service.IClickBaitReadService
import com.clickbait.defeater.clickbaitservice.read.service.exception.ClickBaitReadServiceException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
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
@RestController
@RequestMapping("/clickbait")
class ClickBaitReadController(private val clickBaitReadService: IClickBaitReadService) {

    @PostMapping("/score")
    fun scoreMediaPost(@RequestBody instance: PostInstance): Mono<ClickBaitScore> {
        return clickBaitReadService
            .scorePostInstance(instance)
            .onErrorMap(ClickBaitReadServiceException::class.java) { ResponseStatusException(it.statusMapping, it.message) }
    }
}
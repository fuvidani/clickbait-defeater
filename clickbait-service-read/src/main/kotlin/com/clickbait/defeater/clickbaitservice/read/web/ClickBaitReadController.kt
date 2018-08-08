package com.clickbait.defeater.clickbaitservice.read.web

import com.clickbait.defeater.clickbaitservice.read.model.ClickBaitScore
import com.clickbait.defeater.clickbaitservice.read.model.PostInstance
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.Random

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
class ClickBaitReadController(private val redisTemplate: ReactiveStringRedisTemplate) {

    @PostMapping("/score")
    fun scoreMediaPost(@RequestBody instance: PostInstance): Mono<ClickBaitScore> {
        return redisTemplate.opsForValue().set(instance.id, Random().nextDouble().toString())
            .flatMap { redisTemplate.opsForValue().get(instance.id) }
            .map { ClickBaitScore(instance.id, it.toDouble()) }
    }
}
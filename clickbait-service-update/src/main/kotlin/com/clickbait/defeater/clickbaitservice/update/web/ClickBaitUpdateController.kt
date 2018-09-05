package com.clickbait.defeater.clickbaitservice.update.web

import com.clickbait.defeater.clickbaitservice.update.model.ClickBaitVote
import com.clickbait.defeater.clickbaitservice.update.model.toDecoded
import com.clickbait.defeater.clickbaitservice.update.service.ClickBaitVoteService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Flux
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
class ClickBaitUpdateController(private val clickBaitVoteService: ClickBaitVoteService) {

    @PostMapping("/vote")
    fun submitVote(@RequestBody vote: ClickBaitVote): Mono<Void> {
        return clickBaitVoteService.submitVote(vote.toDecoded())
    }

    @GetMapping("/vote")
    fun getUserVote(@RequestParam userId: String, @RequestParam url: String): Mono<ResponseEntity<ClickBaitVote>> {
        return clickBaitVoteService
            .findVote(ClickBaitVote(userId, url).toDecoded())
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(
                Mono.just(ResponseEntity.noContent().build())
            )
    }

    @GetMapping(
        "/votes",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getUserVotes(
        @RequestParam userId: String,
        @RequestParam(defaultValue = "-1") page: Int,
        @RequestParam(defaultValue = "0") size: Int
    ): Flux<ClickBaitVote> {
        val pageable = if (page <= -1 || size <= 0) {
            Pageable.unpaged()
        } else {
            PageRequest.of(page, size)
        }
        return clickBaitVoteService.findAllUserVotes(userId, pageable)
    }

    @GetMapping(
        "/votes",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_STREAM_JSON_VALUE]
    )
    fun getUserVotes(@RequestParam userId: String): Flux<ClickBaitVote> {
        return clickBaitVoteService.findAllUserVotes(userId, Pageable.unpaged())
    }
}
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
 * Reactive, non-blocking REST controller for the ClickBait Update-Service.
 * An extensive REST documentation in HTML format can be found in the resources.
 *
 * @property clickBaitVoteService an implementation of the [ClickBaitVoteService] interface
 * supporting all its operations
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/clickbait")
class ClickBaitUpdateController(private val clickBaitVoteService: ClickBaitVoteService) {

    /**
     * Processes a submit clickbait vote request with the provided
     * `vote`.
     *
     * @param vote a deserialized, valid [ClickBaitVote] object
     * @return an empty Mono or an empty response in case of a
     * blocking request
     */
    @PostMapping("/vote")
    fun submitVote(@RequestBody vote: ClickBaitVote): Mono<Void> {
        return clickBaitVoteService.submitVote(vote.toDecoded())
    }

    /**
     * Processes a request for retrieving an existing [ClickBaitVote].
     *
     * @param userId the unique ID of the user, must be provided
     * @param url the URL of the web page the required vote corresponds to,
     * must be provided
     * @return a Mono emitting the found [ClickBaitVote] or
     * [org.springframework.http.HttpStatus.NO_CONTENT] if there is no such vote
     */
    @GetMapping("/vote")
    fun getUserVote(@RequestParam userId: String, @RequestParam url: String): Mono<ResponseEntity<ClickBaitVote>> {
        return clickBaitVoteService
            .findVote(ClickBaitVote(userId, url).toDecoded())
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(
                Mono.just(ResponseEntity.noContent().build())
            )
    }

    /**
     * Processes a request for getting all votes for a specific user.
     * The request may be optionally paginated.
     *
     * @param userId the unique ID of the user, must be provided
     * @param page the number of the desired page when using
     * pagination; defaults to -1 which inherently means to
     * pagination
     * @param size the number of entries to return in the page;
     * defaults to 0 which inherently means no pagination
     * @return a Flux of [ClickBaitVote] of the desired size,
     * all belonging to the `userId`
     */
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

    /**
     * Processes a request for getting all votes for a specific user.
     *
     * @param userId the unique ID of the user, must be provided
     * @return a Flux of all [ClickBaitVote] objects that belong
     * to the `userId`
     */
    @GetMapping(
        "/votes",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_STREAM_JSON_VALUE]
    )
    fun getUserVotes(@RequestParam userId: String): Flux<ClickBaitVote> {
        return clickBaitVoteService.findAllUserVotes(userId, Pageable.unpaged())
    }
}
package com.clickbait.defeater.clickbaitservice.read.service.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

/**
 * A general [RuntimeException] for errors in this service that can be propagated towards
 * the client.
 *
 * @property message the detail message of the error
 * @property statusMapping the [HttpStatus] the error should be propagated towards http clients
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class ClickBaitReadServiceException(message: String, val statusMapping: HttpStatus) : RuntimeException(message)
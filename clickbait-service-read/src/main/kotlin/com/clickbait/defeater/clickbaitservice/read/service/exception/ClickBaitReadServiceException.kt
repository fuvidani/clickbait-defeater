package com.clickbait.defeater.clickbaitservice.read.service.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class ClickBaitReadServiceException(message: String, val statusMapping: HttpStatus) : RuntimeException(message)
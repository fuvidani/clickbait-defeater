/*
 * Clickbait-Defeater
 * Copyright (c) 2018. Daniel Füvesi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
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

package com.clickbait.defeater.clickbaitservice.update.service.judgments.scheduler.components

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.clickbaitservice.update.model.*
import com.clickbait.defeater.clickbaitservice.update.persistence.JudgmentsRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.after
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
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
@RunWith(SpringRunner::class)
class JudgmentsPersistenceHandlerTest {

    private lateinit var persistenceHandler: JudgmentsPersistenceHandler
    @MockBean
    private lateinit var judgmentsRepository: JudgmentsRepository

    @Before
    fun setUp() {
        persistenceHandler = JudgmentsPersistenceHandler(judgmentsRepository)
    }

    @Test
    fun `GIVEN an empty judgments wrapper, THEN JudgmentsRepository is not invoked`() {
        persistenceHandler.persist(MultiplePostInstanceJudgments(emptyList()))
        Mockito.verifyZeroInteractions(judgmentsRepository)
    }

    @Test
    fun `GIVEN a valid list of judgments to persist, THEN handler tries to persist them`() {
        val post = PostInstance("id", "en", emptyList())
        val judgment = PostInstanceJudgments(
            post,
            PostInstanceJudgmentStats(post.id, listOf(0.0, 0.0, 0.0))
        )
        val wrapper = MultiplePostInstanceJudgments(listOf(judgment, judgment, judgment))

        Mockito.`when`(judgmentsRepository.saveAll(wrapper)).thenReturn(Mono.empty())

        persistenceHandler.persist(wrapper)

        Mockito.verify(judgmentsRepository, after(200)).saveAll(wrapper)
    }

    @Test
    fun `GIVEN an arbitrary error during persisting, THEN handler silently fails`() {
        val post = PostInstance("id", "en", emptyList())
        val judgment = PostInstanceJudgments(
            post,
            PostInstanceJudgmentStats(post.id, listOf(0.0, 0.0, 0.0))
        )
        val wrapper = MultiplePostInstanceJudgments(listOf(judgment))

        Mockito.`when`(judgmentsRepository.saveAll(wrapper))
            .thenReturn(Mono.error(RuntimeException("Some error occurred")))

        persistenceHandler.persist(wrapper)

        Mockito.verify(judgmentsRepository, after(200)).saveAll(wrapper)
    }
}
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

package com.clickbait.defeater.contentextraction.service.html.extractor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Indicates that an annotated class is an implementation of the
 * {@link com.clickbait.defeater.contentextraction.service.html.extractor.Extractor}
 * interface and can be used for automatic dependency injection.
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface ExtractorBean {

    /**
     * The order value indicating the priority of
     * this bean.
     *
     * <p>Default is {@link Integer#MAX_VALUE}.
     */
    int order() default Integer.MAX_VALUE;
}

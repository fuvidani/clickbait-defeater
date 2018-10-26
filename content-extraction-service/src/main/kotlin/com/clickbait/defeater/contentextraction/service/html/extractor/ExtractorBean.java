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

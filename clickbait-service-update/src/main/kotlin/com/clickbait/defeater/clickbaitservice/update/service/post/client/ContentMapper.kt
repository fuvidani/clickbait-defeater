package com.clickbait.defeater.clickbaitservice.update.service.post.client

import com.clickbait.defeater.clickbaitservice.update.model.PostInstance
import com.clickbait.defeater.clickbaitservice.update.model.content.ContentWrapper
import com.clickbait.defeater.clickbaitservice.update.model.content.MetaDataContent
import com.clickbait.defeater.clickbaitservice.update.model.content.MetaDataType
import com.clickbait.defeater.clickbaitservice.update.model.content.TextContent

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class ContentMapper private constructor() {

    companion object {
        fun toCompletePostInstance(incompleteInstance: PostInstance, contents: ContentWrapper): PostInstance {
            var timeStamp = ""
            var targetTitle = ""
            var targetDescription = ""
            var targetKeyWords = ""
            var language: String? = null
            val targetParagraphs: MutableList<String> = mutableListOf()
            contents.contents
                .stream()
                .forEach {
                    if (it is TextContent) {
                        targetParagraphs.add(it.text)
                    } else if (it is MetaDataContent) {
                        when {
                            it.type == MetaDataType.TIMESTAMP -> timeStamp = it.data
                            it.type == MetaDataType.DESCRIPTION -> targetDescription = it.data
                            it.type == MetaDataType.TITLE -> targetTitle = it.data
                            it.type == MetaDataType.KEYWORDS -> targetKeyWords = it.data
                            it.type == MetaDataType.LANGUAGE -> language = it.data
                        }
                    }
                }
            return PostInstance(incompleteInstance.id,
                language ?: incompleteInstance.language,
                incompleteInstance.postText,
                timeStamp, incompleteInstance.postMedia,
                targetTitle,
                targetDescription,
                targetKeyWords,
                targetParagraphs)
        }
    }
}
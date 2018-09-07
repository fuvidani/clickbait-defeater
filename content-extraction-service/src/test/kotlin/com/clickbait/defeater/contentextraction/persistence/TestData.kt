package com.clickbait.defeater.contentextraction.persistence

/* ktlint-disable no-wildcard-imports */
import com.clickbait.defeater.contentextraction.model.*

/**
 * <h4>About this class</h4>
 *
 * <p>Description</p>
 *
 * @author Daniel Fuevesi
 * @version 1.0.0
 * @since 1.0.0
 */
class TestData {

    companion object {

        fun getSampleContentWrapper(url: String): ContentWrapper {
            return ContentWrapper(
                url,
                url,
                listOf(
                    getSampleTextContent(),
                    getSampleImageMediaContent(),
                    getSampleVideoMediaContent(),
                    getSampleAudioMediaContent(),
                    getSampleTitleMetaDataContent(),
                    getSampleKeywordsMetaDataContent(),
                    getSampleDescriptionMetaDataContent(),
                    getSampleLanguageMetaDataContent(),
                    getSampleTimestampMetaDataContent(),
                    getSampleInstagramSocialMediaContent(),
                    getSampleTwitterSocialMediaContent()
                )
            )
        }

        fun getSampleTextContent(): Content {
            return TextContent("After all this time? - Always.")
        }

        fun getSampleImageMediaContent(): Content {
            return MediaContent(MediaType.IMAGE, "http://guess-this.com/image")
        }

        fun getSampleVideoMediaContent(): Content {
            return MediaContent(MediaType.VIDEO, "http://guess-this.com/video/stream")
        }

        fun getSampleAudioMediaContent(): Content {
            return MediaContent(MediaType.AUDIO, "http://guess-this.com/audio/stream")
        }

        fun getSampleTitleMetaDataContent(): Content {
            return MetaDataContent(MetaDataType.TITLE, "This is an article you'll never forget")
        }

        fun getSampleKeywordsMetaDataContent(): Content {
            return MetaDataContent(MetaDataType.KEYWORDS, "apple, kiwi, banana, hakuna, matata")
        }

        fun getSampleDescriptionMetaDataContent(): Content {
            return MetaDataContent(
                MetaDataType.DESCRIPTION,
                "This sentence is tirelessly trying to describe the content of this page."
            )
        }

        fun getSampleLanguageMetaDataContent(): Content {
            return MetaDataContent(MetaDataType.LANGUAGE, "en")
        }

        fun getSampleTimestampMetaDataContent(): Content {
            return MetaDataContent(MetaDataType.TIMESTAMP, "2018-08-13T16:23:25+02:00")
        }

        fun getSampleInstagramSocialMediaContent(): Content {
            return SocialMediaContent(SocialMediaEmbeddingType.INSTAGRAM, "http://instagram.com/something")
        }

        fun getSampleTwitterSocialMediaContent(): Content {
            return SocialMediaContent(SocialMediaEmbeddingType.TWITTER, "http://twitter.com/something")
        }
    }
}
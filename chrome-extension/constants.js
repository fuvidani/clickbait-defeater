/* background messaging */
const PREDICT_ARTICLE_SCORE = "PREDICT_ARTICLE_SCORE";
const VOTE_ARTICLE_SCORE = "VOTE_ARTICLE_SCORE";
const RETRIEVE_ARTICLE_SCORE_FOR_USER = "RETRIEVE_ARTICLE_SCORE_FOR_USER";
const EXTRACT_CONTENT = "EXTRACT_CONTENT";

const USER_ID = "USER_ID";

/* temporary */
const testExtractionResponse = {
    "redirectUrl": "https://l.facebook.com/l.php?u=http%3A%2F%2Fwww.thesportbible.com%2Ffootball%2Fnews-the-201819-champions-league-group-stage-draw-20180830%3Fc%3D1535648755050&h=AT2vJlSbVPsEF7qmmeWalBO-XkVpvT4lIlEtIMre91p7eul1rAfaqIr-12NqPT9uJtbEuYZ-j0cYfscjoZemrQnJ_HKrBG99kb6KXFjIhWFt7rm5WJ7qcPoV8NkvMh9tU6bR_JKbu8_ilUvN81tTzw5BFP8svIw--8yWfUyPQvfC2bHG7gVzoQC0W8y8HHHjj1GKgTjOiddyLRnBYASFk-0Rzll7_FO9IfEQgWs1MCs32mQZxEJEk3JRkoWzgCr85YwNTRhBZZ2vkncnAKywVtsBWbnUrvCEdUeDawcHHmR3sIkDQY26q7ucCu_nj7f4mauZcMlQCW4rdzJ5xldyDHruWjwFcFTDtYQ",
    "sourceUrl": "http://www.sportbible.com/football/news-the-201819-champions-league-group-stage-draw-20180830?c=1535648755050",
    "contents": [
        {
            "type": "LANGUAGE",
            "data": "en",
            "contentType": "META_DATA"
        },
        {
            "type": "KEYWORDS",
            "data": "Champions League,Football,Football News",
            "contentType": "META_DATA"
        },
        {
            "type": "TITLE",
            "data": "The 2018/19 Champions League Group Stage Draw",
            "contentType": "META_DATA"
        },
        {
            "type": "INSTAGRAM",
            "src": "https://www.instagram.com/p/BnNs_UHFQuZ",
            "contentType": "SOCIAL_MEDIA"
        },
        {
            "data": "With Balotelli coming back into the room and in his own little world, the Napoli man jumped out on the former Liverpool , scaring him shitless and leading to him nearly dropping his phone in panic before he saw the funny side.",
            "contentType": "TEXT"
        },
        {
            "type": "DESCRIPTION",
            "data": "The first round of Champions League games will take place on September 11-12th. ",
            "contentType": "META_DATA"
        },
        {
            "type": "IMAGE",
            "data": "http://beta.ems.ladbiblegroup.com/s3/content/808x455/bd733c055b8890b98f4e7640882ccb55.png",
            "contentType": "META_DATA"
        },
        {
            "type": "VIDEO",
            "src": "https://embed.teamcoco.com/embed/v/104496",
            "contentType": "MEDIA"
        },
        {
            "type": "TWITTER",
            "src": "https://twitter.com/ChampionsLeague/status/1035212610134843393",
            "contentType": "SOCIAL_MEDIA"
        },
        {
            "type": "TWITTER",
            "src": "https://twitter.com/WBSopron/status/1038375381814968320",
            "contentType": "SOCIAL_MEDIA"
        },
        {
            "type": "INSTAGRAM",
            "src": "https://www.instagram.com/p/BnVyzZaA0M_",
            "contentType": "SOCIAL_MEDIA"
        },
        {
            "type": "IMAGE",
            "src": "http://beta.ems.ladbiblegroup.com/s3/content/808x455/bd733c055b8890b98f4e7640882ccb55.png",
            "contentType": "MEDIA"
        },
        {
            "type": "IMAGE",
            "src": "http://beta.ems.ladbiblegroup.com/s3/content/a9bf20625b882e94484b5f1c0e66f179.jpg",
            "contentType": "MEDIA"
        },
        {
            "type": "IMAGE",
            "src": "http://beta.ems.ladbiblegroup.com/s3/content/95879d945b882e94484b604c7e2cec05.jpg",
            "contentType": "MEDIA"
        },
        {
            "type": "INSTAGRAM",
            "src": "https://www.instagram.com/p/BmaitY0A9QB",
            "contentType": "SOCIAL_MEDIA"
        }
    ]
};

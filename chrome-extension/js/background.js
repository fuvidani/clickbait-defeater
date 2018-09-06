'use strict';

chrome.runtime.onInstalled.addListener(function() {
  chrome.declarativeContent.onPageChanged.removeRules(undefined, function() {
    chrome.declarativeContent.onPageChanged.addRules([{
      conditions: [new chrome.declarativeContent.PageStateMatcher({
        pageUrl: {hostEquals: 'www.facebook.com'},
      })],
      actions: [new chrome.declarativeContent.ShowPageAction()]
    }]);
  });
});

chrome.runtime.onMessage.addListener(
    (request, sender, senderResponse) => {
        switch (request.message) {
            case 'predict_postText': {
                console.log("predict_postText called");
                console.log(request.data);
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4 && xhr.status===200) {
                        senderResponse(JSON.parse(xhr.response));
                    }
                };
                xhr.open("POST", "http://37.252.185.77:5000/predict", true);
                xhr.setRequestHeader("Content-Type", "application/json");
                // xhr.send(request.data);
                setTimeout(function(){ xhr.send(request.data); }, 3000);

                return true;
            }
            case 'score_article': {
                console.log("score_article called");
                chrome.storage.sync.get('userid', function(items) {
                    let userid = items.userid;
                    if (userid) {
                        useToken(userid);
                    } else {
                        userid = getRandomToken();
                        chrome.storage.sync.set({userid: userid}, function() {
                            useToken(userid);
                        });
                    }
                    function useToken(userid) {
                        // call backend with data and userid
                        console.log(request.data);
                        senderResponse(userid);
                    }
                });
                return true;
            }
            case 'get_previous_score': {
                console.log("get_previous_score called");
                senderResponse(request.data);
                return true;
            }
            case 'extract_content': {
                console.log("extract_content called");
                const testResponseString = {
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
                            "src": "https://www.instagram.com/p/BnNs_UHFQuZ/",
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
                            "src": "https://twitter.com/ChampionsLeague/status/1035212610134843393?ref_src=twsrc%5Etfw",
                            "contentType": "SOCIAL_MEDIA"
                        },
                        {
                            "type": "INSTAGRAM",
                            "src": "https://www.instagram.com/p/BnVyzZaA0M_/",
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
                            "html": " <header class=\"header header--shallow header--article\"> <nav class=\"nav header__nav header--article\"> </nav> </header> <div class=\"content__body\"> <section class=\"container article__container\"> <aside class=\"article__sidebar l-hide-to-ml\"> </aside> <div class=\"article__body\"> <span class=\"no-display\"> <span> <a href=\"http://www.sportbible.com/\">SPORTbible</a> </span> <span> <span>http://www.sportbible.com/assets/images/theme/logo-blk.png</span> </span> </span> <p class=\"sixteen-nine\"> <meta> <meta> <meta> </p> <p></p> <div class=\"article__content\"> <div> <p><strong>The 2018/19 Champions League group stage has been drawn out. </strong></p><p>The draw below:</p><p><strong>GROUP A</strong></p><p>Atletico Madrid</p><p>Borussia Dortmund </p><p>AS Monaco </p><p>Club Brugge </p><p><strong>GROUP B</strong></p><p>Barcelona</p><p>Tottenham Hotspurs</p><p>PSV Eindhoven</p><p>Inter Milan</p><p><strong>GROUP C</strong></p><p>Paris Saint-Germain</p><p>Napoli<br></p><p>Liverpool </p><p>Red Star Belgrade</p><p><strong>GROUP D</strong></p><p>Lokomotiv Moscow</p><p>FC Porto</p><p>Schalke </p><p>Galatasaray </p><p><strong>GROUP E</strong></p><p>Bayern Munich</p><p>Benfica</p><p>Ajax</p><p>AEK Athens</p><p><strong>GROUP F</strong></p><p>Manchester City</p><p>Shakhtar Donetsk<br></p><p>Lyon</p><p>Hoffenheim</p><p><strong>GROUP G</strong></p><p>Real Madrid</p><p>AS Roma</p><p>CSKA Moscow</p><p>Viktoria Plzen </p><p><strong>GROUP H</strong></p><p>Juventus</p><p>Manchester United</p><p>Valencia </p><p>Young Boys</p><p>Match day one takes place on 18-19th September with the final round of group games commencing on 11-12th December. The knockout round kicks-off in the new year. </p><p>The final will be played on 1st June 2019 at the Wanda Metropolitano - the home of Atletico Madrid. </p><p>Of course, Real Madrid are the defending champions following last seasons 3-1 victory against Liverpool in Kiev, Ukraine. </p><p><img src=\"http://beta.ems.ladbiblegroup.com/s3/content/a9bf20625b882e94484b5f1c0e66f179.jpg\" alt=\"Image: PA\"><br><cite>Image: PA</cite><br></p><p><img src=\"http://beta.ems.ladbiblegroup.com/s3/content/95879d945b882e94484b604c7e2cec05.jpg\" alt=\"Image: PA\"><br><cite>Image: PA</cite><br></p><p>The Spanish giants have won the competition an incredible three years on the bounce but are without Zinedine Zidane&apos;s managerial tutelage, not to mention Cristiano Ronaldo, whose transferred to Juventus, for this campaign. </p><p>Who will raise European silverware in Madrid? </p><p>Manchester City? Juventus? Barcelona? Real Madrid? Bayern Munich?</p><p>Let us know your prediction in the comment section below. </p> </div> <section class=\"author-bio__container\" id=\"author-bio\"> <p class=\"author-bio__bio\"> Nasir Jabbar is a journalist at SPORTbible. He graduated from Bath Spa University with a BA in Media Communications. He&apos;s a combat sport aficionado, and has contributed to MMA websites AddictedMMA and CagePotato. Nasir has covered some of the biggest fights, while interviewing the likes of Conor McGregor, Michael Bisping and Anthony Joshua. He&apos;s also an avid Bristol City fan. </p> </section> </div> </div> </section> </div> <section class=\"article-list article-list--next-up anchor-bottom article__next-up\"> </section> <p class=\"no-display\"> <svg> <symbol id=\"icon--arrow-down\"> <path /> <path /> </symbol> <symbol id=\"icon--arrow-left\"> <path /> <path /> </symbol> <symbol id=\"icon--arrow-right\"> <path /> <path /> </symbol> <symbol id=\"icon--arrow-up\"> <path /> <path /> </symbol> <symbol id=\"icon--camera\"> <circle /><path /> </symbol> <symbol id=\"icon--clock\"> <g> <g> <path /> <path /> </g> </g> </symbol> <symbol id=\"icon--close\"> <path /> <path /> </symbol> <symbol id=\"icon--cursor\"> <path /> </symbol> <symbol id=\"icon--email\"> <path /> </symbol> <symbol id=\"icon--facebook-messenger\"> <path /> </symbol> <symbol id=\"icon--facebook\"> <path /> </symbol> <symbol id=\"icon--instagram\"> <path /><path /><circle /> </symbol> <symbol id=\"icon--link\"> <path /> </symbol> <symbol id=\"icon--new-window\"> <path /> <path /> </symbol> <symbol id=\"icon--phone\"> <path /> </symbol> <symbol id=\"icon--play\"> <path /> </symbol> <symbol id=\"icon--snapchat\"> <path /> </symbol> <symbol id=\"icon--submit\"> <path /> </symbol> <symbol id=\"icon--twitter\"> <path /> </symbol> <symbol id=\"icon--vine\"> <g> <path /> <path /> </g> </symbol> <symbol id=\"icon--whatsapp\"> <g id=\"WA_Logo\"> <g> <path /> </g> </g> </symbol> <symbol id=\"icon--logo\"> <g id=\"Layer_2\"><g id=\"Layer_1-2\"><path /><path /><path /><path /><path /><path /><path /><path /><polygon /><path /></g></g> </symbol> <symbol id=\"icon--safari-pinned-tab\"> <metadata> Created by potrace 1.11, written by Peter Selinger 2001-2013 </metadata> <g> <path /> <path /> <path /> </g> </symbol> <symbol id=\"icon--logo-theoddsbible\"> <path /><path /><path /><path /><path /><path /><path /><path /><path /> </symbol> <symbol id=\"icon--safari-pinned-tab\"> <metadata> Created by potrace 1.11, written by Peter Selinger 2001-2013 </metadata> <g> <path /> <path /> <path /> <path /> </g> </symbol></svg> </p> ",
                            "contentType": "HTML"
                        }
                    ]
                };

                // senderResponse(testResponseString);
                setTimeout(function(){ senderResponse(testResponseString); }, 3000);
                return true;
            }
            default:
        }
    }
);

function getRandomToken() {
    const randomPool = new Uint8Array(32);
    crypto.getRandomValues(randomPool);
    let hex = '';
    for (let i = 0; i < randomPool.length; ++i) {
        hex += randomPool[i].toString(16);
    }

    return hex;
}

'use strict';

chrome.runtime.onInstalled.addListener(function () {
    chrome.declarativeContent.onPageChanged.removeRules(undefined, function () {
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
        console.log(request);
        switch (request.message) {
            case PREDICT_ARTICLE_SCORE: {
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4 && (xhr.status === 200 || xhr.status === 400)) {
                        senderResponse(JSON.parse(xhr.response));
                    }
                };
                xhr.open("POST", hostUrl + "/clickbait/score", true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.send(request.data);
                // setTimeout(function(){ xhr.send(request.data); }, 3000);

                return true;
            }
            case VOTE_ARTICLE_SCORE: {
                chrome.storage.sync.get(USER_ID, function (items) {
                    let userId = items.USER_ID;
                    if (userId) {
                        useToken(userId);
                    } else {
                        userId = getRandomToken();
                        chrome.storage.sync.set({USER_ID: userId}, function () {
                            useToken(userId);
                        });
                    }

                    function useToken(userId) {
                        // call backend with data and userId
                        senderResponse({USER_ID: userId, url: request.data.url, score: request.data.score});
                    }
                });
                return true;
            }
            case RETRIEVE_ARTICLE_SCORE_FOR_USER: {
                senderResponse(request.data);
                return true;
            }
            case EXTRACT_CONTENT: {
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4 && (xhr.status === 200)) {
                        senderResponse(JSON.parse(xhr.response));
                    }
                };
                xhr.open("GET", hostUrl + "/content?" + "url=" + request.data.url, true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.send(null);

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

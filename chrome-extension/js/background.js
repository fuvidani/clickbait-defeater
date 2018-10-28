'use strict';

/**
 * Adds listener to inject script on the right page.
 */
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

/**
 * Adds listeners to catch in-extension messages and process them accordingly.
 */
chrome.runtime.onMessage.addListener(
    (request, sender, senderResponse) => {
        if (logging) console.log(request);
        switch (request.message) {
            case PREDICT_ARTICLE_SCORE: {
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4 && (xhr.status === 200 || xhr.status === 400 || xhr.status === 500)) {
                        senderResponse(JSON.parse(xhr.response));
                    }
                };
                xhr.open("POST", hostUrl + "/clickbait/score", true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.setRequestHeader("Authorization", "Basic " + btoa("username" + ":" + "49v28G2Dvj3?F6cg3GAH&fBA.X"));
                xhr.send(request.data);

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
                        request.data.userId = userId;
                        const xhr = new XMLHttpRequest();
                        xhr.onreadystatechange = function () {
                            if (xhr.readyState === 4 && xhr.status === 200) {
                                senderResponse(true);
                            }
                        };
                        xhr.open("POST", hostUrl + "/clickbait/vote", true);
                        xhr.setRequestHeader("Content-Type", "application/json");
                        xhr.setRequestHeader("Authorization", "Basic " + btoa("username" + ":" + "49v28G2Dvj3?F6cg3GAH&fBA.X"));
                        xhr.send(JSON.stringify(request.data));
                    }
                });
                return true;
            }
            case RETRIEVE_ARTICLE_SCORE_FOR_USER: {
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
                        const xhr = new XMLHttpRequest();
                        xhr.onreadystatechange = function () {
                            if (xhr.readyState === 4 && xhr.status === 200) {
                                senderResponse(JSON.parse(xhr.response));
                            }
                        };
                        xhr.open("GET", hostUrl + "/clickbait/vote?" + "url=" + request.data.url + "&userId=" + userId, true);
                        xhr.setRequestHeader("Content-Type", "application/json");
                        xhr.setRequestHeader("Authorization", "Basic " + btoa("username" + ":" + "49v28G2Dvj3?F6cg3GAH&fBA.X"));
                        xhr.send(null);
                    }
                });
                return true;
            }
            case EXTRACT_CONTENT: {
                // senderResponse(testExtractionResponse);
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (xhr.readyState === 4 && (xhr.status === 200)) {
                        senderResponse(JSON.parse(xhr.response));
                    }
                };
                xhr.open("GET", hostUrl + "/content?" + "url=" + request.data.url, true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.setRequestHeader("Authorization", "Basic " + btoa("username" + ":" + "49v28G2Dvj3?F6cg3GAH&fBA.X"));
                xhr.send(null);
                return true;
            }
            default:
        }
    }
);

/**
 * Generates random id for extension.
 *
 * @returns {string} Generated id.
 */
function getRandomToken() {
    const randomPool = new Uint8Array(32);
    crypto.getRandomValues(randomPool);
    let hex = '';
    for (let i = 0; i < randomPool.length; ++i) {
        hex += randomPool[i].toString(16);
    }

    return hex;
}

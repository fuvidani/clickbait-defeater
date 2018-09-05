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
                xhr.send(request.data);
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

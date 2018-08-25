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
        console.log("predict_postText called");
        switch (request.message) {
            case 'predict_postText': {
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function () {
                    if (XMLHttpRequest.DONE && xhr.status===200) {
                        senderResponse(JSON.parse(xhr.response));
                    }
                };
                xhr.open("POST", "http://37.252.185.77:5000/predict", true);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.send(request.data);
                return true;
            }
            default:
        }
    }
);

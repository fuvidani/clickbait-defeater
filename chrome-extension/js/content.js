const targetNode = document.getElementById('stream_pagelet');
const config = {attributes: true, childList: true, subtree: true};
const post_ids = [];
const removed_ids = [];
const sliders = {};

const callback = function (mutationsList) {
    for (let mutation of mutationsList) {
        if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
            if (mutation.target.id !== undefined && mutation.target.id.indexOf("hyperfeed_story_id") === 0) {
                if (post_ids.indexOf(mutation.target.id) === -1 && removed_ids.indexOf(mutation.target.id) === -1) {
                    // filter out sponsored posts
                    let sp, on, so, red = false;

                    const div_list = mutation.target.getElementsByTagName('div');
                    for (let div of div_list) {
                        switch (div.innerText) {
                            case "Sp": {
                                sp = true;
                                break;
                            }
                            case "on": {
                                on = true;
                                break;
                            }
                            case "so": {
                                so = true;
                                break;
                            }
                            case "red": {
                                red = true;
                                break;
                            }
                        }
                    }

                    if (sp && on && so && red) {
                        console.log("Sponsored post");
                        break;
                    }

                    const a_list = mutation.target.getElementsByTagName('a');

                    for (let i = 0; i < a_list.length; i++) {
                        if (a_list[i].href.indexOf("https://l.facebook.com/l.php?u=http") === 0 && a_list[i].hasAttribute("tabindex") && a_list[i].closest(".commentable_item") === null) {
                            createWidget(mutation.target.id, mutation.target);

                            const extractedUrl = extractUrl(a_list[i].href);
                            console.log("extracted url: " + extractedUrl);
                            // document.getElementById(mutation.target.id + "_extract").onclick = function () {
                            //     const win = window.open(extractedUrl, '_blank');
                            //     win.focus();
                            // };

                            const p_list = mutation.target.getElementsByTagName("p");
                            let postTexts = [];
                            for (let p of p_list) {
                                if (p.innerText) {
                                    postTexts.push(p.innerText);
                                }
                            }

                            const list = mutation.target.getElementsByClassName("_3n1k");
                            if (list.length > 0) {
                                postTexts.push(list[0].firstChild.firstChild.textContent);
                            }
                            console.log("postText: " + postTexts);

                            if (postTexts.length > 0) {
                                chrome.runtime.sendMessage({
                                    message: "predict_postText",
                                    data: JSON.stringify({postText: postTexts, id: extractedUrl})
                                }, function (response) {
                                    if (response.clickbaitScore) {
                                        // document.getElementById(mutation.target.id + "_predict").innerText = (response.clickbaitScore * 100).toFixed(2) + "%";
                                    }
                                });
                            }

                            chrome.runtime.sendMessage({
                                message: "get_previous_score",
                                data: {url: extractedUrl}
                            }, function (response) {
                                console.log("Got previous score for: " + response.url);
                            });

                            sliders[mutation.target.id].on("slideStop", function (value) {
                                switch (value) {
                                    case 0: {
                                        sendArticleScore(extractedUrl, 0.0);
                                        break;
                                    }
                                    case 1: {
                                        sendArticleScore(extractedUrl, 0.33333334);
                                        break;
                                    }
                                    case 2: {
                                        sendArticleScore(extractedUrl, 0.6666667);
                                        break;
                                    }
                                    case 3: {
                                        sendArticleScore(extractedUrl, 1.0);
                                        break;
                                    }
                                }
                            });

                            break;
                        }
                    }
                }
            }
        } else if (mutation.type === 'childList' && mutation.removedNodes.length > 0) {
            for (let node of mutation.removedNodes) {
                if (node.id !== undefined && node.id.indexOf("hyperfeed_story_id") === 0) {
                    const clickbaitWidget = document.getElementById(node + "_widget");
                    if (clickbaitWidget) {
                        clickbaitWidget.remove();
                    }
                    removed_ids.push(node.id);
                    console.log("clickbait-widget removed with id: " + node.id + "_widget");
                }
            }
        }
    }
};

const observer = new MutationObserver(callback);
observer.observe(targetNode, config);

const createWidget = function (post_id, mutationTarget) {
    const widgetDiv = document.createElement('div');
    widgetDiv.id = post_id + "_widget";
    widgetDiv.classList.add("widget-container");

    const predictDiv = document.createElement('div');
    predictDiv.classList.add("predict-container");
    predictDiv.classList.add("progress");
    const predictProgress = document.createElement('div');
    predictProgress.id = post_id + "_predict";
    predictProgress.classList.add("progress-bar");
    predictProgress.classList.add("progress-bar-info");
    predictProgress.classList.add("active");
    predictProgress.classList.add("progress-bar-striped");
    predictProgress.setAttribute("role", "progressbar");
    predictProgress.setAttribute("aria-valuenow", "100");
    predictProgress.setAttribute("aria-valuemin", "0");
    predictProgress.setAttribute("aria-valuemax", "100");
    predictProgress.setAttribute("style", "width: 100%");
    predictProgress.innerText = "Loading score..";
    predictDiv.appendChild(predictProgress);

    const sliderDiv = document.createElement("div");
    sliderDiv.classList.add("widget-slider");
    const sliderInput = document.createElement("input");
    sliderInput.id = post_id + "_slider";
    sliderInput.type = "text";
    sliderDiv.appendChild(sliderInput);

    const extractDiv = document.createElement('div');
    const button = document.createElement('button');
    button.id = post_id + "_extract";
    button.setAttribute("type","button");
    button.setAttribute("data-toggle", "popover");
    button.setAttribute("title", "Popover title");
    button.setAttribute("data-content", "And here's some amazing content. It's very engaging. Right?");
    button.classList.add("btn");
    button.classList.add("btn-info");
    button.classList.add("pull-right");
    button.classList.add("extract-button");
    button.innerText = "Extract";
    extractDiv.appendChild(button);

    widgetDiv.appendChild(predictDiv);
    widgetDiv.appendChild(extractDiv);
    widgetDiv.appendChild(sliderDiv);

    post_ids.push(post_id);
    mutationTarget.parentNode.insertBefore(widgetDiv, mutationTarget);
    console.log("clickbait-widget added with id: " + post_id);

    // initialize slider
    const slider = new Slider("#" + post_id + "_slider", {
        value: 0,
        ticks: [0, 1, 2, 3],
        formatter: function (value) {
            switch (value) {
                case 0:
                    return "Not click baiting";
                case 1:
                    return "Slightly click baiting";
                case 2:
                    return "Considerably click baiting";
                case 3:
                    return "Heavily click baiting";
            }

            return "N/A"
        }
    });

    sliders[post_id] = slider;
};

const sendArticleScore = function (url, score, callback) {
    chrome.runtime.sendMessage({
        message: "score_article",
        data: {url: url, score: score}
    }, function (response) {
        console.log("scored article with id: " + response);
    });
};

const extractUrl = function (href) {
    const index = href.indexOf("http", 1);
    const uri = href.substring(index);
    let decodedUrl = decodeURIComponent(uri);
    const indexOfFirstArgument = decodedUrl.indexOf('?');
    const indexOfFirstAndMark = decodedUrl.indexOf('&');

    if (indexOfFirstArgument > -1 && indexOfFirstAndMark > -1) {
        const min = Math.min(indexOfFirstArgument, indexOfFirstAndMark);
        decodedUrl = decodedUrl.substring(0, min)
    } else if (indexOfFirstArgument > -1) {
        decodedUrl = decodedUrl.substring(0, indexOfFirstArgument)
    } else if (indexOfFirstAndMark > -1) {
        decodedUrl = decodedUrl.substring(0, indexOfFirstAndMark)
    }

    return decodedUrl
};

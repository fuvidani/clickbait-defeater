const targetNode = document.getElementById('stream_pagelet');
const config = {attributes: true, childList: true, subtree: true};
const post_ids = [];
const removed_ids = [];
const sliders = {};
const extractedIds = [];

const callback = function (mutationsList) {
    for (let mutation of mutationsList) {
        if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
            if (mutation.target.id !== undefined && mutation.target.id.indexOf("hyperfeed_story_id") === 0) {
                if (post_ids.indexOf(mutation.target.id) === -1 && removed_ids.indexOf(mutation.target.id) === -1) {
                    // filter out sponsored posts
                    let sp, on, so, red = false;

                    const spanList = mutation.target.getElementsByTagName('span');
                    for (let span of spanList) {
                        switch (span.innerText) {
                            case "Sp": {
                                if (span.offsetParent !== null) sp = true;
                                break;
                            }
                            case "on": {
                                if (span.offsetParent !== null) on = true;
                                break;
                            }
                            case "so": {
                                if (span.offsetParent !== null) so = true;
                                break;
                            }
                            case "red": {
                                if (span.offsetParent !== null) red = true;
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
                            const extractButton = document.getElementById(mutation.target.id + "_extract");
                            extractButton.onclick = function () {
                                if (extractedIds.indexOf(mutation.target.id) === -1) {
                                    chrome.runtime.sendMessage({
                                        message: EXTRACT_CONTENT,
                                        data: {url: extractedUrl}
                                    }, function (response) {
                                        const titles = response.contents.filter(content => content.contentType === "META_DATA" && content.type === "TITLE");

                                        if (titles.length > 0) {
                                            const title = titles[0].data;
                                            $(extractButton).attr('data-original-title', title);
                                        }

                                        const texts = response.contents.filter(content => content.contentType === "TEXT");

                                        let textElement = null;
                                        if (texts.length > 0) {
                                            const text = texts[0].data;
                                            textElement = document.createElement("p");
                                            textElement.innerText = text;
                                        }

                                        let contentHtml = "";
                                        if (textElement) {
                                            contentHtml += textElement.outerHTML;
                                        }

                                        const carouselElement = createCarousel(mutation.target.id, response.contents);
                                        contentHtml += carouselElement.outerHTML;

                                        $(extractButton).attr('data-content', contentHtml);
                                        $(extractButton).popover('show');
                                        $('.carousel').carousel({
                                            interval: 0
                                        });

                                        extractedIds.push(mutation.target.id);
                                    });
                                }
                            };

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
                                    message: PREDICT_ARTICLE_SCORE,
                                    data: JSON.stringify({postText: postTexts, id: extractedUrl})
                                }, function (response) {
                                    if (response.clickbaitScore) {
                                        const progressBar = document.getElementById(mutation.target.id + "_predict");
                                        const scorePercent = (response.clickbaitScore * 100).toFixed(2);
                                        progressBar.innerText = scorePercent + "%";
                                        progressBar.classList.remove("active");
                                        progressBar.setAttribute("aria-valuenow", scorePercent.toString());
                                        progressBar.setAttribute("style", "width: " + scorePercent.toString() + "%");
                                        progressBar.classList.remove("progress-bar-striped");
                                        progressBar.classList.remove("progress-bar-info");

                                        if (scorePercent < 34) {
                                            progressBar.classList.add("progress-bar-success");
                                        } else if (scorePercent >= 34 && scorePercent < 67) {
                                            progressBar.classList.add("progress-bar-warning");
                                        } else if (scorePercent >= 67) {
                                            progressBar.classList.add("progress-bar-danger");
                                        }
                                    } else if (response.message) {
                                        const progressBar = document.getElementById(mutation.target.id + "_predict");
                                        progressBar.innerText = response.message;
                                        progressBar.classList.remove("active");
                                        progressBar.setAttribute("aria-valuenow", "100");
                                        progressBar.setAttribute("style", "width: 100%");
                                        progressBar.classList.remove("progress-bar-striped");
                                    }
                                });
                            }

                            chrome.runtime.sendMessage({
                                message: RETRIEVE_ARTICLE_SCORE_FOR_USER,
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

if (targetNode) {
    const observer = new MutationObserver(callback);
    observer.observe(targetNode, config);
}

const createCarousel = function (postId, contents) {
    <!-- Container -->
    const carouselContainer = document.createElement("div");
    carouselContainer.id = postId + "_carousel";
    carouselContainer.setAttribute("data-ride", "carousel");
    carouselContainer.classList.add("carousel");
    carouselContainer.classList.add("slide");

    <!-- Indicators -->
    const indicatorList = document.createElement("ol");
    indicatorList.classList.add("carousel-indicators");

    <!-- Wrapper for slides -->
    const itemWrapper = document.createElement("div");
    itemWrapper.classList.add("carousel-inner");
    itemWrapper.setAttribute("role", "listbox");
    itemWrapper.classList.add("extract-item-wrapper");

    <!-- Controls -->
    const leftControl = document.createElement("a");
    leftControl.classList.add("left");
    leftControl.classList.add("carousel-control");
    leftControl.setAttribute("data-target", "#" + carouselContainer.id);
    leftControl.setAttribute("role", "button");
    leftControl.setAttribute("data-slide", "prev");
    const leftIcon = document.createElement("span");
    leftIcon.classList.add("icon-prev");
    leftIcon.setAttribute("aria-hidden", "true");
    leftControl.appendChild(leftIcon);

    const rightControl = document.createElement("a");
    rightControl.classList.add("right");
    rightControl.classList.add("carousel-control");
    rightControl.setAttribute("data-target", "#" + carouselContainer.id);
    rightControl.setAttribute("role", "button");
    rightControl.setAttribute("data-slide", "next");
    const rightIcon = document.createElement("span");
    rightIcon.classList.add("icon-next");
    rightIcon.setAttribute("aria-hidden", "true");
    rightControl.appendChild(rightIcon);

    let counter = 0;
    for (let content of contents) {
        let injected = false;
        if (content.contentType === "SOCIAL_MEDIA" && content.type === "INSTAGRAM") {
            itemWrapper.appendChild(createIframeItem(content.src + "/embed/captioned", counter === 0, postId, counter));
            injected = true;
        } else if (content.contentType === "MEDIA" && content.type === "IMAGE") {
            itemWrapper.appendChild(createImageItem(content.src, counter === 0));
            injected = true;
        } else if (content.contentType === "MEDIA" && content.type === "VIDEO") {
            itemWrapper.appendChild(createIframeItem(content.src, counter === 0, postId, counter));
            injected = true;
        } else if (content.contentType === "SOCIAL_MEDIA" && content.type === "TWITTER") {
            itemWrapper.appendChild(createIframeItem("https://twitframe.com/show?url=" + content.src, counter === 0, postId, counter));
            injected = true;
        } else if (content.contentType === "HTML") {
            itemWrapper.appendChild(createHtmlItem(content.html, counter === 0));
            injected = true;
        }

        if (injected) {
            const indicator = document.createElement("li");
            indicator.setAttribute("data-target", "#" + carouselContainer.id);
            indicator.setAttribute("data-slide-to", counter.toString());
            if (counter === 0) {
                indicator.classList.add("active");
            }
            // indicatorList.appendChild(indicator);
            counter++;
        }
    }

    // carouselContainer.appendChild(indicatorList);
    carouselContainer.appendChild(itemWrapper);
    carouselContainer.appendChild(leftControl);
    carouselContainer.appendChild(rightControl);

    return carouselContainer;
};

const iframeLoaded = function (iframeId) {
    console.log("HERE");
    var iFrameID = document.getElementById(iframeId);
    if (iFrameID) {
        // here you can make the height, I delete it first, then I make it again
        iFrameID.height = "";
        iFrameID.height = iFrameID.contentWindow.document.body.scrollHeight + "px";
    }
};

const createHtmlItem = function (htmlString, active) {
    const item = document.createElement("div");
    item.classList.add("item");
    if (active) item.classList.add("active");

    item.innerHTML = htmlString;

    return item;
};

const createIframeItem = function (src, active, postId, counter) {
    const item = document.createElement("div");
    item.classList.add("item");
    item.classList.add("extract-item");
    if (active) item.classList.add("active");

    const iframe = document.createElement("iframe");
    iframe.id = postId + "_" + counter + "_" + "iframe";
    iframe.setAttribute("src", src);
    iframe.setAttribute("width", "300");
    iframe.setAttribute("height", "500");
    iframe.setAttribute("frameborder", "0");
    iframe.classList.add("extract-iframe");

    const caption = document.createElement("div");
    caption.classList.add("carousel-caption");

    item.appendChild(iframe);
    item.appendChild(caption);

    return item;
};

const createImageItem = function (src, active) {
    const item = document.createElement("div");
    item.classList.add("item");
    item.classList.add("extract-item");
    if (active) item.classList.add("active");

    const img = document.createElement("img");
    img.classList.add("extract-img");
    img.src = src;

    const caption = document.createElement("div");
    caption.classList.add("carousel-caption");

    item.appendChild(img);
    // item.appendChild(caption);

    return item;
};

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
    button.setAttribute("type", "button");
    button.setAttribute("data-toggle", "popover");
    // button.setAttribute("data-trigger", "focus");
    button.setAttribute("data-html", "true");
    button.setAttribute("title", "Extracting relevant content...");
    button.setAttribute("data-content", '<div class=\"loader center-block\"></div><a href="#" target="_blank" id=' + post_id + "_popoverLink" + '></a>');
    button.classList.add("btn");
    button.classList.add("btn-info");
    button.classList.add("pull-right");
    button.classList.add("extract-button");
    button.innerText = "Extract";
    extractDiv.appendChild(button);

    widgetDiv.appendChild(predictDiv);
    widgetDiv.appendChild(extractDiv);
    widgetDiv.appendChild(sliderDiv);

    $(function () {
        $('[data-toggle="popover"]').popover();
    });

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
        message: VOTE_ARTICLE_SCORE,
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

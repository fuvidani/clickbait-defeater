const targetNode = document.getElementById('stream_pagelet');
const config = {attributes: true, childList: true, subtree: true};
const post_ids = [];
const removed_ids = [];
const sliders = {};
const extractedIds = [];

/**
 * Callback for mutation observer. It listens to changes on DOM for a particular html element.
 *
 * @param mutationsList
 */
const callback = function (mutationsList) {
    for (let mutation of mutationsList) {
        if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
            if (mutation.target.id !== undefined && mutation.target.id.indexOf("hyperfeed_story_id") === 0) {
                if (post_ids.indexOf(mutation.target.id) === -1 && removed_ids.indexOf(mutation.target.id) === -1) {
                    // filter out sponsored posts, if "Sponsored" is visible
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
                        if (logging) console.log("Sponsored post");
                        break;
                    }

                    // filter hidden posts
                    if (mutation.target.classList.contains("hidden_elem") || mutation.target.style.display === "none" || mutation.target.style.display === "none !important") {
                        if (logging) console.log("Hidden post!");
                        break;
                    }

                    // observe mutations on a tags
                    const a_list = mutation.target.getElementsByTagName('a');

                    for (let i = 0; i < a_list.length; i++) {
                        if (a_list[i].href.indexOf("https://l.facebook.com/l.php?u=http") === 0 && a_list[i].hasAttribute("tabindex") && a_list[i].closest(".commentable_item") === null) {
                            const encodedUrl = a_list[i].href;
                            const extractedUrl = extractUrl(encodedUrl);
                            if (logging) console.log("extracted url: " + extractedUrl);

                            // filter out posts with utm_source=dynamic
                            const urlObject = new URL(extractedUrl);
                            const utmSource = urlObject.searchParams.get("utm_source");
                            if (utmSource === "dynamic") {
                                if (logging) console.log("dynamic utm_source skip post", extractedUrl);
                                break;
                            }

                            // create and inject clickbait widget
                            createWidget(mutation.target.id, mutation.target);

                            // add extraction button for clickbait widget
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
                                        } else {
                                            $(extractButton).attr('data-original-title', "No title found");
                                        }

                                        const texts = response.contents.filter(content => content.contentType === "TEXT");

                                        // let textElement = null;
                                        const textContainer = document.createElement("div");
                                        textContainer.classList.add("text-container");
                                        if (texts.length > 0) {
                                            const firstText = document.createElement("p");
                                            firstText.id = mutation.target.id + "_extract_firstText";
                                            firstText.classList.add("clipped");
                                            firstText.innerText = texts[0].text;
                                            textContainer.appendChild(firstText);

                                            const collapseButton = document.createElement("a");
                                            collapseButton.id = mutation.target.id + "_extract_collapseButton";
                                            collapseButton.setAttribute("data-target", "#" + mutation.target.id + "_extract_collapseContainer");
                                            collapseButton.setAttribute("href", "#");
                                            collapseButton.setAttribute("data-toggle", "collapse");
                                            collapseButton.innerText = "Show more";
                                            collapseButton.classList.add("collapse-button");

                                            const collapseContainer = document.createElement("div");

                                            collapseContainer.id = mutation.target.id + "_extract_collapseContainer";
                                            collapseContainer.classList.add("collapse");
                                            collapseContainer.classList.add("extract-collapse");
                                            for (let i = 1; i < texts.length; i++) {
                                                const paragraph = document.createElement("p");
                                                paragraph.innerText = texts[i].text;
                                                collapseContainer.appendChild(paragraph);
                                            }
                                            const link = document.createElement("a");
                                            link.setAttribute("href", extractedUrl);
                                            link.setAttribute("target", "_blank");
                                            link.innerText = "Go to original page";
                                            link.classList.add("pull-right");
                                            textContainer.appendChild(collapseContainer);
                                            textContainer.appendChild(link);

                                            textContainer.appendChild(collapseButton);

                                        }

                                        let contentHtml = "";
                                        contentHtml += textContainer.outerHTML;

                                        const carouselElement = createCarousel(mutation.target.id, response.contents);
                                        contentHtml += carouselElement.outerHTML;

                                        $(extractButton).attr('data-content', contentHtml);
                                        $(extractButton).popover('show');
                                        $('.carousel').carousel({
                                            interval: 0
                                        });

                                        $("#" + mutation.target.id + "_extract_collapseContainer").on('show.bs.collapse', function () {
                                            if (logging) console.log("Starting collapsing", mutation.target.id);
                                            document.getElementById(mutation.target.id + "_extract_firstText").classList.remove("clipped");
                                            document.getElementById(mutation.target.id + "_extract_firstText").classList.add("large-clipped");
                                            document.getElementById(mutation.target.id + "_extract_collapseButton").innerText = "Show less";

                                            $(extractButton).popover('reposition');
                                        }).on('hide.bs.collapse', function () {
                                            if (logging) console.log("Back collapsing", mutation.target.id);
                                            document.getElementById(mutation.target.id + "_extract_firstText").classList.remove("large-clipped");
                                            document.getElementById(mutation.target.id + "_extract_firstText").classList.add("clipped");
                                            document.getElementById(mutation.target.id + "_extract_collapseButton").innerText = "Show more";
                                            document.getElementById(mutation.target.id + "_extract_firstText").scrollTop = 0;
                                            $(extractButton).popover('reposition');
                                        });

                                        extractedIds.push(mutation.target.id);
                                    });
                                }
                            };

                            // extract post texts from the post
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
                            if (logging) console.log("postText: " + postTexts);

                            // call background service for clickbait score
                            if (postTexts.length > 0) {
                                chrome.runtime.sendMessage({
                                    message: PREDICT_ARTICLE_SCORE,
                                    data: JSON.stringify({postText: postTexts, id: extractedUrl})
                                }, function (response) {
                                    if (response.clickbaitScore) {
                                        const progressBar = document.getElementById(mutation.target.id + "_predict");
                                        const scorePercent = (response.clickbaitScore * 100).toFixed(2);
                                        // progressBar.innerText = scorePercent + "%";

                                        progressBar.classList.remove("active");
                                        progressBar.setAttribute("aria-valuenow", scorePercent.toString());
                                        progressBar.setAttribute("style", "width: " + scorePercent.toString() + "%");
                                        progressBar.classList.remove("progress-bar-striped");
                                        progressBar.classList.remove("progress-bar-info");

                                        if (scorePercent < 9) {
                                            progressBar.classList.add("progress-bar-success");
                                            progressBar.innerText = "";
                                        } else if (scorePercent >= 9 && scorePercent < 22) {
                                            progressBar.classList.add("progress-bar-success");
                                            progressBar.innerText = scorePercent + "%";
                                        } else if (scorePercent >= 22 && scorePercent < 33) {
                                            progressBar.classList.add("progress-bar-success");
                                            progressBar.innerText = "Go ahead!";
                                        } else if (scorePercent >= 33 && scorePercent < 67) {
                                            progressBar.classList.add("progress-bar-warning");
                                            progressBar.innerText = "Could be click baiting";
                                        } else if (scorePercent >= 67) {
                                            progressBar.classList.add("progress-bar-danger");
                                            progressBar.innerText = "Tsss! Hands off!";
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

                            // retrieve previous score for a particular article for user
                            chrome.runtime.sendMessage({
                                message: RETRIEVE_ARTICLE_SCORE_FOR_USER,
                                data: {url: extractedUrl}
                            }, function (response) {
                                if (logging) console.log("Got previous score: " + response.vote);

                                if (response) {
                                    switch (response.vote) {
                                        case 0: {
                                            sliders[mutation.target.id].setValue(0, false, true);
                                            break;
                                        }
                                        case 0.33333334: {
                                            sliders[mutation.target.id].setValue(1, false, true);
                                            break;
                                        }
                                        case 0.6666667: {
                                            sliders[mutation.target.id].setValue(2, false, true);
                                            break;
                                        }
                                        case 1: {
                                            sliders[mutation.target.id].setValue(3, false, true);
                                            break;
                                        }
                                    }
                                }
                            });

                            // inject slider into clickbait widget
                            sliders[mutation.target.id].on("change", function (event) {
                                const widget = document.getElementById(mutation.target.id + "_widget");
                                const sliderTicksInSelection = widget.getElementsByClassName("slider-tick");
                                const sliderSection = widget.getElementsByClassName("slider-selection tick-slider-selection")[0];
                                const sliderHandle = widget.getElementsByClassName("slider-handle")[0];

                                for (let i = 0; i < sliderTicksInSelection.length; i++) {
                                    if (i > event.newValue) {
                                        sliderTicksInSelection[i].classList.remove("red-slider");
                                        sliderTicksInSelection[i].classList.remove("yellow-slider");
                                        sliderTicksInSelection[i].classList.remove("green-slider");
                                        sliderTicksInSelection[i].classList.add("neutral-slider-tick");
                                    } else {
                                        switch (event.newValue) {
                                            case 0: {
                                                sliderSection.classList.remove("red-slider");
                                                sliderSection.classList.remove("yellow-slider");
                                                sliderSection.classList.remove("green-slider");

                                                sliderHandle.classList.remove("red-slider");
                                                sliderHandle.classList.remove("yellow-slider");
                                                sliderHandle.classList.remove("green-slider");
                                                sliderHandle.classList.add("zero-slider");

                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.remove("neutral-slider-tick");
                                                break;
                                            }
                                            case 1: {
                                                sliderSection.classList.remove("red-slider");
                                                sliderSection.classList.remove("yellow-slider");
                                                sliderSection.classList.add("green-slider");

                                                sliderHandle.classList.remove("red-slider");
                                                sliderHandle.classList.remove("yellow-slider");
                                                sliderHandle.classList.add("green-slider");

                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.add("green-slider");
                                                sliderTicksInSelection[i].classList.remove("neutral-slider-tick");
                                                break;
                                            }
                                            case 2: {
                                                sliderSection.classList.remove("red-slider");
                                                sliderSection.classList.add("yellow-slider");
                                                sliderSection.classList.remove("green-slider");

                                                sliderHandle.classList.remove("red-slider");
                                                sliderHandle.classList.add("yellow-slider");
                                                sliderHandle.classList.remove("green-slider");

                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.add("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.remove("neutral-slider-tick");
                                                break;
                                            }
                                            case 3: {
                                                sliderSection.classList.add("red-slider");
                                                sliderSection.classList.remove("yellow-slider");
                                                sliderSection.classList.remove("green-slider");

                                                sliderHandle.classList.add("red-slider");
                                                sliderHandle.classList.remove("yellow-slider");
                                                sliderHandle.classList.remove("green-slider");

                                                sliderTicksInSelection[i].classList.add("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.remove("neutral-slider-tick");
                                                break;
                                            }
                                        }
                                    }
                                }
                            });

                            // add slider listeners for correctly setting color of the slider
                            sliders[mutation.target.id].on("slideStop", function (event) {
                                const value = sliders[mutation.target.id].getValue();
                                switch (value) {
                                    case 0: {
                                        sendArticleScore(extractedUrl, 0.0, postTexts);
                                        break;
                                    }
                                    case 1: {
                                        sendArticleScore(extractedUrl, 0.33333334, postTexts);
                                        break;
                                    }
                                    case 2: {
                                        sendArticleScore(extractedUrl, 0.6666667, postTexts);
                                        break;
                                    }
                                    case 3: {
                                        sendArticleScore(extractedUrl, 1.0, postTexts);
                                        break;
                                    }
                                }
                            });

                            sliders[mutation.target.id].on("slideStart", function (event) {
                                const value = sliders[mutation.target.id].getValue();
                                const widget = document.getElementById(mutation.target.id + "_widget");
                                const sliderSection = widget.getElementsByClassName("slider-selection tick-slider-selection")[0];
                                const sliderHandle = widget.getElementsByClassName("slider-handle")[0];
                                const sliderTicksInSelection = widget.getElementsByClassName("slider-tick");
                                switch (value) {
                                    case 0: {
                                        sliderSection.classList.remove("red-slider");
                                        sliderSection.classList.remove("yellow-slider");
                                        sliderSection.classList.remove("green-slider");

                                        sliderHandle.classList.remove("red-slider");
                                        sliderHandle.classList.remove("yellow-slider");
                                        sliderHandle.classList.remove("green-slider");
                                        sliderHandle.classList.add("zero-slider");
                                        for (let i = 0; i < sliderTicksInSelection.length; i++) {
                                            if (i > value) {
                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.add("neutral-slider-tick");
                                            }
                                        }
                                        break;
                                    }
                                    case 1: {
                                        sliderSection.classList.remove("red-slider");
                                        sliderSection.classList.remove("yellow-slider");
                                        sliderSection.classList.add("green-slider");

                                        sliderHandle.classList.remove("red-slider");
                                        sliderHandle.classList.remove("yellow-slider");
                                        sliderHandle.classList.add("green-slider");

                                        for (let i = 0; i < sliderTicksInSelection.length; i++) {
                                            if (i > value) {
                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.add("neutral-slider-tick");
                                            } else {
                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.add("green-slider");
                                            }
                                        }
                                        break;
                                    }
                                    case 2: {
                                        sliderSection.classList.remove("red-slider");
                                        sliderSection.classList.remove("green-slider");
                                        sliderSection.classList.add("yellow-slider");

                                        sliderHandle.classList.remove("red-slider");
                                        sliderHandle.classList.remove("green-slider");
                                        sliderHandle.classList.add("yellow-slider");

                                        for (let i = 0; i < sliderTicksInSelection.length; i++) {
                                            if (i > value) {
                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.add("neutral-slider-tick");
                                            } else {
                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.add("yellow-slider");
                                            }
                                        }
                                        break;
                                    }
                                    case 3: {
                                        sliderSection.classList.remove("green-slider");
                                        sliderSection.classList.remove("yellow-slider");
                                        sliderSection.classList.add("red-slider");

                                        sliderHandle.classList.remove("green-slider");
                                        sliderHandle.classList.remove("yellow-slider");
                                        sliderHandle.classList.add("red-slider");

                                        for (let i = 0; i < sliderTicksInSelection.length; i++) {
                                            if (i > value) {
                                                sliderTicksInSelection[i].classList.remove("red-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.add("neutral-slider-tick");
                                            } else {
                                                sliderTicksInSelection[i].classList.remove("green-slider");
                                                sliderTicksInSelection[i].classList.remove("yellow-slider");
                                                sliderTicksInSelection[i].classList.add("red-slider");
                                            }
                                        }
                                        break;
                                    }
                                }
                            });

                            break;
                        }
                    }
                }
            }
        } else if (mutation.type === 'childList' && mutation.removedNodes.length > 0) { // remove widget if post gets removed from the news feed
            for (let node of mutation.removedNodes) {
                if (node.id !== undefined && node.id.indexOf("hyperfeed_story_id") === 0 && node.id.split("_").length === 4) {
                    const clickbaitWidget = document.getElementById(node.id + "_widget");
                    if (clickbaitWidget) {
                        clickbaitWidget.remove();
                    }
                    removed_ids.push(node.id);
                    if (logging) console.log("clickbait-widget removed with id: " + node.id + "_widget");
                }
            }
        }
    }
};

if (targetNode) {
    const observer = new MutationObserver(callback);
    observer.observe(targetNode, config);
}

/**
 * Creates carousel for media view.
 *
 * @param postId: Id of post.
 * @param contents: Extracted content for post.
 * @returns {HTMLElement} containing the carousel.
 */
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

    const imageUrls = [];
    let counter = 0;
    for (let content of contents) {
        let injected = false;
        if (content.contentType === "SOCIAL_MEDIA" && content.type === "INSTAGRAM") {
            itemWrapper.appendChild(createIframeItem(content.src + "embed/captioned", counter === 0, postId, counter));
            injected = true;
        } else if (content.contentType === "MEDIA" && content.type === "IMAGE") {
            if (imageUrls.indexOf(content.src) === -1 && !content.src.startsWith("data")) {
                itemWrapper.appendChild(createImageItem(content.src, counter === 0));
                imageUrls.push(content.src);
                injected = true;
            }
        } else if (content.contentType === "MEDIA" && content.type === "VIDEO") {
            if (content.src.startsWith("https")) {
                const item = createIframeItem(content.src, counter === 0, postId, counter);
                itemWrapper.appendChild(item);
                injected = true;
            } else {
                if (logging) console.log("VIDEO with http. Skip!");
            }
        } else if (content.contentType === "SOCIAL_MEDIA" && content.type === "TWITTER") {
            itemWrapper.appendChild(createIframeItem("https://twitframe.com/show?url=" + content.src, counter === 0, postId, counter));
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

    if (counter !== 0) {
        // carouselContainer.appendChild(indicatorList);
        carouselContainer.appendChild(itemWrapper);
        carouselContainer.appendChild(leftControl);
        carouselContainer.appendChild(rightControl);

        return carouselContainer;
    } else {
        return document.createElement("div");
    }
};

/**
 * Creates Iframe element for media viewer.
 *
 * @param src: Url for media content.
 * @param active: True if first carousel slide.
 * @param postId: Id of post.
 * @param counter: Index of carousel slide.
 * @returns {HTMLElement} containing the Iframe element.
 */
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
    iframe.setAttribute("allowfullscreen", "true");
    iframe.classList.add("extract-iframe");

    const caption = document.createElement("div");
    caption.classList.add("carousel-caption");

    item.appendChild(iframe);
    // item.appendChild(caption);

    return item;
};

/**
 * Creates image element for media viewer.
 *
 * @param src: Url of image.
 * @param active: True if first slide of carousel.
 * @returns {HTMLElement} containing the image element.
 */
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

/**
 * Creates clickbait widget template and appends before facebook post element.
 *
 * @param post_id: Id of post.
 * @param mutationTarget: HTML object of target post.
 */
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
    const button = document.createElement('a');
    button.id = post_id + "_extract";
    // button.setAttribute("type", "button");
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

    $(document).on("click", function (e) {
        const $target = $(e.target);
        const isPopover = $target.is('[data-toggle=popover]');
        const inPopover = $target.closest('.popover').length > 0;

        //hide only if clicked on button or inside popover
        if (!isPopover && !inPopover) $(button).popover('hide');
    });

    $(document).on('hidden.bs.popover', function (e) {
        $(e.target).data("bs.popover").inState.click = false;
    });

    post_ids.push(post_id);
    mutationTarget.parentNode.insertBefore(widgetDiv, mutationTarget);
    if (logging) console.log("clickbait-widget added with id: " + post_id);

    // initialize slider
    const slider = new Slider("#" + post_id + "_slider", {
        value: 0,
        ticks: [0, 1, 2, 3],
        ticks_positions: [0, 33.33333, 66.66667, 100],
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
        },
        ticks_tooltip: true,
        tooltip_position: "bottom"
    });

    sliders[post_id] = slider;
};

/**
 * Calls background script to vote for an article.
 *
 * @param url: Url of article.
 * @param vote: Number between 0 and 1 referring to the user vote.
 * @param postText: List of post texts.
 * @param callback: Will be called on successful processing of voting.
 */
const sendArticleScore = function (url, vote, postText, callback) {
    chrome.runtime.sendMessage({
        message: VOTE_ARTICLE_SCORE,
        data: {url: url, vote: vote, postText: postText}
    }, function (response) {
        if (logging) console.log("scored article: ", response);
    });
};

/**
 * Extracts base url from facebook intern url scheme.
 *
 * @param uri: Uri to decode.
 * @returns {string} decoded url.
 */
const extractUrl = function (uri) {
    const decodedUrl = decodeURIComponent(uri);
    const url = new URL(decodedUrl);

    return url.searchParams.get("u");
};

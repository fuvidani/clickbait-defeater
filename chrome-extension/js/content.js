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
                                // const win = window.open(extractedUrl, '_blank');
                                // win.focus();
                                if (extractedIds.indexOf(mutation.target.id) === -1) {
                                    chrome.runtime.sendMessage({
                                        message: "extract_content",
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
                                        $('.carousel').carousel();

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
                                    message: "predict_postText",
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
    leftControl.setAttribute("href", "#" + carouselContainer.id);
    leftControl.setAttribute("role", "button");
    leftControl.setAttribute("data-slide", "prev");
    const leftIcon = document.createElement("span");
    leftIcon.classList.add("icon-prev");
    leftIcon.setAttribute("aria-hidden", "true");
    leftControl.appendChild(leftIcon);

    const rightControl = document.createElement("a");
    rightControl.classList.add("right");
    rightControl.classList.add("carousel-control");
    rightControl.setAttribute("href", "#" + carouselContainer.id);
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
            itemWrapper.appendChild(createIframeItem(content.src + "embed", counter === 0));
            injected = true;
        } else if (content.contentType === "MEDIA" && content.type === "IMAGE") {
            itemWrapper.appendChild(createImageItem(content.src, counter === 0));
            injected = true;
        } else if (content.contentType === "MEDIA" && content.type === "VIDEO") {
            itemWrapper.appendChild(createIframeItem(content.src, counter === 0));
            injected = true;
        } else if (content.contentType === "SOCIAL_MEDIA" && content.type === "TWITTER") {
            itemWrapper.appendChild(createIframeItem("https://twitframe.com/show?url=" + content.src, counter === 0));
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
            indicatorList.appendChild(indicator);
            counter++;
        }
    }

    carouselContainer.appendChild(indicatorList);
    carouselContainer.appendChild(itemWrapper);
    carouselContainer.appendChild(leftControl);
    carouselContainer.appendChild(rightControl);

    return carouselContainer;
};

const createHtmlItem = function (htmlString, active) {
    const item = document.createElement("div");
    item.classList.add("item");
    if (active) item.classList.add("active");

    item.innerHTML = htmlString;

    return item;
};

const createIframeItem = function (src, active) {
    const item = document.createElement("div");
    item.classList.add("item");
    if (active) item.classList.add("active");

    // item.innerHTML = "\u003cblockquote class=\"instagram-media\" data-instgrm-captioned data-instgrm-permalink=\"https://www.instagram.com/p/BnNs_UHFQuZ/?utm_source=ig_embed_loading\" data-instgrm-version=\"12\"style=\" background:#FFF; border:0; border-radius:3px; box-shadow:0 0 1px 0 rgba(0,0,0,0.5),0 1px 10px 0 rgba(0,0,0,0.15); margin: 1px; max-width:658px; min-width:326px; padding:0; width:99.375%; width:-webkit-calc(100% - 2px); width:calc(100% - 2px);\"\u003e\u003cdiv style=\"padding:16px;\"\u003e \u003ca href=\"https://www.instagram.com/p/BnNs_UHFQuZ/?utm_source=ig_embed_loading\" style=\" background:#FFFFFF; line-height:0; padding:0 0; text-align:center; text-decoration:none; width:100%;\" target=\"_blank\"\u003e \u003cdiv style=\" display: flex; flex-direction: row; align-items: center;\"\u003e \u003cdiv style=\"background-color: #F4F4F4; border-radius: 50%; flex-grow: 0; height: 40px; margin-right: 14px; width: 40px;\"\u003e\u003c/div\u003e \u003cdiv style=\"display: flex; flex-direction: column; flex-grow: 1; justify-content: center;\"\u003e \u003cdiv style=\" background-color: #F4F4F4; border-radius: 4px; flex-grow: 0; height: 14px; margin-bottom: 6px; width: 100px;\"\u003e\u003c/div\u003e \u003cdiv style=\" background-color: #F4F4F4; border-radius: 4px; flex-grow: 0; height: 14px; width: 60px;\"\u003e\u003c/div\u003e\u003c/div\u003e\u003c/div\u003e\u003cdiv style=\"padding: 19% 0;\"\u003e\u003c/div\u003e\u003cdiv style=\"display:block; height:50px; margin:0 auto 12px; width:50px;\"\u003e\u003csvg width=\"50px\" height=\"50px\" viewBox=\"0 0 60 60\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"\u003e\u003cg stroke=\"none\" stroke-width=\"1\" fill=\"none\" fill-rule=\"evenodd\"\u003e\u003cg transform=\"translate(-511.000000, -20.000000)\" fill=\"#000000\"\u003e\u003cg\u003e\u003cpath d=\"M556.869,30.41 C554.814,30.41 553.148,32.076 553.148,34.131 C553.148,36.186 554.814,37.852 556.869,37.852 C558.924,37.852 560.59,36.186 560.59,34.131 C560.59,32.076 558.924,30.41 556.869,30.41 M541,60.657 C535.114,60.657 530.342,55.887 530.342,50 C530.342,44.114 535.114,39.342 541,39.342 C546.887,39.342 551.658,44.114 551.658,50 C551.658,55.887 546.887,60.657 541,60.657 M541,33.886 C532.1,33.886 524.886,41.1 524.886,50 C524.886,58.899 532.1,66.113 541,66.113 C549.9,66.113 557.115,58.899 557.115,50 C557.115,41.1 549.9,33.886 541,33.886 M565.378,62.101 C565.244,65.022 564.756,66.606 564.346,67.663 C563.803,69.06 563.154,70.057 562.106,71.106 C561.058,72.155 560.06,72.803 558.662,73.347 C557.607,73.757 556.021,74.244 553.102,74.378 C549.944,74.521 548.997,74.552 541,74.552 C533.003,74.552 532.056,74.521 528.898,74.378 C525.979,74.244 524.393,73.757 523.338,73.347 C521.94,72.803 520.942,72.155 519.894,71.106 C518.846,70.057 518.197,69.06 517.654,67.663 C517.244,66.606 516.755,65.022 516.623,62.101 C516.479,58.943 516.448,57.996 516.448,50 C516.448,42.003 516.479,41.056 516.623,37.899 C516.755,34.978 517.244,33.391 517.654,32.338 C518.197,30.938 518.846,29.942 519.894,28.894 C520.942,27.846 521.94,27.196 523.338,26.654 C524.393,26.244 525.979,25.756 528.898,25.623 C532.057,25.479 533.004,25.448 541,25.448 C548.997,25.448 549.943,25.479 553.102,25.623 C556.021,25.756 557.607,26.244 558.662,26.654 C560.06,27.196 561.058,27.846 562.106,28.894 C563.154,29.942 563.803,30.938 564.346,32.338 C564.756,33.391 565.244,34.978 565.378,37.899 C565.522,41.056 565.552,42.003 565.552,50 C565.552,57.996 565.522,58.943 565.378,62.101 M570.82,37.631 C570.674,34.438 570.167,32.258 569.425,30.349 C568.659,28.377 567.633,26.702 565.965,25.035 C564.297,23.368 562.623,22.342 560.652,21.575 C558.743,20.834 556.562,20.326 553.369,20.18 C550.169,20.033 549.148,20 541,20 C532.853,20 531.831,20.033 528.631,20.18 C525.438,20.326 523.257,20.834 521.349,21.575 C519.376,22.342 517.703,23.368 516.035,25.035 C514.368,26.702 513.342,28.377 512.574,30.349 C511.834,32.258 511.326,34.438 511.181,37.631 C511.035,40.831 511,41.851 511,50 C511,58.147 511.035,59.17 511.181,62.369 C511.326,65.562 511.834,67.743 512.574,69.651 C513.342,71.625 514.368,73.296 516.035,74.965 C517.703,76.634 519.376,77.658 521.349,78.425 C523.257,79.167 525.438,79.673 528.631,79.82 C531.831,79.965 532.853,80.001 541,80.001 C549.148,80.001 550.169,79.965 553.369,79.82 C556.562,79.673 558.743,79.167 560.652,78.425 C562.623,77.658 564.297,76.634 565.965,74.965 C567.633,73.296 568.659,71.625 569.425,69.651 C570.167,67.743 570.674,65.562 570.82,62.369 C570.966,59.17 571,58.147 571,50 C571,41.851 570.966,40.831 570.82,37.631\"\u003e\u003c/path\u003e\u003c/g\u003e\u003c/g\u003e\u003c/g\u003e\u003c/svg\u003e\u003c/div\u003e\u003cdiv style=\"padding-top: 8px;\"\u003e \u003cdiv style=\" color:#3897f0; font-family:Arial,sans-serif; font-size:14px; font-style:normal; font-weight:550; line-height:18px;\"\u003e Sieh dir diesen Beitrag auf Instagram an\u003c/div\u003e\u003c/div\u003e\u003cdiv style=\"padding: 12.5% 0;\"\u003e\u003c/div\u003e \u003cdiv style=\"display: flex; flex-direction: row; margin-bottom: 14px; align-items: center;\"\u003e\u003cdiv\u003e \u003cdiv style=\"background-color: #F4F4F4; border-radius: 50%; height: 12.5px; width: 12.5px; transform: translateX(0px) translateY(7px);\"\u003e\u003c/div\u003e \u003cdiv style=\"background-color: #F4F4F4; height: 12.5px; transform: rotate(-45deg) translateX(3px) translateY(1px); width: 12.5px; flex-grow: 0; margin-right: 14px; margin-left: 2px;\"\u003e\u003c/div\u003e \u003cdiv style=\"background-color: #F4F4F4; border-radius: 50%; height: 12.5px; width: 12.5px; transform: translateX(9px) translateY(-18px);\"\u003e\u003c/div\u003e\u003c/div\u003e\u003cdiv style=\"margin-left: 8px;\"\u003e \u003cdiv style=\" background-color: #F4F4F4; border-radius: 50%; flex-grow: 0; height: 20px; width: 20px;\"\u003e\u003c/div\u003e \u003cdiv style=\" width: 0; height: 0; border-top: 2px solid transparent; border-left: 6px solid #f4f4f4; border-bottom: 2px solid transparent; transform: translateX(16px) translateY(-4px) rotate(30deg)\"\u003e\u003c/div\u003e\u003c/div\u003e\u003cdiv style=\"margin-left: auto;\"\u003e \u003cdiv style=\" width: 0px; border-top: 8px solid #F4F4F4; border-right: 8px solid transparent; transform: translateY(16px);\"\u003e\u003c/div\u003e \u003cdiv style=\" background-color: #F4F4F4; flex-grow: 0; height: 12px; width: 16px; transform: translateY(-4px);\"\u003e\u003c/div\u003e \u003cdiv style=\" width: 0; height: 0; border-top: 8px solid #F4F4F4; border-left: 8px solid transparent; transform: translateY(-4px) translateX(8px);\"\u003e\u003c/div\u003e\u003c/div\u003e\u003c/div\u003e\u003c/a\u003e \u003cp style=\" margin:8px 0 0 0; padding:0 4px;\"\u003e \u003ca href=\"https://www.instagram.com/p/BnNs_UHFQuZ/?utm_source=ig_embed_loading\" style=\" color:#000; font-family:Arial,sans-serif; font-size:14px; font-style:normal; font-weight:normal; line-height:17px; text-decoration:none; word-wrap:break-word;\" target=\"_blank\"\u003eI\u2019m the Captain now \ud83d\ude0f . Birthday Boat \ud83c\udf78ThankYou @danbilzerian \ud83c\udf89\ud83c\udf8a \ud83c\udf82 \ud83d\udcf7 @lifeoflou__ . . #thailand #adventure #island #fun #itsmybirthday #bday #girl #boat #cake #love #life #fun #party #sea #blonde #sunnies\u003c/a\u003e\u003c/p\u003e \u003cp style=\" color:#c9c8cd; font-family:Arial,sans-serif; font-size:14px; line-height:17px; margin-bottom:0; margin-top:8px; overflow:hidden; padding:8px 0 7px; text-align:center; text-overflow:ellipsis; white-space:nowrap;\"\u003eEin Beitrag geteilt von \u003ca href=\"https://www.instagram.com/chloeothen/?utm_source=ig_embed_loading\" style=\" color:#c9c8cd; font-family:Arial,sans-serif; font-size:14px; font-style:normal; font-weight:normal; line-height:17px;\" target=\"_blank\"\u003e Chloe Othen\u003c/a\u003e (@chloeothen) am \u003ctime style=\" font-family:Arial,sans-serif; font-size:14px; line-height:17px;\" datetime=\"2018-09-02T06:43:52+00:00\"\u003eSep 1, 2018 um 11:43 PDT\u003c/time\u003e\u003c/p\u003e\u003c/div\u003e\u003c/blockquote\u003e\n\u003cscript async defer src=\"//www.instagram.com/embed.js\"\u003e\u003c/script\u003e";

    const iframe = document.createElement("iframe");
    iframe.setAttribute("src", src);
    iframe.setAttribute("width", "400");
    iframe.setAttribute("height", "800");
    iframe.setAttribute("frameborder", "0");
    iframe.setAttribute("scrolling", "no");
    iframe.setAttribute("allowtransparency", "true");
    iframe.classList.add("extract-iframe");

    const caption = document.createElement("div");
    caption.classList.add("carousel-caption");

    item.appendChild(iframe);
    // item.appendChild(caption);

    return item;
};

const createImageItem = function (src, active) {
    const item = document.createElement("div");
    item.classList.add("item");
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

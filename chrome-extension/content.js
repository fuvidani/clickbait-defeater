const targetNode = document.getElementById('stream_pagelet');
const config = { attributes: true, childList: true, subtree: true };
const post_ids = [];

const callback = function(mutationsList) {
    for(let mutation of mutationsList) {
        if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
            if (mutation.target.id !== undefined && mutation.target.id.indexOf("hyperfeed_story_id") === 0) {
                if (post_ids.indexOf(mutation.target.id) === -1) {
                    const a_list = mutation.target.getElementsByTagName('a');

                    for (let i = 0; i < a_list.length; i++) {
                        if (a_list[i].href.indexOf("https://l.facebook.com/l.php?u=http") === 0 && a_list[i].hasAttribute("tabindex")) {
                            const widget = createWidget(mutation.target.id);

                            post_ids.push(mutation.target.id);
                            mutation.target.parentNode.insertBefore(widget, mutation.target);
                            console.log("clickbait-widget added with id: " + widget.id);

                            const extractedUrl = extractUrl(a_list[i].href);
                            console.log("extracted url: " + extractedUrl);
                            document.getElementById(mutation.target.id + "_extract").onclick = function () {
                                const win = window.open(extractedUrl, '_blank');
                                win.focus();
                            };

                            const p_list = mutation.target.getElementsByTagName("p");
                            let postText = null;
                            if (p_list.length > 0 && p_list[0].innerText) {
                                postText = p_list[0].innerText;
                            }
                            console.log("postText: " + postText);

                            if (postText) {
                                const postTexts = [];
                                postTexts.push(postText);
                                chrome.runtime.sendMessage({ message: "predict_postText", data: JSON.stringify({postText: postTexts, id: extractedUrl}) }, function (response) {
                                    if (response.clickbaitScore) {
                                        document.getElementById(mutation.target.id + "_predict").innerText = (response.clickbaitScore * 100).toFixed(2) + "%";
                                    }
                                });
                            }

                            break;
                        }
                    }
                }
            }
        } else if (mutation.type === 'childList' && mutation.removedNodes.length > 0) {
            if (mutation.removedNodes[0].id !== undefined && mutation.removedNodes[0].id.indexOf("hyperfeed_story_id") === 0) {
                const clickbaitWidget = document.getElementById(mutation.removedNodes[0].id + "_widget");
                if (clickbaitWidget) {
                    clickbaitWidget.remove();
                }
                console.log("clickbait-widget removed with id: " + mutation.removedNodes[0].id + "_widget");
            }
        }
    }
};

const observer = new MutationObserver(callback);
observer.observe(targetNode, config);

const createWidget = function (post_id) {
    const widgetDiv = document.createElement('div');
    widgetDiv.style.padding = "8px";
    widgetDiv.style.boxShadow = "0px 0px 1px 0px rgba(138,138,138,1)";
    widgetDiv.style.borderRadius = "3px";
    widgetDiv.style.marginTop = "30px";
    widgetDiv.style.marginBottom = "5px";
    widgetDiv.style.backgroundColor = "white";
    widgetDiv.id = post_id + "_widget";

    const predictDiv = document.createElement('div');
    predictDiv.id = post_id + "_predict";
    predictDiv.innerText = "N/A";

    const scoreDiv = document.createElement('div');
    const scoreForm = document.createElement('form');
    scoreForm.style.marginTop = "5px";
    scoreForm.style.marginBottom = "10px";

    const firstOptionDiv = document.createElement('div');
    const label1 = document.createElement('label');
    label1.innerText = "not click baiting";
    label1.htmlFor = post_id + "score_1";
    const radio1 = document.createElement('input');
    radio1.id = post_id + "score_1";
    radio1.type = "radio";
    radio1.value = "radio1";
    radio1.name = "score";
    firstOptionDiv.appendChild(label1);
    firstOptionDiv.appendChild(radio1);

    const secondOptionDiv = document.createElement('div');
    const label2 = document.createElement('label');
    label2.innerText = "slightly click baiting";
    label2.htmlFor = post_id + "score_2";
    const radio2 = document.createElement('input');
    radio2.id = post_id + "score_2";
    radio2.type = "radio";
    radio2.value = "radio2";
    radio2.name = "score";
    secondOptionDiv.appendChild(label2);
    secondOptionDiv.appendChild(radio2);

    const thirdOptionDiv = document.createElement('div');
    const label3 = document.createElement('label');
    label3.innerText = "considerably click baiting";
    label3.htmlFor = post_id + "score_3";
    const radio3 = document.createElement('input');
    radio3.id = post_id + "score_3";
    radio3.type = "radio";
    radio3.value = "radio3";
    radio3.name = "score";
    thirdOptionDiv.appendChild(label3);
    thirdOptionDiv.appendChild(radio3);

    const fourthOptionDiv = document.createElement('div');
    const label4 = document.createElement('label');
    label4.innerText = "heavily click baiting";
    label4.htmlFor = post_id + "score_4";
    const radio4 = document.createElement('input');
    radio4.id = post_id + "score_4";
    radio4.type = "radio";
    radio4.value = "radio4";
    radio4.name = "score";
    fourthOptionDiv.appendChild(label4);
    fourthOptionDiv.appendChild(radio4);

    scoreForm.appendChild(firstOptionDiv);
    scoreForm.appendChild(secondOptionDiv);
    scoreForm.appendChild(thirdOptionDiv);
    scoreForm.appendChild(fourthOptionDiv);
    scoreDiv.appendChild(scoreForm);

    const extractDiv = document.createElement('div');
    const button = document.createElement('input');
    button.id = post_id + "_extract";
    button.type = 'button';
    button.value = 'extract';
    extractDiv.appendChild(button);

    widgetDiv.appendChild(predictDiv);
    widgetDiv.appendChild(scoreDiv);
    widgetDiv.appendChild(extractDiv);

    return widgetDiv
};

const extractUrl = function (href) {
    const index = href.indexOf("http",1);
    const uri = href.substring(index);
    let decodedUrl = decodeURIComponent(uri);
    const indexOfFirstArgument = decodedUrl.indexOf('?');
    const indexOfFirstAndMark = decodedUrl.indexOf('&');

    if (indexOfFirstArgument > -1 && indexOfFirstAndMark > -1) {
        const min = Math.min(indexOfFirstArgument,indexOfFirstAndMark);
        decodedUrl = decodedUrl.substring(0,min)
    } else if (indexOfFirstArgument > -1) {
        decodedUrl = decodedUrl.substring(0,indexOfFirstArgument)
    } else if (indexOfFirstAndMark > -1) {
        decodedUrl = decodedUrl.substring(0,indexOfFirstAndMark)
    }

    return decodedUrl
};

"use strict";

function generate_result_tiles(e) {
    $(".result--section-container").empty();
    for (var s = 0, t = 0; 3 > s; t++)
        if (console.log("result_array: " + JSON.stringify(e[t], null, 4)), e[t].site && e[t].title && e[t].text && e[t].questionAuthorUrl && e[t].questionAuthor && e[t].acceptedAnswer && e[t].acceptedAnswer.answerAuthor && e[t].acceptedAnswer.answerAuthorUrl && e[t].acceptedAnswer.answer) {
            var n = '<div class="result--item-container"><div class="result--question"><div class="result--question-title"><a class="result--question-title-link result--link" href="http://' + e[t].site + '" target="_blank">Q: ' + e[t].title + '</a><span class="result--expand icon icon-expand-arrow down"></span></div><div class="result--question-content"><blockquote class="base--blockquote"><div class="result--question-content-text short">' + e[t].text + '</div><div class="result--question-author hidden"><a class="result--quetion-author-url result--link" href="http://' + e[t].questionAuthorUrl + '" target="_blank">' + e[t].questionAuthor + '</a></div></blockquote></div></div><div class="result--answer hidden"><div class="result--answer-author"><a class="result--answer-author-url result--link" href="http://' + e[t].acceptedAnswer.answerAuthorUrl + '" target="_blank">' + e[t].acceptedAnswer.answerAuthor + '</a>\'s answer:</div><div class="result--answer-content">' + e[t].acceptedAnswer.answer + '</div><div class="result--stackoverflow">2015 Stack Exchange Inc; user contributions licensed under <a class="result--stackoverflow-url result--link" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank">cc by-sa 3.0</a></div></div></div>';
            $(".result--section-container").append(n), s++
        }
    $(".result").show("slow"), $(".result--expand").click(function(e) {
        e.preventDefault();
        var s = $(this),
            t = s.closest(".result--item-container");
        s.toggleClass("result--expand_UP"), t.find(".result--question-content-text").toggleClass("short"), t.find(".result--question-author").slideToggle("slow"), t.find(".result--answer").slideToggle("slow")
    })
}

var polyglot = null; //Polyglot manages the language bundles

function get_msg_from_json_file(json_url){
//given the url of a json file which specify the strings displayed on the webpage in a certain language,
//send async Http request to load the json file into the Polyglot and translate the text into the specific language.
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var msg = JSON.parse(xmlhttp.responseText);
            polyglot = new Polyglot();
            polyglot.extend(msg);
            document.getElementById('greeting_1').innerHTML = polyglot.t("greeting_1");
            document.getElementById('greeting_2').innerHTML = polyglot.t("greeting_2");
            document.getElementById('greeting_3').innerHTML = polyglot.t("greeting_3");
            document.getElementById('greeting_4').innerHTML = polyglot.t("greeting_4");
            document.getElementById('praise').innerHTML = polyglot.t("praise");
            document.getElementById('progressing').innerHTML = polyglot.t("progressing");
            document.getElementById('answer').innerHTML = polyglot.t("answer");
           document.getElementById('term').innerHTML = polyglot.t("term");
            document.getElementById('privacy').innerHTML = polyglot.t("privacy");
            document.getElementById('build').innerHTML = polyglot.t("build");
        }
    };
    xmlhttp.open("GET", json_url, true);
    xmlhttp.send();
}



$(document).ready(function() {
    // User language detection and internationalization

    var browserLang = navigator.language || navigator.userLanguage;

    var complLang = browserLang.split('-');

    var lang    = complLang[0];
    var dialect = complLang[1];

    var lang_url = "js/" + lang + ".json";
    get_msg_from_json_file(lang_url);//send the ansyc request to set the display language once the page is loaded

//message section 8
    var e = ["How to use an apostrophe?", 'Where did "love someone to the bones" come from?', "What is the rule for adjective order?", '"by foot" VS "on foot"', "When should I use an em-dash, an en-dash, and a hyphen", 'Do you use "a" or "an" before acronyms?', 'Is it "bear" or "bare" with me?', "Should I put a comma before the last item in a list?", 'When should "no problem" replace "you\'re welcome" as a response to "thank you"?', 'Did English ever have a formal "you"?'],
        s = 0;
    $(".question-input--form").submit(function(e) {
        e.preventDefault(), $(".speech--speech-one").css("display", "none"), $(".question-input").css("display", "none"), $(".speech--speech-two").show("slow"), $(".question-input--input").val() || $(".question-input--input").val("difference between effect and affect?");
        var s = $(this).serializeArray();
        console.log("postData: " + s), s && $.ajax({
            method: "POST",
            url: "/api/askquestion",
            data: s
        }).success(function(e, s, t) {
            generate_result_tiles(e), $(".speech--speech-two").css("display", "none"), $(".footer").css("display", "none"), $(".speech--speech-three").show(), $(".learn-more").show()
        }).error(function(e, s, t) {
            console.log("error")
        })

        document.getElementById('learn').innerHTML = polyglot.t("learn");
        document.getElementById('doc_title').innerHTML = polyglot.t("doc_title");
        document.getElementById('doc_content').innerHTML = polyglot.t("doc_content");
        document.getElementById('bluemix_title').innerHTML = polyglot.t("bluemix_title");
        document.getElementById('bluemix_content').innerHTML = polyglot.t("bluemix_content");
        document.getElementById('eco_title').innerHTML = polyglot.t("eco_title");
        document.getElementById('eco_content').innerHTML = polyglot.t("eco_content");
        document.getElementById('term').innerHTML = polyglot.t("term");
        document.getElementById('privacy').innerHTML = polyglot.t("privacy");
        document.getElementById('build').innerHTML = polyglot.t("build");

    }), $(".result--another-question").click(function() {
        $(".speech--speech-three").css("display", "none"), $(".result").css("display", "none"), $(".learn-more").css("display", "none"), $(".speech--speech-one").show(), $(".question-input").show(), $(".footer").show(), $(".question-input--input").val("")
    }), $(".question-input--suggest-question").click(function() {
        var t = e[s];
        s++, s %= e.length, $(".question-input--input").val(t)
    })
    //


});

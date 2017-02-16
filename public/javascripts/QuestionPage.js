var answerForm = document.querySelector('article div#answerForm form');
var body = document.querySelector('textarea[name="body"]');
var upvote = document.querySelector('button.upvote');
var downvote = document.querySelector('button.downvote');
var markdownArea = document.querySelector('article div#markdown');
var textarea = document.querySelector('article div#answerForm form textarea');

upvote.onclick = function(e) {
    vote(true);
}

downvote.onclick = function(e) {
    vote(false);
}

function vote(up) {
    var xhr = new XMLHttpRequest();
    var href = location.protocol + '//' + location.host + location.pathname;
    xhr.open("POST", href + "/vote", true);
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");

    if (up) {
        xhr.send(JSON.stringify({
            "upvote":  true
        }));
    } else {
        xhr.send(JSON.stringify({
            "upvote":  false
        }));
    }


    xhr.onreadystatechange = function() {
        var responseJson = xhr.responseText ? JSON.parse(xhr.responseText) : "";
        if (xhr.readyState == XMLHttpRequest.DONE ) {
           if (xhr.status == 200) {
               //console.log('Everything is fine');
               window.location.href = responseJson.redirect;
           }
           else if (xhr.status == 400) {
               console.log('There was an error 400');
               alert(responseJson.message);
           }
           else {
               console.log('something else');
           }
        }
    }

    return false;
}

answerForm.onsubmit = function() {

    if (readCookie("user") == null) {
        alert("請先登入");
        return false;
    }

    var xhr = new XMLHttpRequest();
    var href = location.protocol + '//' + location.host + location.pathname;
    xhr.open("POST", href + "/answer", true);
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");

    xhr.send(JSON.stringify({
        "body": markdownArea.innerHTML,
        "user": readCookie("user")
    }));

    xhr.onreadystatechange = function() {
        var responseJson = xhr.responseText ? JSON.parse(xhr.responseText) : "";
        if (xhr.readyState == XMLHttpRequest.DONE ) {
           if (xhr.status == 200) {
               location.reload(false); 
           }
           else if (xhr.status == 400) {
               console.log('There was an error 400');
               alert(responseJson.message);
           }
           else {
               console.log('something else');
           }
        }
    }

    return false;
}

window.onload = function() {

    marked.setOptions({
        renderer: new marked.Renderer(),
        gfm: true,
        tables: true,
        breaks: true,
        pedantic: true,
        sanitize: true,
        smartLists: true,
        smartypants: true,
        math: function mathify(mathcode) {
            return '<a target="_blank" href="https://chart.googleapis.com/chart?cht=tx&chl={urlmathcode}">\
                        <img src="https://chart.googleapis.com/chart?cht=tx&chl={urlmathcode}" alt="{mathcode}">\
                    <a/>'
                    .replace(/\{mathcode\}/ig, mathcode)
                    .replace(/\{urlmathcode\}/ig, encodeURIComponent(mathcode));
        }
    });

    var convertTextAreaToMarkdown = function(){
        markdownArea.innerHTML = marked(textarea.value);
    };

    textarea.addEventListener('input', convertTextAreaToMarkdown);

    convertTextAreaToMarkdown();
};

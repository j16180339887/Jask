var askForm = document.querySelector('article form#askForm');
var title = document.querySelector('form#askForm input[name="title"]');
var body = document.querySelector('form#askForm textarea[name="body"]');
var markdownArea = document.querySelector('article div#markdown');
var textarea = document.querySelector('textarea');

askForm.onsubmit = function() {

    if (readCookie("user") == null) {
        alert("請先登入");
        return false;
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/ask", true);
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");

    xhr.send(JSON.stringify({
        "title":  title.value,
        "body": markdownArea.innerHTML,
        "user": readCookie("user")
    }));

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

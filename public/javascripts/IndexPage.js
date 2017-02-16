var searchForm = document.querySelector('nav form#search');
var searchText = document.querySelector('nav form#search input[type="text"]');
var userName = document.querySelector('nav a#username');

searchForm.onsubmit = function(e) {
    window.location.href = "/search?key=" + searchText.value;
    return false;
}

function createCookie(name,value,days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        var expires = "; expires=" + date.toUTCString();
    }
    else var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

function eraseCookie(name) {
    createCookie(name,"",-1);
}

var user = readCookie("user");
    var password = readCookie("password");

    if(user) {
        var logout = document.querySelector('nav a#logout');
        userName.innerHTML = user;
        logout.innerHTML = "登出";
    }
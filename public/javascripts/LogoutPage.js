window.onload = function() {

    if(readCookie("user")) {
        eraseCookie("user");
        eraseCookie("password");
    }

    window.location.href = "/";
};
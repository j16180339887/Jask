var loginForm = document.querySelector('article form#loginForm');
var signupForm = document.querySelector('article form#signupForm');

loginForm.onsubmit = function() {
    var xhr = new XMLHttpRequest();
    var user = loginForm.querySelector("form#loginForm input#loginUser");
    var password = loginForm.querySelector("form#loginForm input#loginPassword");
    
    xhr.open("POST", "/login", true);
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");

    xhr.send(JSON.stringify({
        "user": user.value,
        "password": password.value
    }));

    xhr.onreadystatechange = function() {
        var responseJson = xhr.responseText ? JSON.parse(xhr.responseText) : "";
        if (xhr.readyState == XMLHttpRequest.DONE ) {
           if (xhr.status == 200) {
               createCookie("user", user.value, 7);
               createCookie("password", password.value, 7);
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

signupForm.onsubmit = function() {
    var xhr = new XMLHttpRequest();
    var user = signupForm.querySelector("form#signupForm input#signupUser");
    var password1 = signupForm.querySelector("form#signupForm input#signupPassword1");
    var password2 = signupForm.querySelector("form#signupForm input#signupPassword2");

    xhr.open("POST", "/singup", true);
    xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");

    xhr.send(JSON.stringify({
        "user": user.value,
        "password1": password1.value,
        "password2": password2.value
    }));

    xhr.onreadystatechange = function() {
        var responseJson = xhr.responseText ? JSON.parse(xhr.responseText) : "";
        if (xhr.readyState == XMLHttpRequest.DONE ) {
           if (xhr.status == 200) {
               createCookie("user", user.value, 7);
               createCookie("password", password1.value, 7);
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

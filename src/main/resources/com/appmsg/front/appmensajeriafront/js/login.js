const Login = {
    tryToLogin: function () {
        Bridge.tryToLogin(
            document.getElementById("username").value,
            document.getElementById("password").value
        )
    }
}

function onLoginResult(result) {
    if (result.ok) {
        console.log("Login OK, userId:", result.userId);

        Bridge.navigate("main.html");

    } else {
        alert("Usuario o contraseña incorrectos");
    }
}

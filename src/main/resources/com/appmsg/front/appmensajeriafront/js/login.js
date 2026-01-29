const Login = {
    tryToLogin: function () {
        Bridge.tryToLogin(
            document.getElementById("username").value,
            document.getElementById("password").value
        )
    }
}
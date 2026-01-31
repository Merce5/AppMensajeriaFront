const Login = {
    tryToLogin: function () {
        Bridge.log("Intentando login...");

        Bridge.tryToLogin(
            document.getElementById("username").value,
            document.getElementById("password").value
        );
    },
    register: function () {
        Bridge.register(
            document.getElementById("username-register").value,
            document.getElementById("password-register").value
        )
    },
    verifyRegister: function () {
        Bridge.verifyRegister(
            document.getElementById("code").value
        )
    }
};

window.onLoginResult = function (result) {
    // Por si alguna vez llega como string JSON
    const data = (typeof result === "string") ? JSON.parse(result) : result;

    if (data && data.ok) {
        console.log("Login OK, userId:", data.userId);

    } else {
        alert("Usuario o contraseña incorrectos");
    }
};
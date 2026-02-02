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
            document.getElementById("email-register").value,
            document.getElementById("password-register").value
        )
    },
    verifyRegister: function () {
        Bridge.verifyRegister(
            document.getElementById("code").value
        )
    }
};

window.onErrorLoginResult = function (result) {
    const data = (typeof result === "string") ? JSON.parse(result) : result;

    Bridge.log(data?.error);

    if (data && data.error) {
        document.getElementById("error-card").textContent =
            typeof data.error === "string" ? data.error : "Error";
    } else {
        console.log(data);
    }
};

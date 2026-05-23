const Login = {
    tryToLogin: function () {
        const button = document.querySelector(".btn-primary");
        if (button && button.disabled) return;

        Bridge.log("Intentando login...");
        Bridge.setLoading(true, button);
        Bridge.tryToLogin(
            document.getElementById("username").value,
            document.getElementById("password").value
        );
    },
    register: function () {
        const button = document.querySelector(".btn-primary");
        sessionStorage.setItem("registerEmail", document.getElementById("email-register").value);
        Bridge.setLoading(true, button);
        Bridge.register(
            document.getElementById("email-register").value,
            document.getElementById("password-register").value
        );
    },
    verifyRegister: function () {
        const button = document.querySelector(".btn-primary");
        Bridge.setLoading(true, button);
        Bridge.verifyRegister(document.getElementById("code").value);
    },
    resendCode: function () {
        const email = sessionStorage.getItem("registerEmail");
        const button = document.querySelector(".btn-primary");
        if (!email) {
            Bridge.log("No se encontró el email");
            return;
        }
        Bridge.setLoading(true, button);
        Bridge.resendVerificationCode(email);
    },
    goRegister: function () {
        Bridge.navigate("register.html");
    },
    goBack: function () {
        Bridge.navigate("login.html");
    },
    validateForm: function () {
        const emailInput = document.getElementById("email-register");
        const passwordInput = document.getElementById("password-register");

        const emailError = document.getElementById("email-error");
        const passwordHint = document.getElementById("password-hint");

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/;

        let isValid = true;

        if (!emailRegex.test(emailInput.value.trim())) {
            emailError.textContent = "Introduce un correo electrónico válido.";
            emailError.style.display = "block";
            emailInput.style.borderColor = "red";
            emailInput.style.color = "red";
            isValid = false;
        } else {
            emailError.style.display = "none";
            emailInput.style.color = "";
            emailInput.style.borderColor = "";
        }

        if (!passwordRegex.test(passwordInput.value)) {
            passwordHint.style.color = "red";
            passwordInput.style.borderColor = "red";
            passwordInput.style.color = "red";
            isValid = false;
        } else {
            passwordHint.style.color = "";
            passwordInput.style.borderColor = "";
        }

        if (isValid) {
            Login.register();
        }
    }
};

document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("login-form");
    if (!loginForm) return;

    loginForm.addEventListener("submit", function (event) {
        event.preventDefault();
        Login.tryToLogin();
    });
});

window.onErrorLoginResult = function (result) {
    const data = (typeof result === "string") ? JSON.parse(result) : result;
    Bridge.log(data?.error);
    const button = document.querySelector(".btn-primary");
    Bridge.setLoading(false, button);
    if (data && data.error) {
        const inputCode = document.getElementById("code")
        if (inputCode !== null) {
            inputCode.style.color = "red";
            inputCode.style.borderColor = "red"
        }
        const card = document.getElementById("error-card")
        card.textContent = typeof data.error === "string" ? data.error : "Error";
        card.style.color = "red";
        card.style.display = "block";
    } else {
        console.log(data);
    }
};

window.onSuccessResendCode = function(result) {
    const response = typeof result === "string" ? JSON.parse(result) : result;
    const card = document.getElementById("error-card");
    card.style.color = "green";
    card.style.display = "block";
    card.innerText = response.message || "Código reenviado correctamente";
    Bridge.setLoading(false, document.querySelector(".btn-primary"));
};

window.orSuccessResult = function (result) {
    const button = document.querySelector(".btn-primary");
    Bridge.setLoading(false, button);
}

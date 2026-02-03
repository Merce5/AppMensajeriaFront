const Login = {
    tryToLogin: function () {
        Bridge.log("Intentando login...");
        const button = document.querySelector(".btn-primary");
        Bridge.setLoading(true, button);
        Bridge.tryToLogin(
            document.getElementById("username").value,
            document.getElementById("password").value
        );
    },
    register: function () {
        const button = document.querySelector(".btn-primary");
        Bridge.setLoading(true, button);
        Bridge.register(
            document.getElementById("email-register").value,
            document.getElementById("password-register").value
        );
    },
    verifyRegister: function () {
        const button = document.querySelector(".btn-primary");
        Bridge.setLoading(true, button);
        Bridge.verifyRegister(
            document.getElementById("code").value
        );
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
        document.getElementById("error-card").textContent =
            typeof data.error === "string" ? data.error : "Error";
    } else {
        console.log(data);
    }
};

window.orSuccessResult = function (result) {
    const button = document.querySelector(".btn-primary");
    Bridge.setLoading(false, button);
}

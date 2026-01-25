function login() {
    try {
        app.login(
            document.getElementById("username").value,
            document.getElementById("password").value
        );
    } catch(e) {
        document.getElementById("username").value = e.message
    }
}
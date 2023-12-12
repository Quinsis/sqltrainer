<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Авторизация</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/auth.css">
</head>
<body>
<div class="wrapper">
    <main class="main">
        <form action="/login" class="main__form" method="post">
            <h1 class="main__form__title">Авторизация</h1>
            <#if activateMessage?if_exists != "" >
                <div class="main__form__input-group">
                    <small class="main__form__message <#if activateMessageType?if_exists != "">${activateMessageType}</#if>">${activateMessage}</small>
                </div>
            </#if>
            <div class="main__form__input-group">
                <label id="label_name" class="main__form__label" for="name">Логин</label>
                <input class="main__form__input" id="name" type="text" name="name" autocomplete="on" required>
            </div>
            <div class="main__form__input-group">
                <label id="label_password" class="main__form__label" for="password">Пароль</label>
                <div class="main__form__input__row password-row">
                    <input class="main__form__input" id="password" type="password" name="password" autocomplete="on" required>
                    <a href="/password/forgot" class="main__form__forget__password"><i class="fa-solid fa-triangle-exclamation"></i></a>
                    <i id="password-toggle" class="fa-regular fa-eye password-toggle"></i>
                </div>
            </div>
            <#if error?if_exists != "">${error}</#if>
            <input class="main__form__input input-submit" id="submit" type="submit" value="Войти">
            <div class="main__form__alternative">
                <small class="main__form__alternative__text">У вас нет аккаунта?
                    <a class="main__form__alternative__link" href="/signup">Создать</a>
                </small>
            </div>
        </form>
    </main>
</div>
</body>
<script>
    window.addEventListener("load", function () {
        if (document.getElementById("name").value.length > 0) {
            document.getElementById("label_name").classList.add("focused");
            document.getElementById("label_name").style.color="#636363";
        }
        if (document.getElementById("password").value.length > 0) {
            document.getElementById("label_password").classList.add("focused");
            document.getElementById("label_password").style.color="#636363";
        }
    });

    document.getElementById("password-toggle").addEventListener("click", function () {
        if (document.getElementById("password-toggle").classList.contains("toggled")) {
            document.getElementById("password-toggle").classList.remove("toggled");
            document.getElementById("password-toggle").classList.add("fa-eye");
            document.getElementById("password-toggle").classList.remove("fa-eye-slash");
            document.getElementById("password").setAttribute("type", "password");
        } else {
            document.getElementById("password-toggle").classList.add("toggled");
            document.getElementById("password-toggle").classList.add("fa-eye-slash");
            document.getElementById("password-toggle").classList.remove("fa-eye");
            document.getElementById("password").setAttribute("type", "text");
        }
    });

    document.getElementById("name").addEventListener("focus", function () {
        document.getElementById("label_name").classList.add("focused");
        document.getElementById("label_name").style.color="#10A37F";
    });
    document.getElementById("name").addEventListener("focusout", function () {
        if (document.getElementById("name").value == "") {
            document.getElementById("label_name").classList.remove("focused");
        }
        document.getElementById("label_name").style.color="#636363";
    });

    document.getElementById("password").addEventListener("focus", function () {
        document.getElementById("label_password").classList.add("focused");
        document.getElementById("label_password").style.color="#10A37F";
    });
    document.getElementById("password").addEventListener("focusout", function () {
        if (document.getElementById("password").value == "") {
            document.getElementById("label_password").classList.remove("focused");
        }
        document.getElementById("label_password").style.color="#636363";
    });
</script>
</html>
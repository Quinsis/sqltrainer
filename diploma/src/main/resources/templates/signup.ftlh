<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Регистрация</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/auth.css">
</head>
<body>
<div class="wrapper">
    <main class="main">
        <form action="/signup" class="main__form" method="post">
            <h1 class="main__form__title">Регистрация</h1>
            <div class="main__form__input-group <#if errorMessage?if_exists == "Логин уже используется">input-error</#if>">
                <label id="label_name" class="main__form__label" for="name">Логин</label>
                <input class="main__form__input" id="name" type="text" name="name"
                       <#if errorMessage?if_exists != "">value="${account.name}"</#if> required>
                <#if errorMessage?if_exists == "Логин уже используется">
                    <small class="main__form__error"><i class="fa-solid fa-circle-exclamation"></i> ${errorMessage}</small>
                </#if>
            </div>
            <div class="main__form__input-group <#if errorMessage?if_exists == "Почта уже используется">input-error</#if>">
                <label id="label_email" class="main__form__label" for="email">Почта</label>
                <input class="main__form__input" id="email" type="email" name="email"
                       <#if errorMessage?if_exists != "">value="${account.email}"</#if> required>
                <#if errorMessage?if_exists == "Почта уже используется">
                    <small class="main__form__error"><i class="fa-solid fa-circle-exclamation"></i> ${errorMessage}</small>
                </#if>
            </div>
            <div class="main__form__input-group">
                <label id="label_password" class="main__form__label" for="password">Пароль</label>
                <div class="main__form__input__row password-row">
                    <input class="main__form__input" id="password" type="password" name="password" required>
                    <small class="main__form__tips__generate"><i class="fa-solid fa-rotate"></i></small>
                    <i id="password-toggle" class="fa-regular fa-eye password-toggle"></i>
                </div>
            </div>
            <div class="main__form__tips">
                <small class="main__form__tips__text">Ваш пароль должен содержать:</small>
                <ul class="main__form__tips__list">
                    <li class="main__form__tips__item">
                        <small class="main__form__tips__item__small">Хотя бы 16 символов</small>
                    </li>
                    <li class="main__form__tips__item">
                        <small class="main__form__tips__item__small">Прописные и строчные латинские буквы</small>
                    </li>
                    <li class="main__form__tips__item">
                        <small class="main__form__tips__item__small">Цифры и символы: ! " # $ % & и т.д.</small>
                    </li>
                </ul>
            </div>
            <input class="main__form__input input-submit disabled" id="submit" type="submit" value="Зарегистрироваться" disabled>
            <div class="main__form__alternative">
                <small class="main__form__alternative__text">У вас есть аккаунт?
                    <a class="main__form__alternative__link" href="/login">Войти</a>
                </small>
            </div>
        </form>
    </main>
</div>
<script>
    let passIsValid = false;
    window.addEventListener("load", function () {
        if (document.getElementById("name").value.length > 0) {
            document.getElementById("label_name").classList.add("focused");
            document.getElementById("label_name").style.color="#636363";
        }
        if (document.getElementById("password").value.length > 0) {
            document.getElementById("label_password").classList.add("focused");
            document.getElementById("label_password").style.color="#636363";
        }
        if (document.getElementById("email").value.length > 0) {
            document.getElementById("label_email").classList.add("focused");
            document.getElementById("label_email").style.color="#636363";
        }
    });

    function validate() {
        let name = document.getElementById("name").value;
        let email = document.getElementById("email").value;
        let password = document.getElementById("password").value;

        let symbolAndDigit;
        let length;
        let latin;

        length = password.length >= 16;
        if (length) document.getElementsByClassName("main__form__tips__item__small")[0].classList.add("tip-complete");
        else document.getElementsByClassName("main__form__tips__item__small")[0].classList.remove("tip-complete");

        latin = !containsCyrillic(password) && containsLatin(password) && hasUpperCaseAndLowerCase(password);
        if (latin) document.getElementsByClassName("main__form__tips__item__small")[1].classList.add("tip-complete");
        else document.getElementsByClassName("main__form__tips__item__small")[1].classList.remove("tip-complete");

        symbolAndDigit = containsSpecSymbol(password) && containsDigit(password);
        if (symbolAndDigit) document.getElementsByClassName("main__form__tips__item__small")[2].classList.add("tip-complete");
        else document.getElementsByClassName("main__form__tips__item__small")[2].classList.remove("tip-complete");

        passIsValid = symbolAndDigit && length && latin;

        if (passIsValid) {
            document.getElementsByClassName("main__form__tips")[0].style.border="1px solid #10A37F";
            document.getElementsByClassName("main__form__tips__text")[0].innerHTML = "Все требования выполнены!";
            document.getElementsByClassName("main__form__tips__text")[0].style.color = "#10A37F";
        } else {
            document.getElementsByClassName("main__form__tips")[0].style.border="1px solid #d3d3d3";
            document.getElementsByClassName("main__form__tips__text")[0].innerHTML = "Ваш пароль должен содержать:";
            document.getElementsByClassName("main__form__tips__text")[0].style.color = "#636363";
        }

        if (name == "") {
            document.getElementById("submit").setAttribute("disabled", "true");
            document.getElementById("submit").classList.add("disabled");
        } else if (email == "") {
            document.getElementById("submit").setAttribute("disabled", "true");
            document.getElementById("submit").classList.add("disabled");
        } else if (!passIsValid) {
            document.getElementById("submit").setAttribute("disabled", "true");
            document.getElementById("submit").classList.add("disabled");
        } else {
            document.getElementById("submit").removeAttribute("disabled");
            document.getElementById("submit").classList.remove("disabled");
        }
    }
    setInterval(validate, 1);

    function hasUpperCaseAndLowerCase(object) {
        let hasUpperCase = false, hasLowerCase = false;
        for (let i = 0; i < object.length; i++) {
            if (isDigit(object.charAt(i)) || isSpecSymbol(object.charAt(i))) continue;
            if (object.charAt(i) == object.charAt(i).toUpperCase()) hasUpperCase = true;
            else if (object.charAt(i) == object.charAt(i).toLowerCase()) hasLowerCase = true;
        }
        return hasUpperCase && hasLowerCase;
    }

    function isPhone(object) {
        for (let i = 0; i < object.length; i++) {
            if (!isDigit(object.charAt(i))) return false;
        }
        return true;
    }

    function containsDigit(object) {
        for (let i = 0; i < object.length; i++) {
            if (isDigit(object.charAt(i))) return true;
        }
        return false;
    }

    function containsSpecSymbol(object) {
        for (let i = 0; i < object.length; i++) {
            if (isSpecSymbol(object.charAt(i))) return true;
        }
        return false;
    }

    function containsLatin(object) {
        for (let i = 0; i < object.length; i++) {
            if (isLatin(object.charAt(i))) return true;
        }
        return false;
    }

    function containsCyrillic(object) {
        for (let i = 0; i < object.length; i++) {
            if (isCyrillic(object.charAt(i))) return true;
        }
        return false;
    }

    function isSpecSymbol(object) {
        return /[!"#$%&'()*+,\-./:;<=>?@\[\\\]\^_`\{|\}~]/i.test(object);
    }

    function isDigit(object) {
        return /[0-9]/i.test(object);
    }

    function isLatin(object) {
        return /[a-z]/i.test(object);
    }

    function isCyrillic(object) {
        return /[а-я]/i.test(object);
    }

    function generatePassword() {
        document.getElementById("label_password").classList.add("focused");
        document.getElementById("label_password").style.color = "#636363";
        const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
        let password = "";

        while (!containsSpecSymbol(password) || !containsDigit(password) || !hasUpperCaseAndLowerCase(password)) {
            password = "";
            for (let i = 0; i < 16; i++) {
                const randomIndex = Math.floor(Math.random() * charset.length);
                password += charset.charAt(randomIndex);
            }
        }

        document.getElementById("password").value = password;
    }

    document.getElementsByClassName("main__form__tips__generate")[0].addEventListener("click", generatePassword);

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
        document.getElementById("label_name").style.color = "#10A37F";
    });
    document.getElementById("name").addEventListener("focusout", function () {
        if (document.getElementById("name").value == "") {
            document.getElementById("label_name").classList.remove("focused");
        }
        document.getElementById("label_name").style.color="#636363";
    });

    document.getElementById("password").addEventListener("focus", function () {
        document.getElementById("label_password").classList.add("focused");
        document.getElementById("label_password").style.color = "#10A37F";
    });
    document.getElementById("password").addEventListener("focusout", function () {
        if (document.getElementById("password").value == "") {
            document.getElementById("label_password").classList.remove("focused");
        }
        document.getElementById("label_password").style.color = "#636363";
    });

    document.getElementById("email").addEventListener("focus", function () {
        document.getElementById("label_email").classList.add("focused");
        document.getElementById("label_email").style.color = "#10A37F";
    });
    document.getElementById("email").addEventListener("focusout", function () {
        if (document.getElementById("email").value == "") {
            document.getElementById("label_email").classList.remove("focused");
        }
        document.getElementById("label_email").style.color = "#636363";
    });
</script>
</body>
</html>
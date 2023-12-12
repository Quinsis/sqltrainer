<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Сброс пароля</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/style.css">
    <link rel="stylesheet" href="/css/auth.css">
</head>
<body>
<div class="wrapper">
    <main class="main">
        <form action="/password/forgot" class="main__form" method="post">
            <h1 class="main__form__title title-reset">Сброс пароля</h1>
            <small class="main__form__about">Введите ваш адрес электронной почты, и мы вышлем вам инструкции по сбросу пароля.</small>
            <#if activateMessage?if_exists != "">
                <div class="main__form__input-group">
                    <small class="main__form__message <#if activateMessageType?if_exists != "">${activateMessageType}</#if>">${activateMessage}</small>
                </div>
            </#if>
            <div class="main__form__input-group <#if errorMessage?if_exists="Аккаунт с такой почтой не существует">input-error</#if>">
                <label id="label_email" class="main__form__label" for="email">Почта</label>
                <input class="main__form__input" id="email" type="email" name="email"
                       <#if errorMessage?if_exists != "">value="${email}"</#if> required>
                <#if errorMessage?if_exists == "Аккаунт с такой почтой не существует">
                    <small class="main__form__error"><i class="fa-solid fa-circle-exclamation"></i>${errorMessage?if_exists}</small>
                </#if>
            </div>
            <input class="main__form__input input-submit submit-reset" id="submit" type="submit" value="Продолжить">
            <div class="main__form__alternative">
                <small class="main__form__alternative__text">Имеете доступ к аккаунту?
                    <a class="main__form__alternative__link" href="/login">Войти</a>
                </small>
            </div>
        </form>
    </main>
</div>
<script>
    window.addEventListener("load", function () {
        if (document.getElementById("email").value.length > 0) {
            document.getElementById("label_email").classList.add("focused");
            document.getElementById("label_email").style.color="#636363";
        }
    });

    document.getElementById("email").addEventListener("focus", function () {
        document.getElementById("label_email").classList.add("focused");
        document.getElementById("label_email").style.color="#10A37F";
    });
    document.getElementById("email").addEventListener("focusout", function () {
        if (document.getElementById("email").value == "") {
            document.getElementById("label_email").classList.remove("focused");
        }
        document.getElementById("label_email").style.color="#636363";
    });
</script>
</body>
</html>
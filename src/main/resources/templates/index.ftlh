<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>SQL Тренажёр</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/index.css">
    <link rel="shortcut icon" href="/img/sql.png"/>
</head>
<body>
    <section class="main">
        <div class="navbar">
            <#if user?has_content>
                <h2>${greeting}, ${user.getName()}!</h2>
                <a class="navbar-link" href="/logout">Выйти</a>
            <#else>
                <a class="navbar-link" href="/login">Войти</a>
            </#if>
        </div>
        <div class="panel">
            <div class="left-container">
                <h1 class="panel_title">Интерактивная платформа для изучения SQL</h1>
            </div>
            <div class="panel_tools">
                <a class="tool" href="/sandbox">
                    <i class="fa-solid fa-arrow-right"></i>
                    <span class="tool-title">Песочница</span>
                    <small class="tool-desc">Создание баз данных и выполнение запросов.</small>
                </a>
                <a class="tool" href="/role">
                    <i class="fa-solid fa-arrow-right"></i>
                    <span class="tool-title">Задания</span>
                    <small class="tool-desc">Создание и выполнение заданий</small>
                </a>
            </div>
        </div>
    </section>
</body>
</html>
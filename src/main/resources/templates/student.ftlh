<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/teacher.css">
    <link rel="stylesheet" href="/css/student.css">
    <link rel="shortcut icon" href="/img/sql.png"/>
    <title>Мои задания</title>
</head>
<body>
<section>
    <div class="overlay" id="overlay"></div>
    <div class="navbar">
        <div class="navbar-header">
            <a onclick="modalOpen()" class="create-task">
                Присоединиться к заданию
                <i class="fa-solid fa-up-right-from-square"></i>
            </a>
        </div>
        <ul class="categories">
            <li class="category incomplete active">Доступные</li>
            <li class="category complete">Выполненные</li>
        </ul>
        <ul class="tasks"></ul>
        <a class="exit" href="/role">
            <span>Вернуться</span>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="icon-md"><path d="M11 3H7C5.89543 3 5 3.89543 5 5V19C5 20.1046 5.89543 21 7 21H11" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path><path d="M20 12H11M20 12L16 16M20 12L16 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
        </a>
    </div>
    <div class="playground">
        <div class="outputs">
            <span class="text-helper">Выберите ваше задание</span>
        </div>
        <div class="query">
            <span onclick="toggleTable()" id="close-table">Свернуть результат<i class="fa-solid fa-chevron-down"></i></span>
            <div class="input-wrapper">
                <div class="input-group">
                    <input type="text" id="sql-input" class="sql-input" placeholder="Отправить запрос">
                    <svg onclick="executeQuery()" id="execute-sql" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="none"
                         class="icon-sm m-1 md:m-0">
                        <path d="M.5 1.163A1 1 0 0 1 1.97.28l12.868 6.837a1 1 0 0 1 0 1.766L1.969 15.72A1 1 0 0 1 .5 14.836V10.33a1 1 0 0 1 .816-.983L8.5 8 1.316 6.653A1 1 0 0 1 .5 5.67V1.163Z"
                              fill="currentColor"></path>
                    </svg>
                </div>
            </div>
        </div>
    </div>
    <div class="modal">
        <div class="modal-header">
            <span>Код задания</span>
            <i onclick="modalClose()" class="fa-solid fa-xmark"></i>
        </div>
        <input id="code" type="text" placeholder="Введите код задания">
        <span class="join" onclick="join()">Присоединиться к заданию</span>
    </div>
</section>
</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    let activeTask = '';
    let sqlProcessing = false, queryIsValid = false;
    let tableToggle = false;
    let tableOutputWrapper, tableOutput, schemaName;

    function modalClose() {
        document.querySelector(".modal").style.display = "none";
        document.querySelector(".overlay").style.display = "none";
    }

    function modalOpen() {
        document.querySelector(".modal").style.display = "flex";
        document.querySelector(".overlay").style.display = "block";
    }

    function join() {
        $.ajax({
            type: 'POST',
            url: '/join',
            data: { code: document.querySelector("#code").value },
            dataType: 'json',
            success: function (data) {
                loadIncompleteUserTasks();
                modalClose();
                notification(data.message);
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function completeTask() {
        $.ajax({
            type: 'POST',
            url: '/setCompletedTask',
            data: { code: activeTask, isCompleted: true },
            success: function () {
                $.ajax({
                    type: 'POST',
                    url: '/sendTaskCompleteNotification',
                    data: { code: activeTask },
                    success: function () {},
                    error: function (error) {
                        console.error(error);
                    }
                });
                loadIncompleteUserTasks();
                notification("Задание сдано");
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function cancelTask() {
        $.ajax({
            type: 'POST',
            url: '/setCompletedTask',
            data: { code: activeTask, isCompleted: false },
            success: function () {
                loadCompleteUserTasks();
                $(".outputs").html("<span class='text-helper'>Выберите ваше задание</span>");
                notification("Сдача отменена");
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function loadCompleteUserTasks() {
        activeTask = '';
        $.ajax({
            type: 'POST',
            url: '/loadCompleteUserTasks',
            dataType: 'json',
            success: function (data) {
                $(".tasks").html(data.tasks);
                document.querySelector(".complete").classList.add("active");
                document.querySelector(".incomplete").classList.remove("active");
                document.querySelector(".input-group").classList.add("disable");
            },
            error: function (error) {
                console.error(error);
            }
        });
        $(".outputs").html("<span class='text-helper'>Выберите ваше задание</span>");
    }

    function loadIncompleteUserTasks() {
        activeTask = '';
        $.ajax({
            type: 'POST',
            url: '/loadIncompleteUserTasks',
            dataType: 'json',
            success: function (data) {
                $(".tasks").html(data.tasks);
                document.querySelector(".incomplete").classList.add("active");
                document.querySelector(".complete").classList.remove("active");
                document.querySelector(".input-group").classList.remove("disable");
            },
            error: function (error) {
                console.error(error);
            }
        });
        $(".outputs").html("<span class='text-helper'>Выберите ваше задание</span>");
    }

    function choose(task) {
        $.ajax({
            type: 'POST',
            url: '/loadUserTaskByCode',
            data: { code: task.getAttribute("id") },
            dataType: 'json',
            success: function (data) {
                $(".outputs").html(data.userTaskInfo);
                document.querySelector('.task.active')?.classList.remove("active");
                task.classList.add("active");
                activeTask = task.getAttribute("id");
                toggleCompleteButton();
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function toggleCompleteButton() {
        if (document.querySelector('.category.active').textContent == 'Выполненные') {
            document.querySelector(".completeTask").setAttribute("onclick", "cancelTask()");
            document.querySelector(".completeTask").textContent = "Отменить сдачу";
        } else {
            document.querySelector(".completeTask").setAttribute("onclick", "completeTask()");
            document.querySelector(".completeTask").textContent = "Сдать задание";
        }
    }

    function showAttempts(tab) {
        if (activeTask != '' && !tab.classList.contains("active")) {
            $.ajax({
                type: 'POST',
                url: '/getUserTaskAttempts',
                data: { code: activeTask },
                dataType: 'json',
                success: function (data) {
                    let attempts = document.createElement("ul");
                    attempts.classList.add("attempts");
                    attempts.innerHTML = data.attempts;
                    document.querySelector(".taskInfo").removeChild(document.querySelector(".taskInfo-description"));
                    document.querySelector(".taskInfo").appendChild(attempts);
                    document.querySelector(".taskTab.active")?.classList.remove("active");
                    tab.classList.add("active");
                    removeTable();
                },
                error: function (error) {
                    console.error(error);
                }
            });
        }
    }

    function showDescription(tab) {
        if (activeTask != '' && !tab.classList.contains("active")) {
            choose(document.getElementById(activeTask));
            document.querySelector(".taskTab.active")?.classList.remove("active");
            tab.classList.add("active");
        }
    }

    function notification(text) {
        let notification = document.createElement("span");
        setTimeout(function () {
            notification.classList.add("notification");
            notification.innerHTML = "<i class='fa-regular fa-bell'></i><span class='notification-message'></span>"
            document.querySelector("body").appendChild(notification);
            setTimeout(function () {
                notification.classList.add("show");
                notification.querySelector(".notification-message").textContent = text;
                setTimeout(function () {
                    notification.classList.remove("show");
                    setTimeout(function () {
                        document.querySelector("body").removeChild(notification);
                    }, 300);
                }, 4000);
            }, 50);
        }, 0);
    }

    document.querySelector(".incomplete").addEventListener("click", function () {
        if (!document.querySelector(".incomplete").classList.contains("active")) {
            loadIncompleteUserTasks();
        }
    });

    document.querySelector(".complete").addEventListener("click", function () {
        if (!document.querySelector(".complete").classList.contains("active")) {
            loadCompleteUserTasks();
        }
    });

    function showTable() {
        tableOutputWrapper.style.height = "200px";
        tableOutputWrapper.appendChild(tableOutput);
        document.getElementById("close-table").innerHTML = "Свернуть результат<i class='fa-solid fa-chevron-down'></i>"
        tableToggle = false;
    }

    function closeTable() {
        tableOutputWrapper.style.height = "0px";
        tableOutputWrapper.removeChild(tableOutput);
        document.getElementById("close-table").innerHTML = "Показать результат<i class='fa-solid fa-chevron-up'></i>"
        tableToggle = true;
    }

    function removeTable() {
        if (tableOutputWrapper != null) {
            document.querySelector(".query").removeChild(tableOutputWrapper);
            tableOutputWrapper = null;
            tableToggle = false;
            document.getElementById("close-table").innerHTML = "Свернуть результат<i class='fa-solid fa-chevron-down'></i>"
            document.getElementById("close-table").style.display = "none";
        }
    }

    function toggleTable() {
        if (tableOutputWrapper != null) {
            if (!tableToggle) {
                closeTable();
            } else {
                showTable();
            }
        }
    }

    function executeQuery() {
        if (!sqlProcessing && !document.querySelector(".input-group").classList.contains("disable")) {
            sqlProcessing = true;
            document.querySelector("#execute-sql").classList.add("disable");

            let sqlQuery = document.querySelector("#sql-input").value;

            if (activeTask != '' && sqlQuery.length > 0) {
                $.ajax({
                    type: 'POST',
                    url: '/executeQueryWithoutSave',
                    data: { sqlQuery: sqlQuery, schemaId: '', taskId: '', taskCode: activeTask },
                    success: function (success) {
                        if (tableOutputWrapper == null) {
                            tableOutput = document.createElement("table")
                            tableOutput.classList.add("table-output");
                        }

                        var successAnimate;
                        tableOutput.innerHTML = "<span class='error-table'>" + success + "</span>";

                        if (success.endsWith('В таблице отсутствуют записи.') || success.endsWith('запрос не вернул результатов.')) {
                            successAnimate = true;
                            queryIsValid = true;
                        } else if (success.startsWith("ERROR")) {
                            successAnimate = false;
                            queryIsValid = false;
                        } else {
                            successAnimate = true;
                            queryIsValid = true;
                            tableOutput.innerHTML = jsonToHtmlTable(success)
                        }

                        if (tableOutputWrapper == null) {
                            tableOutputWrapper = document.createElement("div");
                            tableOutputWrapper.classList.add("output-wrapper");
                            tableOutputWrapper.appendChild(tableOutput);
                            document.getElementById("close-table").style.display = "flex";
                            document.querySelector(".query").insertBefore(tableOutputWrapper, document.querySelector(".input-wrapper"));
                        } else {
                            showTable();
                        }

                        if (successAnimate) {
                            tableOutputWrapper.classList.add("animate-success-output");
                            setTimeout(function () {
                                tableOutputWrapper.classList.remove("animate-success-output");
                            }, 500);
                            sqlProcessing = false;
                        } else {
                            tableOutputWrapper.classList.add("animate-error-output");
                            setTimeout(function () {
                                tableOutputWrapper.classList.remove("animate-error-output");
                            }, 500);
                            sqlProcessing = false;
                        }
                        document.querySelector("#execute-sql").classList.remove("disable");

                        $.ajax({
                            type: 'POST',
                            url: '/checkTaskQuery',
                            data: { code: activeTask, sqlQuery: sqlQuery },
                            success: function (result) {
                                notification(result);

                                $.ajax({
                                    type: 'PUT',
                                    url: '/addUserAttempt',
                                    data: { code: activeTask, sqlQuery: sqlQuery, status: result },
                                    success: function () {},
                                    error: function (error) {
                                        console.error(error);
                                    }
                                });
                            },
                            error: function (error) {
                                console.error(error);
                            }
                        });
                    },
                    error: function () {
                        sqlProcessing = false;
                        document.querySelector("#execute-sql").classList.remove("disable");
                    }
                });
            }
        }
    }

    // Функция для преобразования JSON в HTML таблицу
    function jsonToHtmlTable(jsonData) {
        try {
            var json = JSON.parse(jsonData);

            tableHtml = "<thead>"
            tableHtml += "<tr>"
            var keys = Object.keys(json[0]);
            for (var key of keys) {
                tableHtml += "<th>" + key + "</th>"
            }
            tableHtml += "</tr>"
            tableHtml += "</thead>"

            tableHtml += "<tbody>"
            for (let i = 0; i < json.length; i++) {
                tableHtml += "<tr>"
                var values = Object.values(json[i])
                for (var value of values) {
                    tableHtml += "<td>" + value + "</td>"
                }
            }
            tableHtml += "</tbody>"
            return tableHtml;
        } catch (error) {
            return "<span class='error-table'>Таблица не имеет записей</span>";
        }
    }

    loadIncompleteUserTasks();
</script>
</html>
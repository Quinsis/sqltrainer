<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/teacher.css">
    <link rel="shortcut icon" href="/img/sql.png"/>
    <title>Панель управления учителя</title>
</head>
<body>
    <section>
        <div class="overlay" id="overlay"></div>
        <div class="navbar">
            <div class="navbar-header">
                <a class="create-task">
                    Создать задание
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="icon-md"><path fill-rule="evenodd" clip-rule="evenodd" d="M16.7929 2.79289C18.0118 1.57394 19.9882 1.57394 21.2071 2.79289C22.4261 4.01184 22.4261 5.98815 21.2071 7.20711L12.7071 15.7071C12.5196 15.8946 12.2652 16 12 16H9C8.44772 16 8 15.5523 8 15V12C8 11.7348 8.10536 11.4804 8.29289 11.2929L16.7929 2.79289ZM19.7929 4.20711C19.355 3.7692 18.645 3.7692 18.2071 4.2071L10 12.4142V14H11.5858L19.7929 5.79289C20.2308 5.35499 20.2308 4.64501 19.7929 4.20711ZM6 5C5.44772 5 5 5.44771 5 6V18C5 18.5523 5.44772 19 6 19H18C18.5523 19 19 18.5523 19 18V14C19 13.4477 19.4477 13 20 13C20.5523 13 21 13.4477 21 14V18C21 19.6569 19.6569 21 18 21H6C4.34315 21 3 19.6569 3 18V6C3 4.34314 4.34315 3 6 3H10C10.5523 3 11 3.44771 11 4C11 4.55228 10.5523 5 10 5H6Z" fill="currentColor"></path></svg>
                </a>
            </div>
            <ul class="tasks"></ul>
            <a class="exit" href="/role">
                <span>Вернуться</span>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="icon-md"><path d="M11 3H7C5.89543 3 5 3.89543 5 5V19C5 20.1046 5.89543 21 7 21H11" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path><path d="M20 12H11M20 12L16 16M20 12L16 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
            </a>
        </div>
        <div class="settings">
            <div class="connect">
                <i class="fa-solid fa-paperclip"></i>
                <small>Привязать схему</small>
            </div>
            <i class="fa-solid fa-xmark close-settings"></i>
            <div class="task-header">
                <small class="description-title">Название задания</small>
                <div class="options">
                    <input id="task-name" class="task-name" type="text" required>
                </div>
            </div>
            <div class="description">
                <small class="description-title">Описание задания</small>
                <textarea id="description-text" class="description-text" required></textarea>
            </div>
            <div class="queryWrapper">
                <small class="query-title">Запрос</small>
                <textarea id="query-text" class="query-text" required></textarea>
            </div>
            <div class="buttons">
                <span onclick="executeQuery()" id="execute-sql" class="button">Проверить запрос</span>
                <span onclick="createTask()" id="save-task" class="button disable">Сохранить задание</span>
            </div>
        </div>
        <div class="playground">
            <div class="outputs">
                <span class="text-helper">Создайте или выберите ваше задание</span>
            </div>
            <div class="query">
                <span onclick="toggleTable()" id="close-table">Свернуть результат<i class="fa-solid fa-chevron-down"></i></span>
                <div class="input-wrapper"></div>
            </div>
        </div>
        <div class="connectModal">
            <div class="modal-header">
                <span>Ваши схемы</span>
                <i class="fa-solid fa-xmark close-modal-button"></i>
            </div>
            <ul class="schemas"></ul>
            <div class="connectButtonWrapper">
                <span class="modal-button connect-button disable"><i class="fa-solid fa-check"></i> Выбрать схему</span>
            </div>
        </div>
    </section>
</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    let activeSchema = '', activeTask = '', previousEditText = '';
    let sqlProcessing = false;
    let tableToggle = false;
    let tableOutputWrapper, tableOutput, schemaName;
    let timeoutShare = false, timeoutImport = false;
    let queryIsValid = false;
    let settingsIsVisible = false;
    let taskIsVisible = false;

    const categories = document.querySelectorAll(".category");
    const connectSchemaButton = document.querySelector(".connect");
    const connectModalWindow = document.querySelector(".connectModal");
    const overlay = document.querySelector(".overlay");
    const closeModalButton = document.querySelector(".close-modal-button");
    const connectButton = document.querySelector(".connect-button");
    const section = document.querySelector("section");
    const navbar = document.querySelector(".navbar");
    const navbarHeader = document.querySelector(".navbar-header");
    const settings = document.querySelector(".settings");
    const closeSettingsButton = document.querySelector(".close-settings");
    const playground = document.querySelector(".playground");
    const queryButton = document.querySelector("#execute-sql");
    const saveButton = document.querySelector("#save-task");

    section.removeChild(settings);

    function showDescription(tab) {
        if (activeTask != '' && !tab.classList.contains("active")) {
            loadTask();
            document.querySelector(".taskTab.active")?.classList.remove("active");
            tab.classList.add("active");
        }
    }

    function showResults(tab) {
        if (activeTask != '' && !tab.classList.contains("active")) {
            $.ajax({
                type: 'POST',
                url: '/getTaskResults',
                data: { code: activeTask },
                dataType: 'json',
                success: function (data) {
                    let results = document.createElement("ul");
                    results.classList.add("results");
                    results.innerHTML = data.taskResults;
                    document.querySelector(".taskInfo").removeChild(document.querySelector(".taskInfo-description"));
                    document.querySelector(".taskInfo").appendChild(results);
                    document.querySelector(".taskTab.active")?.classList.remove("active");
                    tab.classList.add("active");
                    taskIsVisible = false;
                    removeTable();
                },
                error: function (error) {
                    console.error(error);
                }
            });
        }
    }

    function loadTasks() {
        $.ajax({
            type: 'POST',
            url: '/loadTasks',
            dataType: 'json',
            success: async function (data) {
                $(".tasks").html(data.tasks);
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function editTaskInfoItem(taskInfo) {
        taskInfo.classList.add("editable");
        taskInfo.querySelector(".edit").classList.add("opacity-0");
        taskInfo.querySelector(".editAccept").classList.remove("opacity-0");
        taskInfo.querySelector(".editDeny").classList.remove("opacity-0");
        taskInfo.querySelector(".editAccept").classList.remove("unclickable");
        taskInfo.querySelector(".editDeny").classList.remove("unclickable");

        Array.from(document.querySelectorAll(".taskInfo-item")).splice(1).forEach(function (item) {
            item.removeAttribute("onclick");
        })
        taskInfo.querySelector('.taskInfo-content').setAttribute("contenteditable", "true");
        previousEditText = taskInfo.querySelector('.taskInfo-content').textContent;
    }

    function editAccept(event) {
        event.stopPropagation();
        let taskInfo = document.querySelector(".taskInfo-item.editable");
        taskInfo.querySelector(".edit").classList.remove("opacity-0");
        taskInfo.querySelector(".editAccept").classList.add("opacity-0");
        taskInfo.querySelector(".editDeny").classList.add("opacity-0");
        taskInfo.querySelector(".editAccept").classList.add("unclickable");
        taskInfo.querySelector(".editDeny").classList.add("unclickable");

        Array.from(document.querySelectorAll(".taskInfo-item")).splice(1).forEach(function (item) {
            item.setAttribute("onclick", "editTaskInfoItem(this)");
        })
        taskInfo.querySelector('.taskInfo-content').removeAttribute("contenteditable", "false");
        taskInfo.classList.remove("editable");
        previousEditText = null;
    }

    function editDeny(event) {
        event.stopPropagation();

        let taskInfo = document.querySelector(".taskInfo-item.editable");
        taskInfo.querySelector(".edit").classList.remove("opacity-0");
        taskInfo.querySelector(".editAccept").classList.add("opacity-0");
        taskInfo.querySelector(".editDeny").classList.add("opacity-0");
        taskInfo.querySelector(".editAccept").classList.add("unclickable");
        taskInfo.querySelector(".editDeny").classList.add("unclickable");

        Array.from(document.querySelectorAll(".taskInfo-item")).splice(1).forEach(function (item) {
            item.setAttribute("onclick", "editTaskInfoItem(this)");
        })
        taskInfo.querySelector('.taskInfo-content').removeAttribute("contenteditable", "false");
        taskInfo.classList.remove("editable");
        taskInfo.querySelector('.taskInfo-content').textContent = previousEditText;
    }

    function invalidQuery() {
        queryIsValid = false;
    }

    function copyTaskCode() {
        navigator.clipboard.writeText(document.querySelector(".taskInfo-code").textContent)
            .then(function () {
                notification("Код задания скопирован");
            });
    }

    function saveTask() {
        $.ajax({
            type: 'PUT',
            url: '/createTask',
            data: {
                name: document.querySelector(".taskInfo-name").textContent,
                description: document.querySelector(".taskInfo-desc").textContent,
                query: document.querySelector(".taskInfo-query").textContent,
                taskId: activeTask
            },
            dataType: 'json',
            success: function () {
                notification("Ваше задание сохранено");
            },
            error: function (error) {
                console.log(error);
            }
        });
    }

    function deleteTask(task) {
        var task1 = task.parentNode.parentNode;

        $.ajax({
            type: 'DELETE',
            url: '/deleteTask',
            data: { taskId: activeTask },
            success: function () {
                document.querySelector(".tasks").removeChild(task1);
                $(".outputs").html('<span class="text-helper">Создайте или выберите вашу схему</span>');
                activeTask = '';
                notification("Задание успешно удалено");
                taskIsVisible = false;
            },
            error: function (error) {
                console.log(error);
            }
        });
    }

    navbarHeader.addEventListener("click", function () {
        section.removeChild(navbar);
        section.insertBefore(settings, playground);
        settingsIsVisible = true;
        taskIsVisible = false;

        document.querySelector("#query-text").addEventListener("input", function () {
            queryIsValid = false;
        })

        $(".outputs").html('<span class="text-helper">Необходимо привязать схему</span>');

        if (tableOutputWrapper) {
            $(".query").removeChild(document.querySelector("#close-table"));
            tableOutputWrapper = null;
        }
    });

    function createTask() {
        $.ajax({
            type: 'PUT',
            url: '/createTask',
            data: {
                name: document.querySelector("#task-name").value,
                description: document.querySelector("#description-text").value,
                query: document.querySelector("#query-text").value,
                schemaId: activeSchema
            },
            dataType: 'json',
            success: function () {
                removeTable();

                section.removeChild(settings);
                section.insertBefore(navbar, playground);
                settingsIsVisible = false;
                queryIsValid = false;
                $(".outputs").html('<span class="text-helper">Создайте или выберите ваше задание</span>');
                notification("Задание успешно создано");
                loadTasks();
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    closeSettingsButton.addEventListener("click", function () {
        section.removeChild(settings);
        section.insertBefore(navbar, playground);
        settingsIsVisible = false;
        queryIsValid = false;
        activeSchema = '';
        activeTask = '';
        document.querySelector('.task.active')?.classList.remove("active");
        $(".outputs").html('<span class="text-helper">Создайте или выберите ваше задание</span>');
    });

    categories.forEach(cat => {
        cat.addEventListener("click", function () {
            categories.forEach(cat => cat.classList.remove("active"));
            cat.classList.add("active");
        })
    });

    connectSchemaButton.addEventListener("click", function () {
        openModal();
    });

    function openModal() {
        document.querySelector(".connectModal").style.display = "flex";
        document.querySelector(".overlay").style.display = "block";

        $.ajax({
            type: 'POST',
            url: '/loadSchemasForConnect',
            dataType: 'json',
            success: function (data) {
                $(".schemas").html(data.schemas);
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function closeModal() {
        document.querySelector(".connectModal").style.display = "none";
        document.querySelector(".overlay").style.display = "none";
        connectButton.classList.add("disable");
    }

    closeModalButton.addEventListener("click", function () {
        closeModal();
        taskIsVisible = false;
        queryIsValid = false;
    });

    connectButton.addEventListener("click", function () {
        loadTables();
        closeModal();
        notification("Схема привязана к заданию");
    })

    function loadTables() {
        $.ajax({
            type: 'POST',
            url: '/loadTablesBySchemaId',
            data: { schemaId: activeSchema },
            dataType: 'json',
            success: function (data) {
                $(".outputs").html(data.tables);
                document.querySelector(".table-header").removeChild(document.querySelector("#share_" + activeSchema));
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function loadTask() {
        $.ajax({
            type: 'POST',
            url: '/loadTaskById',
            data: { taskId: activeTask },
            dataType: 'json',
            success: function (data) {
                $(".outputs").html(data.task);
                taskIsVisible = true;
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function chooseSchema(schema) {
        // Сброс таблицы вывода данных
        removeTable();

        // Подсвечивание выбранной схемы
        document.querySelector('.schema.active')?.classList.remove("active");
        schema.classList.add("active");
        activeSchema = schema.getAttribute("id");
        connectButton.classList.remove("disable");
    }

    function chooseTask(task) {
        // Сброс таблицы вывода данных
        removeTable();

        document.querySelector('.task.active')?.classList.remove("active");
        task.classList.add("active");
        activeTask = task.getAttribute("id");

        loadTask();
    }


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
        if (!sqlProcessing) {
            sqlProcessing = true;
            document.querySelector("#execute-sql").classList.add("disable");

            let sqlQuery = ''
            let queryFromTask = false;

            if (settingsIsVisible) {
                sqlQuery = document.querySelector(".query-text").value
            } else {
                queryFromTask = true;
                sqlQuery = document.querySelector(".taskInfo-query").textContent
            }

            if (queryFromTask || (activeSchema != '' & sqlQuery != '')) {
                $.ajax({
                    type: 'POST',
                    url: '/executeQueryWithoutSave',
                    data: { sqlQuery: sqlQuery, schemaId: activeSchema, taskId: activeTask },
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
                        } else if (success.startsWith("ERROR: ")) {
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

                        if (settingsIsVisible) loadTables();

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
                    },
                    error: function (error) {
                        sqlProcessing = false;
                        document.querySelector("#execute-sql").classList.remove("disable");
                    }
                });
            } else {
                sqlProcessing = false;
                document.querySelector("#execute-sql").classList.remove("disable");
                notification("Требуется привязанная схема и введённый запрос")
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

    setInterval(function() {
        if (settingsIsVisible) {
            if (!document.querySelector("#task-name").checkValidity()) {
                saveButton.classList.add("disable");
            } else if (!document.querySelector("#description-text").checkValidity()) {
                saveButton.classList.add("disable");
            } else if (!document.querySelector("#query-text").checkValidity()) {
                saveButton.classList.add("disable");
            } else if (activeSchema == '') {
                saveButton.classList.add("disable");
            } else if (!queryIsValid) {
                saveButton.classList.add("disable");
            } else {
                saveButton.classList.remove("disable");
            }
        } else if (taskIsVisible) {
            let editButton = document.querySelector("#edit-task");
            if (!queryIsValid) {
                editButton.classList.add("disable");
            } else {
                editButton.classList.remove("disable");
            }
        }
    }, 1);

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

    loadTasks();
</script>
</html>
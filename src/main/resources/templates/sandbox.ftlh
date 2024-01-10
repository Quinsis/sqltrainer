<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/sandbox.css">
    <link rel="shortcut icon" href="/img/sql.png"/>
    <title>Песочница</title>
</head>
<body>
<section id="main">
    <div class="overlay" id="overlay"></div>
    <div class="navbar">
        <div class="navbar-header">
            <a class="createSchema">
                Создать схему
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="icon-md"><path fill-rule="evenodd" clip-rule="evenodd" d="M16.7929 2.79289C18.0118 1.57394 19.9882 1.57394 21.2071 2.79289C22.4261 4.01184 22.4261 5.98815 21.2071 7.20711L12.7071 15.7071C12.5196 15.8946 12.2652 16 12 16H9C8.44772 16 8 15.5523 8 15V12C8 11.7348 8.10536 11.4804 8.29289 11.2929L16.7929 2.79289ZM19.7929 4.20711C19.355 3.7692 18.645 3.7692 18.2071 4.2071L10 12.4142V14H11.5858L19.7929 5.79289C20.2308 5.35499 20.2308 4.64501 19.7929 4.20711ZM6 5C5.44772 5 5 5.44771 5 6V18C5 18.5523 5.44772 19 6 19H18C18.5523 19 19 18.5523 19 18V14C19 13.4477 19.4477 13 20 13C20.5523 13 21 13.4477 21 14V18C21 19.6569 19.6569 21 18 21H6C4.34315 21 3 19.6569 3 18V6C3 4.34314 4.34315 3 6 3H10C10.5523 3 11 3.44771 11 4C11 4.55228 10.5523 5 10 5H6Z" fill="currentColor"></path></svg>
            </a>
        </div>
        <ul class="schemas"></ul>
        <a class="exit" href="/">
            <span>Вернуться</span>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" class="icon-md"><path d="M11 3H7C5.89543 3 5 3.89543 5 5V19C5 20.1046 5.89543 21 7 21H11" stroke="currentColor" stroke-width="2" stroke-linecap="round"></path><path d="M20 12H11M20 12L16 16M20 12L16 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path></svg>
        </a>
    </div>
    <div class="playground">
        <div class="outputs">
            <span class="text-helper">Создайте или выберите вашу схему</span>
        </div>
        <div class="query">
            <span onclick="toggleTable()" id="close-table">Свернуть результат<i class="fa-solid fa-chevron-down"></i></span>
            <div class="input-wrapper">
                <div class="input-group">
                    <input type="text" id="sql-input" class="sql-input" placeholder="Отправить запрос">
                    <svg id="execute-sql" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="none"
                         class="icon-sm m-1 md:m-0">
                        <path d="M.5 1.163A1 1 0 0 1 1.97.28l12.868 6.837a1 1 0 0 1 0 1.766L1.969 15.72A1 1 0 0 1 .5 14.836V10.33a1 1 0 0 1 .816-.983L8.5 8 1.316 6.653A1 1 0 0 1 .5 5.67V1.163Z"
                              fill="currentColor"></path>
                    </svg>
                </div>
            </div>
        </div>
    </div>
    <div class="shareModal">
        <div class="modal-header">
            <span>Поделиться схемой</span>
            <i onclick="shareModalClose()" class="fa-solid fa-xmark"></i>
        </div>
        <div class="modal-content">
            <div class="online-share share-active">
                <div class="share-group">
                    <span>Онлайн ссылка</span>
                    <small>Пользователи смогут импортировать вашу схему в том виде, в котором она представлена в настоящий момент.</small>
                </div>
            </div>
            <div class="offline-share">
                <div class="share-group">
                    <span>Оффлайн ссылка</span>
                    <small>Пользователи смогут импортировать вашу схему в том виде, в котором она была представлена на момент формирования ссылки.</small>
                </div>
            </div>
        </div>
        <div class="copyGroup">
            <span onclick="shareLink()" class="modal-button share-button"><i class="fa-solid fa-link"></i> Скопировать ссылку</span>
        </div>
    </div>
</section>
</body>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    let activeSchema = '';
    let sqlProcessing = false;
    let tableToggle = false;
    let tableOutputWrapper, tableOutput, schemaName;
    let timeoutShare = false, timeoutImport = false;

    document.querySelector(".online-share").addEventListener("click", function () {
        document.querySelector(".online-share").classList.add("share-active");
        document.querySelector(".offline-share").classList.remove("share-active");
    })

    document.querySelector(".offline-share").addEventListener("click", function () {
        document.querySelector(".offline-share").classList.add("share-active");
        document.querySelector(".online-share").classList.remove("share-active");
    })

    function loadTables() {
        $.ajax({
            type: 'POST',
            url: '/loadTablesBySchemaId',
            data: { schemaId: activeSchema },
            dataType: 'json',
            success: function (data) {
                $(".outputs").html(data.tables);
            },
            error: function (error) {
                console.error(error);
            }
        });
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

    function loadSchemas() {
        $.ajax({
            type: 'POST',
            url: '/loadSchemas',
            dataType: 'json',
            success: async function (data) {
                $(".schemas").html(data.schemas);
            },
            error: function (error) {
                console.error(error);
            }
        });
    }

    function edit(schema) {
        schema.parentNode.classList.add("edit");

        var span = document.querySelector('.active').children.item(1);
        var actions = document.querySelector('.active').children.item(2);

        var newInput = document.createElement("input");
        newInput.setAttribute("type", "text");
        schemaName = span.textContent;
        newInput.value = schemaName;

        document.querySelector('.active').removeChild(span);
        document.querySelector('.active').insertBefore(newInput, actions);
    }

    document.querySelector(".createSchema").addEventListener("click", function () {
        $.ajax({
            type: 'PUT',
            url: '/createSchema',
            dataType: 'json',
            success: function (data) {
                removeTable();
                document.getElementById("sql-input").value = "";

                // Создаем новый элемент схемы
                let newSchemaHTML = data.schemaHtml;
                let range = document.createRange();
                let fragmentSchema = range.createContextualFragment(newSchemaHTML);

                if (activeSchema != '') {
                    [...$(".schema")].find(el => el.id === activeSchema).classList.remove("active");
                }

                document.querySelector(".schemas").insertBefore(fragmentSchema, document.querySelector(".schemas").firstChild);

                document.querySelectorAll(".schema").item(0).classList.add("active");
                activeSchema = document.querySelectorAll(".schema").item(0).getAttribute("id");
                loadTables();
                notification("Схема успешно создана");
            },
            error: function (error) {
                console.error(error);
            }
        });
    })

    function deny(schema) {
        schema.parentNode.classList.remove("edit");

        var input = document.querySelector('.active').children.item(1);
        var actions = document.querySelector('.active').children.item(2);

        var newSpan = document.createElement("span");
        newSpan.innerText = schemaName;

        document.querySelector('.active').removeChild(input);
        document.querySelector('.active').insertBefore(newSpan, actions);
    }

    function accept(schema) {
        var input = document.querySelector('.active').children.item(1);
        var actions = document.querySelector('.active').children.item(2);

        if (input.value.length != 0) {
            schema.parentNode.classList.remove("edit");

            var newSpan = document.createElement("span");
            newSpan.innerText = input.value;

            document.querySelector('.active').removeChild(input);
            document.querySelector('.active').insertBefore(newSpan, actions);

            $.ajax({
                type: 'PUT',
                url: '/changeSchemaName',
                data: {schemaName: input.value, schemaId: activeSchema},
                success: function () {
                    document.getElementById("outputs-schema_name").textContent = newSpan.innerText;
                    notification("Название схемы изменено");
                },
                error: function (error) {
                    console.log(error);
                }
            });
        }
    }

    function deleteSchema(schema) {
        var schema1 = schema.parentNode.parentNode;

        $.ajax({
            type: 'DELETE',
            url: '/deleteSchema',
            data: { schemaId: activeSchema },
            success: async function () {
                document.querySelector(".schemas").removeChild(schema1);
                $(".outputs").html('<span class="text-helper">Создайте или выберите вашу схему</span>');
                activeSchema = '';
                notification("Схема успешно удалена");
            },
            error: function (error) {
                console.log(error);
            }
        });
    }

    function choose(schema) {
        // Сброс таблицы вывода данных
        removeTable();
        document.getElementById("sql-input").value = "";

        // Подсвечивание выбранной схемы
        Array.from(document.querySelectorAll('.schema')).forEach(function (schema1) {
            schema1.classList.remove("active");
        });
        schema.classList.add("active");
        activeSchema = schema.getAttribute("id");

        loadTables();
    }

    function shareLink() {
        if (!timeoutShare) {
            // Формирование ссылки из API
            if (document.querySelector(".online-share").classList.contains("share-active")) {
                $.ajax({
                    type: 'POST',
                    url: '/link/online',
                    data: activeSchema,
                    success: function (data) {
                        navigator.clipboard.writeText(data)
                            .then(function () {
                                shareModalClose();
                                notification("Ссылка на схему скопирована");
                            });
                    }
                })
            } else {
                $.ajax({
                    type: 'POST',
                    url: '/link/offline',
                    data: activeSchema,
                    success: function (data) {
                        navigator.clipboard.writeText(data)
                            .then(function () {
                                shareModalClose();
                                notification("Ссылка на схему скопирована");
                            });
                    }
                })
            }
        }
    }

    function shareModalClose() {
        document.querySelector(".shareModal").style.display = "none";
        document.querySelector(".overlay").style.display = "none";
    }

    function shareModalOpen() {
        document.querySelector(".shareModal").style.display = "flex";
        document.querySelector(".overlay").style.display = "block";
    }

    document.getElementById("execute-sql").addEventListener("click", function () {
        if (!sqlProcessing) {
            sqlProcessing = true;
            document.querySelector(".query").classList.add("disable");
            var sqlQuery = document.getElementById("sql-input").value // Получить SQL-запрос из input

            if (activeSchema != '' & sqlQuery != '') {
                $.ajax({
                    type: 'POST',
                    url: '/executeQuery',
                    data: {sqlQuery: sqlQuery, schemaId: activeSchema},
                    success: function (success) {
                        console.log(success);
                        if (tableOutputWrapper == null) {
                            tableOutput = document.createElement("table")
                            tableOutput.classList.add("table-output");
                        }

                        var successAnimate;
                        tableOutput.innerHTML = "<span class='error-table'>" + success + "</span>";

                        if (success.endsWith('В таблице отсутствуют записи.') || success.endsWith('запрос не вернул результатов.')) {
                            successAnimate = true;
                        } else if (success.startsWith("ERROR")) {
                            successAnimate = false;
                        } else {
                            successAnimate = true;
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

                        loadTables();

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

                        document.querySelector(".query").classList.remove("disable");
                    },
                    error: function (error) {
                        sqlProcessing = false;
                        document.querySelector(".query").classList.remove("disable");

                    }
                });
            } else {
                sqlProcessing = false;
                document.querySelector(".query").classList.remove("disable");
            }
        }
    });

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

    loadSchemas();
</script>
</html>
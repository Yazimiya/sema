document.addEventListener("DOMContentLoaded", function () {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

    document.querySelectorAll(".rating-container").forEach(container => {
        const productId = container.dataset.productId;
        const stars = container.querySelectorAll(".star:not(.guest)");
        const avgDisplay = container.querySelector(".avg");
        const youDisplay = container.querySelector(".you");

        // Получаем среднюю оценку
        fetch(`/api/rating/average?productId=${productId}`)
            .then(res => res.json())
            .then(data => {
                avgDisplay.textContent = `${data.average.toFixed(2)} ⭐ (${data.count} голосов)`;
            });

        // Получаем личную оценку
        fetch(`/api/rating/user-rating?productId=${productId}`)
            .then(res => res.json())
            .then(score => {
                if (score) {
                    youDisplay.textContent = "★".repeat(score);
                    highlightStars(stars, score);
                } else {
                    youDisplay.textContent = "—";
                }
            })
            .catch(() => {
                youDisplay.textContent = "—";
            });

        // Отправка новой оценки
        stars.forEach(star => {
            star.addEventListener("click", function () {
                const value = this.dataset.value;

                fetch(`/api/rating/rate?productId=${productId}&value=${value}`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded",
                        [csrfHeader]: csrfToken
                    },
                    credentials: "same-origin"
                })
                    .then(res => {
                        if (!res.ok) {
                            // Попытка считать JSON-ошибку и выбросить
                            return res.json().then(err => {
                                throw new Error(err.message || "Неизвестная ошибка");
                            });
                        }
                        return res.json();
                    })
                    .then(newAvg => {
                        avgDisplay.textContent = `${newAvg.toFixed(2)} ⭐`;
                        youDisplay.textContent = "★".repeat(value);
                        highlightStars(stars, value);
                    })
                    .catch(err => {
                        alert("Ошибка при оценке: " + err.message);
                    });
            });
        });

        function highlightStars(stars, value) {
            stars.forEach(star => {
                star.classList.toggle("active", star.dataset.value <= value);
            });
        }
    });

    // Для гостей
    document.querySelectorAll(".rating-stars .star.guest").forEach(star => {
        star.addEventListener("click", function () {
            window.location.href = "/login";
        });
    });
});


// использовала для теста
// const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
// const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");
//
// fetch('/api/rating/rate?productId=1&value=5', {
//     method: "POST",
//     headers: {
//         "Content-Type": "application/x-www-form-urlencoded",
//         [csrfHeader]: csrfToken
//     }
// })
//     .then(res => {
//         if (!res.ok) {
//             return res.json().then(err => {
//                 alert("Ошибка: " + err.message);
//             });
//         }
//         return res.json().then(data => {
//             alert("Успешно: " + data);
//         });
//     })
//     .catch(err => {
//         alert("Сетевая ошибка: " + err.message);
//     });

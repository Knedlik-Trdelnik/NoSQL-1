package alfarius.yushinon.nosql1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NoSql1Application {

    public static void main(String[] args) {
        SpringApplication.run(NoSql1Application.class, args);
    }
}
/*
fetch("/login", {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        login: "admin",
        password: "1"
    })
})
    .then(response => {
        console.log("HTTP status:", response.status);
        return response.json();
    })
    .then(data => {
        console.log("Ответ сервера:", data);
    })
    .catch(error => {
        console.error("Ошибка:", error);
    });
 */
/*
fetch("/register", {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        login: "test",
        password: "1",
        role: "USER"
    })
})
    .then(response => {
        console.log("HTTP status:", response.status);
        return response.json();
    })
    .then(data => {
        console.log("Ответ сервера:", data);
    })
    .catch(error => {
        console.error("Ошибка:", error);
    });
 */
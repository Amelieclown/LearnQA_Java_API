import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;


public class HelloWorld {

    @Test
    public void test() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();
        response.prettyPrint();
    }

    @Test
    public void testParsingJson() {
        JsonPath response = RestAssured
                .given()
                .get("https://playground.learnqa.ru/api/get_json_homework")
                .jsonPath();

        String answer = response.get("messages.message[1]");
        System.out.println(answer);
    }

    @Test
    public void testRedirect() {
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        String header = response.getHeader("location");
        System.out.println(header);
    }

    @Test
    public void testPassword() {
        String login = "super_admin";
        List<String> passwords = Arrays.asList(
                "123456", "password", "12345678", "qwerty", "123456789",
                "12345", "1234", "111111", "1234567", "dragon",
                "123123", "baseball", "abc123", "football", "monkey",
                "letmein", "696969", "shadow", "master", "666666",
                "qwertyuiop", "123321", "mustang", "1234567890", "welcome"
        );

        for (String password : passwords) {
            String authCookie = getAuthCookie1(login, password);
            if (authCookie != null) {
                String authResult = checkAuthCookie2(authCookie);
                if (authResult.equals("You are authorized")) {
                    System.out.println("Правильный пароль: " + password);
                    break;
                }else {
                    System.out.println("Неверный пароль");
                }
            }
        }
    }

    public static String getAuthCookie1(String login, String password) {
        Response response = RestAssured
                .given()
                .formParam("login", login)
                .formParam("password", password)
                .when()
                .post("https://playground.learnqa.ru/ajax/api/get_secret_password_homework")
                .andReturn();

        Headers mmm = response.getHeaders();
        System.out.println("\nЗаголовки - \n" + mmm);

        return response.getCookie("auth_cookie");
    }

    public static String checkAuthCookie2(String authCookie) {
        Response response = RestAssured
                .given()
                .cookie("auth_cookie", authCookie)
                .when()
                .get("https://playground.learnqa.ru/ajax/api/check_auth_cookie")
                .andReturn();

        Headers mmm = response.getHeaders();
        System.out.println("\nЗаголовки - \n" + mmm);

        return response.getBody().asString();

    }

    @Test
    public void testToken() throws InterruptedException {

        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();
        String accessToken = response.jsonPath().getString("token");
        System.out.println("Токен доступа - " + accessToken);


        Response responseWithTokenBeforeCompletion = RestAssured
                .given()
                .queryParam("token", accessToken)
                .when()
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();
        String statusBefore = responseWithTokenBeforeCompletion.jsonPath().getString("status");
        System.out.println("Статус до завершения задачи - " + statusBefore);


        int secondsToWait = response.jsonPath().getInt("seconds");

        Thread.sleep(secondsToWait * 1000);



        Response responseWithTokenAfterCompletion = RestAssured
                .given()
                .queryParam("token", accessToken)
                .when()
                .get("https://playground.learnqa.ru/ajax/api/longtime_job")
                .andReturn();
        String statusAfter = responseWithTokenAfterCompletion.jsonPath().getString("status");
        System.out.println("Статус после завершения задачи - " + statusAfter);


        String result = responseWithTokenAfterCompletion.jsonPath().getString("result");
        if (result != null) {
            System.out.println("Результат: " + result);
        } else {
            System.out.println("Результат еще не готов.");
        }
    }

    @Test
    public void testLongRedirect() {
        String url = "https://playground.learnqa.ru/api/long_redirect";
        int redirects = 0;

        while (true) {
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(url)
                    .andReturn();

            redirects++;

            int statusCode = response.getStatusCode();

            if (statusCode == 301 || statusCode == 302 || statusCode == 303) {
                url = response.getHeader("Location");
            } else if (statusCode == 200) {
                System.out.println("Количество редиректов: " + redirects);
                break;
            }
        }
    }
}

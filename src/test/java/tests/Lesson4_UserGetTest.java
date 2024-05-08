package tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class Lesson4_UserGetTest extends BaseTestCase {
    private final static String URL = "https://playground.learnqa.ru";

    @Test
    public void testGetUserDataNotAuth() {
        Response responseUserData = RestAssured
                .get(URL + "/api/user/2")
                .andReturn();

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNotField(responseUserData, "firstName");
        Assertions.assertJsonHasNotField(responseUserData, "lastName");
        Assertions.assertJsonHasNotField(responseUserData, "email");
    }

    @Test
    public void testGetUserDetailsAuthAsSameUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post(URL + "/api/user/login")
                .andReturn();

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get(URL + "/api/user/2")
                .andReturn();

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    @Epic("Ex16: Запрос данных другого пользователя")
    @Feature("Получение данных другого пользователя через авторизованного пользователя")
    @Test
    public void testGetUserDetailsOtherUser() {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.put("username", "KryuKir");

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(URL + "/api/user/")
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post(URL + "/api/user/login")
                .andReturn();

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get(URL + "/api/user/" + userId)
                .andReturn();

        Assertions.assertJsonHasField(responseUserData, "username");
        System.out.println(responseUserData.asString());
    }
}
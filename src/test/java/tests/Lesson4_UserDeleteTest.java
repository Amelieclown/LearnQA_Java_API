package tests;

import io.qameta.allure.Epic;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Lesson4_UserDeleteTest extends BaseTestCase {
    private final static String URL = "https://playground.learnqa.ru";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Epic("Ex18: Тесты на DELETE")
    @Test
    @DisplayName("Удалить пользователя под иммунитетом удаления")
    public void testDeleteImmuneUser() {
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

        JsonPath responseDelete = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .delete(URL + "/api/user/2")
                .jsonPath();

        assertEquals("Please, do not delete test users with ID 1, 2, 3, 4 or 5.",
                responseDelete.getString("error"));
        responseDelete.prettyPrint();
    }

    @Test
    @DisplayName("Создать, авторизоваться и удалить пользователя, для последующего получения информации")
    public void testDeleteSuccessfully() {
        //Создание пользователя
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = apiCoreRequests
                .makePostRequestJson(URL + "/api/user/", userData);

        String userId = responseCreateAuth.getString("id");

        //Авторизация
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/login", authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //Удаление пользователя
        Response responseDelete = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .delete(URL + "/api/user/" + userId)
                .andReturn();

        //Получение данных по удалённому пользователю
        Response responseGetUser = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get(URL + "/api/user/" + userId)
                .andReturn();

        assertEquals("User not found", responseGetUser.asString());
        System.out.println(responseGetUser.asString());
    }

    @Test
    @DisplayName("Удаление чужого пользователя")
    public void testDeleteOtherUser() {
        //Создание пользователя
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = apiCoreRequests
                .makePostRequestJson(URL + "/api/user/", userData);

        String userId = responseCreateAuth.getString("id");

        //Авторизация
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/login", authData);

        //Удаление пользователя
        JsonPath responseDelete = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .delete(URL + "/api/user/96757")
                .jsonPath();

        assertEquals("This user can only delete their own account.", responseDelete.getString("error"));
        System.out.println(responseDelete.getString("error"));
    }
}
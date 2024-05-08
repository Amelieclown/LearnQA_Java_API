package tests;

import io.qameta.allure.Epic;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Lesson4_UserEditTest extends BaseTestCase {
    private final static String URL = "https://playground.learnqa.ru";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    public void testEditJustCreatedTest() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(URL + "/api/user/")
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post(URL + "/api/user/login")
                .andReturn();

        //EDIT
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(URL + "/api/user/" + userId)
                .andReturn();

        //GET
        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .get(URL + "/api/user/" + userId)
                .andReturn();

        Assertions.asserJsonByName(responseUserData, "firstName", newName);
    }

    @Epic("Ex17: Негативные тесты на PUT")
    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    public void testEditUserNonAuth() {
        String newName = "Gavrik";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        JsonPath responseEditUser = RestAssured
                .given()
                .body(editData)
                .put(URL + "/api/user/96757")
                .jsonPath();

        assertEquals("Auth token not supplied", responseEditUser.getString("error"));
        responseEditUser.prettyPrint();
    }

    @Test
    @DisplayName("Изменение данных пользователя под другим авторизованным пользователем")
    public void testEditUserOtherUser() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/", userData);

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/login", authData);

        //EDIT
        String newName = "Gavrik";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        JsonPath responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(URL + "/api/user/96757")
                .jsonPath();

        assertEquals("This user can only edit their own data.", responseEditUser.getString("error"));
        responseEditUser.prettyPrint();
    }

    @Test
    @DisplayName("Изменить email пользователя на новый без символа @")
    public void testEditUserIncorrectEmail() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = apiCoreRequests
                .makePostRequestJson(URL + "/api/user/", userData);

        String userId = responseCreateAuth.getString("id");

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/login", authData);

        //EDIT
        String newEmail = "vinkotovexample.com";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);

        JsonPath responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(URL + "/api/user/" + userId)
                .jsonPath();

        assertEquals("Invalid email format", responseEditUser.getString("error"));
        responseEditUser.prettyPrint();
    }

    @Test
    @DisplayName("Изменить firstName пользователя на короткое значение в один символ")
    public void testEditUserWithShortName() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuth = apiCoreRequests
                .makePostRequestJson(URL + "/api/user/", userData);

        String userId = responseCreateAuth.getString("id");

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/login", authData);

        //EDIT
        String newName = "A";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        JsonPath responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(URL + "/api/user/" + userId)
                .jsonPath();

        assertEquals("The value for field `firstName` is too short", responseEditUser.getString("error"));
        responseEditUser.prettyPrint();
    }
}
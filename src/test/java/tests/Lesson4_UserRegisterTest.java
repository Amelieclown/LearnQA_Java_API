package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Lesson4_UserRegisterTest extends BaseTestCase {
    private final static String URL = "https://playground.learnqa.ru";

    @Test
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(URL + "/api/user/")
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Test
    public void testCreateUserSuccessfully() {
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(URL + "/api/user/")
                .andReturn();

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");

        System.out.println(responseCreateAuth.asString());
    }

    //Начало задания "Ex15: Тесты на метод user"

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Epic("Ex15: Тесты на метод user")
    @Feature("Создание пользователя с условиями")

    @Test
    @Description("Тестирование валидации")
    @DisplayName("Без символа @ в параметре email")
    public void testCreateUserWithoutSymbol() {
        String email = "vinkotovexample.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @ParameterizedTest
    @CsvSource({"email", "password", "username", "firstName", "lastName"})
    @DisplayName("Без одного параметра в запросе")
    public void testCreateUserWithoutOneParam(String missingParameter) {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.remove(missingParameter);

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The following required params are missed: " + missingParameter);
    }

    @ParameterizedTest
    @CsvSource({"email", "password", "username", "firstName", "lastName"})
    @DisplayName("С одним символом в значении параметра")
    public void testCreateUserWithShortValue(String param) {
        Map<String, String> shortValue = new HashMap<>();
        shortValue.put(param, "A");

        Map<String, String> userData = DataGenerator.getRegistrationData(shortValue);

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of '" + param + "' field is too short");
    }

    @ParameterizedTest
    @CsvSource({"username", "firstName", "lastName"})
    @DisplayName("С кол-вом символов больше 250 в значении параметра имени")
    public void testCreateUserLongName(String longName) {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 251;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();

        Map<String, String> longValue = new HashMap<>();
        longValue.put(longName, generatedString);

        Map<String, String> userData = DataGenerator.getRegistrationData(longValue);

        Response responseCreateAuth = apiCoreRequests
                .makePostRequest(URL + "/api/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of '" + longName + "' field is too long");
    }
}

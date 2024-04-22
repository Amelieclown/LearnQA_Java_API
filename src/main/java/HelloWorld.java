import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import io.restassured.response.Response;


public class HelloWorld {

    @Test
    public void test() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();
        response.prettyPrint();
    }
}

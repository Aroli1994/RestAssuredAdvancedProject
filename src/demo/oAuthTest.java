package demo;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import pojo.Api;
import pojo.GetCourse;

public class oAuthTest {

	public static void main(String[] args) {
		String[] courseTitles = { "Selenium Webdriver Java", "Cypress", "Protractor" };

		RestAssured.baseURI = "https://rahulshettyacademy.com/";

		String response = given().log().all()
				.formParam("client_id", "692183103107-p0m7ent2hk7suguv4vq22hjcfhcr43pj.apps.googleusercontent.com")
				.formParam("client_secret", "erZOWM9g3UtwNRj340YYaK_W").formParam("grant_type", "client_credentials")
				.formParam("scope", "trust").when().post("oauthapi/oauth2/resourceOwner/token").then().log().all()
				.assertThat().statusCode(200).extract().response().asString();

		JsonPath js = new JsonPath(response);
		String accessToken = js.getString("access_token");

		// Deserialization
//		GetCourse gc = given().log().all()
//		.queryParam("access_token", accessToken)
//		.when().get("oauthapi/getCourseDetails")
//		.then().log().all().assertThat().statusCode(401)
//		.extract().response().as(GetCourse.class);

		// Deserialization
		GetCourse gc = given().log().all().queryParam("access_token", accessToken).expect().defaultParser(Parser.JSON)
				.when().get("oauthapi/getCourseDetails").as(GetCourse.class);

		System.out.println(gc.getLinkedIn());
		System.out.println(gc.getInstructor());

		System.out.println(gc.getCourses().getApi().get(1).getCourseTitle());

		// To print 'price' of SoapUI Webservices testing
		List<Api> apiCourses = gc.getCourses().getApi();
		for (int i = 0; i < apiCourses.size(); i++) {
			if (apiCourses.get(i).getCourseTitle().equalsIgnoreCase("SoapUI Webservices testing")) {
				System.out.println(apiCourses.get(i).getPrice());
			}
		}

		// To verify course title of WebAutomation json object
		ArrayList<String> actualList = new ArrayList<>();
		int count = gc.getCourses().getWebAutomation().size();
		for (int i = 0; i < count; i++) {
			// System.out.println(gc.getCourses().getWebAutomation().get(i).getCourseTitle());
			actualList.add(gc.getCourses().getWebAutomation().get(i).getCourseTitle());
		}

		List<String> expectedList = Arrays.asList(courseTitles);
		Assert.assertTrue(actualList.equals(expectedList));
	}

}

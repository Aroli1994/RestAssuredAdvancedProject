package demo;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import pojo.LoginRequest;
import pojo.LoginResponse;
import pojo.OrderDetail;
import pojo.OrderPlaced;
import pojo.Orders;

public class EcommerceAPITest {

	public static void main(String[] args) {

	RequestSpecification reqSpec = new RequestSpecBuilder()
			.setBaseUri("https://rahulshettyacademy.com")
			.setContentType(ContentType.JSON)
			.build();
	
	ResponseSpecification resSpec = new ResponseSpecBuilder()
			.expectStatusCode(200)
			.expectContentType(ContentType.JSON)
			.build();
	
	//Login
	LoginRequest loginReq = new LoginRequest();
	loginReq.setUserEmail("vk1062986@gmail.com");
	loginReq.setUserPassword("Vinay@1994");
	//SSL certification
	RequestSpecification requestLogin = given().relaxedHTTPSValidation().log().all().spec(reqSpec).body(loginReq);
	
	LoginResponse loginRes = requestLogin.when().post("/api/ecom/auth/login")
			.then().log().all().spec(resSpec)
			.extract().response().as(LoginResponse.class);
	
	String token = loginRes.getToken();
	String userId = loginRes.getUserId();
	System.out.println(loginRes.getToken());
	System.out.println(loginRes.getUserId());
	
	//Create Product
	RequestSpecification addProductReqSpec = new RequestSpecBuilder()
			.setBaseUri("https://rahulshettyacademy.com")
			.addHeader("Authorization", token)
			.build();
	RequestSpecification reqAddProduct = given().spec(addProductReqSpec)
			.param("productName", "Mac Air")
			.param("productAddedBy", userId)
			.param("productCategory", "electronics")
			.param("productSubCategory", "laptops")
			.param("productPrice", 65000)
			.param("productDescription", "256GB storage, 8GB RAM")
			.param("productFor", "men")
			.multiPart("productImage", new File(System.getProperty("user.dir") + "//pexels-pixabay-38547.jpg"));
	
	Response addProductResponse = reqAddProduct.when().post("/api/ecom/product/add-product")
	.then().log().all().extract().response();
	
	String addProductStringResponse = addProductResponse.asString();
	JsonPath js = new JsonPath(addProductStringResponse);
	
	String productId = js.getString("productId");
	System.out.println(productId);
	
	//Create Order
	RequestSpecification createOrderReqSpec = new RequestSpecBuilder()
			.setBaseUri("https://rahulshettyacademy.com")
			.setContentType(ContentType.JSON)
			.addHeader("Authorization", token).addHeader("Connection", "keep-alive")
			.build();
	
	OrderDetail orderDetail = new OrderDetail();
	orderDetail.setCountry("India");
	orderDetail.setProductOrderedId(productId);
	
	List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
	orderDetailList.add(orderDetail);
	
	Orders orders = new Orders();
	orders.setOrders(orderDetailList);
	
	RequestSpecification createOrderReq = given().log().all().spec(createOrderReqSpec).body(orders);
	
	String responseStringAddOrder = createOrderReq.when().post("/api/ecom/order/create-order")
			.then().log().all().extract().response().asString();
	
	JsonPath js1 = new JsonPath(responseStringAddOrder);
	String orderId = js1.getString("orders[0]");
	
	System.out.println(responseStringAddOrder);
	
	//Get Order Details
	RequestSpecification getOrderDetailsReqSpec = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
			.addHeader("Authorization", token)
			.addQueryParam("id", orderId)
			.build();
	
	RequestSpecification getOrderDetailsReq = given().log().all().spec(getOrderDetailsReqSpec);
	OrderPlaced orderPlaced  = getOrderDetailsReq.when().get("/api/ecom/order/get-orders-details")
			.then().log().all().extract().response().as(OrderPlaced.class);
	
	System.out.println("Order_Id :: " + orderPlaced.getData().get_id());
	System.out.println("Order_By :: " + orderPlaced.getData().getOrderBy());
	System.out.println("ProductOrdered_Id :: " + orderPlaced.getData().getProductOrderedId());
	System.out.println("ProductName :: " + orderPlaced.getData().getProductName());
	System.out.println("Message :: " + orderPlaced.getMessage());
	
	//Delete Product
	RequestSpecification deleteProductReqSpec = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
		.addHeader("Authorization", token)
		.build();
		
	RequestSpecification deleteProductReq = given().log().all().spec(deleteProductReqSpec).pathParam("product_Id", productId);
		
	String responseStringDeleteProduct = deleteProductReq.when().delete("/api/ecom/product/delete-product/{product_Id}")
			.then().log().all().extract().response().asString();
		
	JsonPath js2 = new JsonPath(responseStringDeleteProduct);
		
	String actualMessage = js2.getString("message");
	Assert.assertEquals(actualMessage, "Product Deleted Successfully");
		
	
	//Delete Order
	RequestSpecification deleteOrderReqSpec = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
			.addHeader("Authorization", token)
			.build();
	
	RequestSpecification deleteOrderReq = given().log().all().spec(deleteOrderReqSpec).pathParam("order_Id", orderId);
	
	String responseStringDeleteOrder = deleteOrderReq.when().delete("/api/ecom/order/delete-order/{order_Id}")
			.then().log().all().extract().response().asString();
	
	JsonPath js3 = new JsonPath(responseStringDeleteOrder);
	
	Assert.assertEquals(js3.getString("message"), "Orders Deleted Successfully");
	
	}

}

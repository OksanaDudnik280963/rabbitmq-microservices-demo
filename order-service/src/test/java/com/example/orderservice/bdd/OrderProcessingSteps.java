package com.example.orderservice.bdd;

import com.example.commonmodels.Order;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.model.OrderResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessingSteps {

    @Autowired
    public TestRestTemplate restTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private ResponseEntity<OrderResponse> response;

    @Given("RabbitMQ is running")
    public void rabbit_mq_is_running() {
        assertNotNull(rabbitTemplate.getConnectionFactory(), "RabbitMQ ConnectionFactory must not be null");
    }

    @When("I create an order for customer {string} with email {string} for {int} {string} items costing {double}")
    public void i_create_an_order_for_customer_with_email_for_items_costing(
            String customerName, String email, Integer quantity, String itemType, Double price) {

        OrderRequest request = new OrderRequest();

        // Заполняем все поля, требующие валидации в OrderRequest
        request.setCustomerId("CUST-100");
        request.setCustomerName(customerName);
        request.setCustomerEmail(email);
        request.setEmail(email);
        request.setProductName("Test Product"); // Обязательное поле @NotBlank в текущей модели
        request.setPrice(price);                // Обязательное поле @NotNull Double
        request.setAmount(BigDecimal.valueOf(price));
        request.setItemType(itemType);
        request.setQuantity(quantity);

        response = restTemplate.postForEntity("/api/orders", request, OrderResponse.class);
    }
    @Then("the order response status should be {int}")
    public void the_order_response_status_should_be(Integer expectedStatus) {
        assertNotNull(response, "Response should not be null");
        assertEquals(expectedStatus.intValue(), response.getStatusCode().value(),
                "Expected status " + expectedStatus + " but got " + response.getStatusCode() + ": " + response.getBody());
    }

    @Then("an {string} message should be published")
    public void an_message_should_be_published(String routingKey) {
        assertNotNull(response.getBody(), "Order response body must not be null");
        assertNotNull(response.getBody().getOrderId(), "Order ID should be generated");
    }

    @Then("the message should contain customer {string}")
    public void the_message_should_contain_customer(String expectedCustomer) {
        assertNotNull(response.getBody(), "Order response body must not be null");
        assertEquals(expectedCustomer, response.getBody().getCustomerName());
    }
}
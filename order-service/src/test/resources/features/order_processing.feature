Feature: Order processing through RabbitMQ

  Scenario: Create an order and publish an order-created event
    Given RabbitMQ is running
    When I create an order for customer "CUST-100" with email "customer@example.com" for 2 "LAPTOP" items costing 1999.98
    Then the order response status should be 201
    And an "order.created" message should be published
    And the message should contain customer "CUST-100"
package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


// Order Controller Handles all  the Order related endpoints

@CrossOrigin
@RestController
@RequestMapping("/order")
public class OrderController {


    @Autowired
    OrderService orderService; // Handles all the Service Related Order.

    @Autowired
    CustomerService customerService; // Handles all the Service Related Customer.

    @Autowired
    PaymentService paymentService; // Handles all the Service Related Payment.

    @Autowired
    AddressService addressService; // Handles all the Service Related Address.

    @Autowired
    RestaurantService restaurantService; // Handles all the services related to Restaurant.

    @Autowired
    ItemService itemService;

    /* The method handles get Coupon By CouponName request.It takes authorization from the header and coupon name as the path vataible.
    & produces response in CouponDetailsResponse and returns UUID,Coupon Name and Percentage of coupon present in the DB and if error returns error code and error Message.
    */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET,path = "/coupon/{coupon_name}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(@RequestHeader(value = "authorization") final String authorization, @PathVariable(value = "coupon_name")final String couponName) throws AuthorizationFailedException, CouponNotFoundException {

        //Access the accessToken from the request Header
        String accessToken = authorization.split("Bearer ")[1];

        //Calls customerService getCustomerMethod to check the validity of the customer.this methods returns the customerEntity.
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        //Calls getCouponByCouponName of orderService to get the coupon by name from DB
        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);

        //Creating the couponDetailsResponse containing UUID,Coupon Name and percentage.
        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .couponName(couponEntity.getCouponName())
                .id(UUID.fromString(couponEntity.getUuid()))
                .percent(couponEntity.getPercent());
        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse,HttpStatus.OK);
    }

    /**
     * Takes all required order details from customer and persist the order in DB
     * @param authorization
     * @param saveOrderRequest
     * @return SaveOrderResponse if order is successfully saved
     * @throws AuthorizationFailedException
     * @throws AddressNotFoundException
     * @throws RestaurantNotFoundException
     * @throws CouponNotFoundException
     * @throws PaymentMethodNotFoundException
     */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path="", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(@RequestHeader(value = "authorization") final String authorization, @RequestBody(required = false) final SaveOrderRequest saveOrderRequest)
        throws AuthorizationFailedException, AddressNotFoundException, RestaurantNotFoundException, CouponNotFoundException, PaymentMethodNotFoundException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        CouponEntity couponEntity = orderService
            .getCouponByCouponId(saveOrderRequest.getCouponId().toString());
        PaymentEntity paymentEntity = paymentService
            .getPaymentByUUID(saveOrderRequest.getPaymentId().toString());
        AddressEntity savedAddress = addressService
            .getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity);
        RestaurantEntity restaurantEntity = restaurantService
            .restaurantByUUID(saveOrderRequest.getRestaurantId().toString());

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUuid(UUID.randomUUID().toString());
        orderEntity.setAddress(savedAddress);
        orderEntity.setRestaurant(restaurantEntity);
        orderEntity.setCoupon(couponEntity);
        orderEntity.setPayment(paymentEntity);
        orderEntity.setCustomer(customerEntity);
        orderEntity.setDate(new Date());
        orderEntity.setBill(saveOrderRequest.getBill().doubleValue());

        OrderEntity savedOrder = orderService.saveOrder(orderEntity);

        saveOrderRequest.getItemQuantities().stream().forEach(itemQuantity -> {
            try {
                ItemEntity itemEntity = itemService
                    .getItemByUUID(itemQuantity.getItemId().toString());

                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setOrder(savedOrder);
                orderItemEntity.setQuantity(itemQuantity.getQuantity());
                orderItemEntity.setPrice(itemQuantity.getPrice());
                orderItemEntity.setItem(itemEntity);
                orderService.saveOrderItem(orderItemEntity);
            } catch (ItemNotFoundException e) {
                // TODO: Log the exception
            }
        });

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse();
        saveOrderResponse.setId(savedOrder.getUuid());
        saveOrderResponse.setStatus(HttpStatus.CREATED.toString());
        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }

    /**
     * Return all orders for a customer
     * Customer entity is derived from auth token
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     */
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path="",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getOrdersForCustomer(@RequestHeader(value = "authorization") final String authorization)
        throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        final List<OrderEntity> ordersByCustomers = orderService
            .getOrdersByCustomers(customerEntity.getUuid());

        CustomerOrderResponse response = new CustomerOrderResponse();
        response.setOrders(getOrderListFromOrderEntities(ordersByCustomers));
        return new ResponseEntity<CustomerOrderResponse>(response, HttpStatus.OK);
    }

    /**
     * As orderService returns all saved orders in OrderEntity format,
     * this method transforms list of OrderEntities into list of OrderLists
     * @param orderEntities returned by OrderService.getOrdersByCustomers
     * @return List<OrderList>
     */
    private List<OrderList> getOrderListFromOrderEntities(List<OrderEntity> orderEntities) {
        List<OrderList> orderLists = new ArrayList<>();
        orderEntities.stream().forEach(orderEntity -> {
            OrderList order = new OrderList();
            OrderListCoupon orderListCoupon = new OrderListCoupon();
            OrderListCustomer orderListCustomer = new OrderListCustomer();
            OrderListPayment orderListPayment = new OrderListPayment();
            OrderListAddress orderListAddress = new OrderListAddress();
            OrderListAddressState orderListAddressState = new OrderListAddressState();


            orderListCoupon.setCouponName(orderEntity.getCoupon().getCouponName());
            orderListCoupon.setPercent(orderEntity.getCoupon().getPercent());
            orderListCoupon.setId(UUID.fromString(orderEntity.getCoupon().getUuid()));

            orderListCustomer.setId(UUID.fromString(orderEntity.getCustomer().getUuid()));
            orderListCustomer.setContactNumber(orderEntity.getCustomer().getContactNumber());
            orderListCustomer.setEmailAddress(orderEntity.getCustomer().getEmail());
            orderListCustomer.setFirstName(orderEntity.getCustomer().getFirstName());
            orderListCustomer.setLastName(orderEntity.getCustomer().getLastName());

            orderListPayment.setPaymentName(orderEntity.getPayment().getPaymentName());
            orderListPayment.setId(UUID.fromString(orderEntity.getPayment().getUuid()));

            orderListAddressState.setId(UUID.fromString(orderEntity.getAddress().getStateEntity().getUuid()));
            orderListAddressState.setStateName(orderEntity.getAddress().getStateEntity().getStateName());

            orderListAddress.setState(orderListAddressState);
            orderListAddress.setCity(orderEntity.getAddress().getCity());
            orderListAddress.setFlatBuildingName(orderEntity.getAddress().getFlatBuilNo());
            orderListAddress.setId(UUID.fromString(orderEntity.getAddress().getUuid()));
            orderListAddress.setLocality(orderEntity.getAddress().getLocality());
            orderListAddress.setPincode(orderEntity.getAddress().getPincode());


            order.setId(UUID.fromString(orderEntity.getUuid()));
            order.setBill(BigDecimal.valueOf(orderEntity.getBill()));
            order.setDiscount(BigDecimal.valueOf(orderEntity.getDiscount()));
            order.setDate(orderEntity.getDate().toString());
            order.setCoupon(orderListCoupon);
            order.setCoupon(orderListCoupon);
            order.setCustomer(orderListCustomer);
            order.setPayment(orderListPayment);
            order.setAddress(orderListAddress);


            orderLists.add(order);
        });
        return orderLists;
    }


}
package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import java.util.List;

@CrossOrigin
@RestController
public class AddressController {

    @Autowired
    AddressService stateService;

    @Autowired
    UtilityProvider utilityProvider;

    CustomerService customerService;

    @Autowired
    AddressService addressService;

    /**
     * @return List of states
     */
    @RequestMapping(method = RequestMethod.GET, path="/states", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<StatesListResponse> getAll() {
        List<StateEntity> allStates = stateService.getAllStates();
        StatesListResponse statesListResponse = new StatesListResponse();

        allStates.stream().forEach(stateEntity -> {
            StatesList statesItem = new StatesList();
            statesItem.setId(UUID.fromString(stateEntity.getUuid()));
            statesItem.setStateName(stateEntity.getStateName());

            statesListResponse.addStatesItem(statesItem);
        });
        return  new ResponseEntity<StatesListResponse>(statesListResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader("authorization") final String authorization, @RequestBody(required = false) final SaveAddressRequest saveAddressRequest) throws SaveAddressException, AuthenticationFailedException, AuthorizationFailedException, AddressNotFoundException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setActive(1);

        addressEntity = addressService.saveAddress(addressEntity, stateEntity);

        addressService.saveCustomerAddress(addressEntity, customerEntity);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse();
        saveAddressResponse.setId(addressEntity.getUuid());
        saveAddressResponse.setStatus("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.GET, path = "/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllAddressForCustomer(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        List<AddressEntity> allAddresses = addressService.getAllAddress(customerEntity);
        AddressListResponse  addressListResponse = new AddressListResponse();
        allAddresses.stream().forEach(addressEntity -> {
            AddressListState addressListState = new AddressListState();
            addressListState.setId(UUID.fromString(addressEntity.getStateEntity().getUuid()));
            addressListState.setStateName(addressEntity.getStateEntity().getStateName());

            AddressList addressList = new AddressList();
            addressList.setCity(addressEntity.getCity());
            addressList.setFlatBuildingName(addressEntity.getFlatBuilNo());
            addressList.setId(UUID.fromString(addressEntity.getUuid()));
            addressList.setLocality(addressEntity.getLocality());
            addressList.setState(addressListState);
            addressList.setPincode(addressEntity.getPincode());
            addressListResponse.addAddressesItem(addressList);
        });
        return new ResponseEntity<AddressListResponse>(addressListResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/address/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@RequestHeader("authorization") final String authorization, @PathVariable(value = "address_id") final String addressId) throws AuthorizationFailedException, AuthenticationFailedException, AddressNotFoundException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        AddressEntity addressEntity = addressService.getAddressByUUID(addressId, customerEntity);
        AddressEntity deletedAddressEntity = addressService.deleteAddress(addressEntity);
        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse();
        deleteAddressResponse.setId(UUID.fromString(deletedAddressEntity.getUuid()));
        deleteAddressResponse.setStatus("ADDRESS DELETED SUCCESSFULLY");

        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

}

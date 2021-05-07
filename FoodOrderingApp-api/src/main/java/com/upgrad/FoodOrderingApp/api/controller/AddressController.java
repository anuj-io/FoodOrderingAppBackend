package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.StatesList;
import com.upgrad.FoodOrderingApp.api.model.StatesListResponse;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

import java.util.List;

@CrossOrigin
@RestController
public class AddressController {

    @Autowired
    AddressService stateService;

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

}

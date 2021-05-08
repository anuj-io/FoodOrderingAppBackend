package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.common.UtilityProvider;
import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    @Autowired
    StateDao stateDao;

    @Autowired
    AddressDao addressDao;

    @Autowired
    CustomerAddressDao customerAddressDao;

    @Autowired
    UtilityProvider utilityProvider;

    public List<StateEntity> getAllStates() {
        return stateDao.getAllStates();
    }

    public StateEntity getStateByUUID(String uuid) throws AddressNotFoundException {
        StateEntity stateEntity = stateDao.getStateById(uuid);

        if(stateEntity == null) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }

        return stateEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, StateEntity stateEntity) throws SaveAddressException {
        if(addressEntity.getCity() == null || addressEntity.getFlatBuilNo() == null || addressEntity.getPincode() == null || addressEntity.getLocality() == null) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        } else if(utilityProvider.isPincodeValid(addressEntity.getPincode())) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }
        addressEntity.setState(stateEntity);
        return addressDao.saveAddress(addressEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity saveCustomerAddress(AddressEntity addressEntity, CustomerEntity customerEntity) {
        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setAddressEntity(addressEntity);
        customerAddressEntity.setCustomerEntity(customerEntity);

        return customerAddressDao.saveCustomerAddressEntity(customerAddressEntity);
    }

    public AddressEntity getAddressByUUID(String uuid, CustomerEntity customerEntity) throws AddressNotFoundException, AuthorizationFailedException {
        if(uuid == null || uuid == "") {
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }
        AddressEntity addressEntity = addressDao.getAddressEntityFromUuid(uuid);

        if(addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        CustomerAddressEntity customerAddressEntity = customerAddressDao.getCustomerAddressEntity(addressEntity);
        if (customerAddressEntity.getCustomerEntity().getUuid() != customerEntity.getUuid()) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }
        return addressEntity;
    }

    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {
        return customerAddressDao.getCustomerAddressByCustomer(customerEntity).stream().map(customerAddressEntity -> customerAddressEntity.getAddressEntity()).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        return addressDao.deleteAnswer(addressEntity);
    }
}

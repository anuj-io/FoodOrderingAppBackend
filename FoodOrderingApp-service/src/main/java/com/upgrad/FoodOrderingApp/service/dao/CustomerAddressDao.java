package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CustomerAddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerAddressEntity saveCustomerAddressEntity(CustomerAddressEntity customerAddressEntity) {
        entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }

    public List<CustomerAddressEntity> getCustomerAddressByCustomer(CustomerEntity customerEntity) {
        try {
            return entityManager.createNamedQuery("getAddressesByCustomerId", CustomerAddressEntity.class)
                    .setParameter("customerEntity", customerEntity)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CustomerAddressEntity getCustomerAddressEntity(AddressEntity addressEntity) {
        try {
             return entityManager.createNamedQuery("getCustomerAddressByAddressId", CustomerAddressEntity.class)
                    .setParameter("addressEntity", addressEntity)
                     .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}

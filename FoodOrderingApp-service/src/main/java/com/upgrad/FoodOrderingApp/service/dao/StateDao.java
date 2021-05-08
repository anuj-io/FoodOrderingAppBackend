package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class StateDao {

    @PersistenceContext
    private EntityManager entityManager;

    /*
        Returns a list of state entity
     */
    public List<StateEntity> getAllStates() {
        try {
            return entityManager.createNamedQuery("getAllStates", StateEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public StateEntity getStateById(String uuid) {
        try {
            return entityManager.createNamedQuery("getStateById", StateEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}

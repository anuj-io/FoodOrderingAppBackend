package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CouponDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
public class OrderService {



    @Autowired
    CouponDao couponDao; //Handles all data related to the CouponEntity

    @Autowired
    CustomerDao customerDao; //Handles all data related to the CustomerEntity


    /* This method is to get Coupon By CouponName.Takes the couponName  and returns the Coupon Entity.
    If error throws exception with error code and error message.
    */
    public CouponEntity getCouponByCouponName(String couponName) throws CouponNotFoundException {
        if(couponName == null||couponName == ""){ //Checking if Coupon Name is Null
            throw new CouponNotFoundException("CPF-002","Coupon name field should not be empty");
        }

        //Calls getCouponByCouponName method of CouponDao.
        CouponEntity couponEntity = couponDao.getCouponByCouponName(couponName);
        if(couponEntity == null){ //Checking if couponEntity is Null
            throw new CouponNotFoundException("CPF-001","No coupon by this name");
        }

        return couponEntity;
    }

    /* This method is to get Coupon By CouponId.Takes the couponUuid  and returns the Coupon Entity.
    If error throws exception with error code and error message.
    */
    public CouponEntity getCouponByCouponId(String couponUuid) throws CouponNotFoundException {

        //Calls getCouponByCouponId method of CouponDao to get coupon entity
        CouponEntity couponEntity = couponDao.getCouponByCouponId(couponUuid);
        if(couponEntity == null){   //Checking if couponEntity is Null
            throw new CouponNotFoundException("CPF-002","No coupon by this id");
        }
        return couponEntity;
    }


}
package com.cfs.customerservice.dto;

public class AddressResponse {

    private Long addressId;
    private Long customerId;
    private String addressType;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;

    public AddressResponse() {
    }

    public AddressResponse(
            Long addressId,
            Long customerId,
            String addressType,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String pincode,
            String country) {

        this.addressId = addressId;
        this.customerId = customerId;
        this.addressType = addressType;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.country = country;
    }

    public Long getAddressId() {
        return addressId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getAddressType() {
        return addressType;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }

    public String getCountry() {
        return country;
    }
}
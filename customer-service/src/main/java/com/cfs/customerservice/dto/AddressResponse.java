package com.cfs.customerservice.dto;

public record AddressResponse(
        Long addressId,
        Long customerId,
        String addressType,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String pincode,
        String country) {

    public AddressResponse() {
        this(null, null, null, null, null, null, null, null, null);
    }
}

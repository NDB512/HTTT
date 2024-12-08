package com.example.shopping.services;

import java.util.List;
import java.util.UUID;

import com.example.shopping.dto.AddressRequest;
import com.example.shopping.models.Address;

public interface AddressService {
    Address createAddress(AddressRequest addressRequest, int userId);

    void deleteAddress(UUID id, int userId);

    Address editAddress(UUID id, AddressRequest addressRequest, int userId);

    List<Address> getAddressesByUser(int userId);

    public Address getAddressById(UUID id, int userId);
}

package com.example.shopping.services.impls;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.shopping.dto.AddressRequest;
import com.example.shopping.models.Address;
import com.example.shopping.models.UserDtls;
import com.example.shopping.repositories.AddressRepository;
import com.example.shopping.repositories.UserRepository;
import com.example.shopping.services.AddressService;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired 
    private UserRepository userRepository;

    @Override
    public Address createAddress(AddressRequest addressRequest, int userId) {

        UserDtls user =  userRepository.findById(userId).get();

        Address address = Address.builder()
                                .name(addressRequest.getName())
                                .street(addressRequest.getStreet())
                                .ward(addressRequest.getWard())
                                .district(addressRequest.getDistrict())
                                .city(addressRequest.getCity())
                                .postalCode(addressRequest.getPostalCode())
                                .phoneNumber(addressRequest.getPhoneNumber())
                                .user(user)
                                .build();
        
        return addressRepository.save(address);
    }

    @Override
    public void deleteAddress(UUID id, int userId) {
        UserDtls user =  userRepository.findById(userId).get();
        
        // Lấy địa chỉ theo ID
        Address address = addressRepository.findById(id)
                                           .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Kiểm tra quyền sở hữu
        if (!address.getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Không có quyền xóa địa chỉ này");
        }
        
        addressRepository.delete(address);
    }

    @Override
    public Address editAddress(UUID id, AddressRequest addressRequest, int userId) {
        UserDtls user =  userRepository.findById(userId).get();

        // Tìm địa chỉ theo ID
        Address address = addressRepository.findById(id)
                                           .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
    
        // Kiểm tra nếu địa chỉ thuộc về người dùng hiện tại
        if (!address.getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Không có quyền chỉnh sửa địa chỉ này");
        }
    
        // Cập nhật các trường
        address.setName(addressRequest.getName() != null ? addressRequest.getName() : address.getName());
        address.setStreet(addressRequest.getStreet() != null ? addressRequest.getStreet() : address.getStreet());
        address.setWard(addressRequest.getWard() != null ? addressRequest.getWard() : address.getWard());
        address.setDistrict(addressRequest.getDistrict() != null ? addressRequest.getDistrict() : address.getDistrict());
        address.setCity(addressRequest.getCity() != null ? addressRequest.getCity() : address.getCity());
        address.setPostalCode(addressRequest.getPostalCode() != null ? addressRequest.getPostalCode() : address.getPostalCode());
        address.setPhoneNumber(addressRequest.getPhoneNumber() != null ? addressRequest.getPhoneNumber() : address.getPhoneNumber());
    
        // Lưu lại thay đổi
        return addressRepository.save(address);
    }

    @Override
    public List<Address> getAddressesByUser(int userId) {
        return addressRepository.findAllByUserId(userId);
    }

    @Override
    public Address getAddressById(UUID id, int userId) {

        UserDtls user =  userRepository.findById(userId).get();

        Address address = addressRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        if (!address.getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Không có quyền chỉnh sửa địa chỉ này");
        }
        return address;
    }
}

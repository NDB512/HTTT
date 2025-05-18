package com.example.shopping.repositories;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shopping.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID>{

    List<Address> findAllByUserId(int userId);
}

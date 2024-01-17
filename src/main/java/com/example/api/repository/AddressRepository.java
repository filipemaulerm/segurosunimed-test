package com.example.api.repository;

import com.example.api.domain.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<Address, Long> {

    Page<Address> findAllByOrderByCityAsc(Pageable pageable);

}

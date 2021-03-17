package com.haufegroup.haufebrewery.repository;

import com.haufegroup.haufebrewery.domain.Manufacturer;
import com.haufegroup.haufebrewery.domain.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Manufacturer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
	
	Optional<Manufacturer> findOneByManufacturerName(String manufacturerName);
}

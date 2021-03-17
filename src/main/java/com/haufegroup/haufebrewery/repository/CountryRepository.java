package com.haufegroup.haufebrewery.repository;

import com.haufegroup.haufebrewery.domain.Country;
import com.haufegroup.haufebrewery.domain.Manufacturer;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Country entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
	
	Optional<Country> findOneByCountryCode(String countryCode);
}

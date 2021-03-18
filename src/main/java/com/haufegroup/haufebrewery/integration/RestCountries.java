package com.haufegroup.haufebrewery.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.haufegroup.haufebrewery.domain.Country;
import com.haufegroup.haufebrewery.repository.CountryRepository;
import com.haufegroup.haufebrewery.repository.search.CountrySearchRepository;

public class RestCountries {

	@Autowired
	private Environment environment;

	private String url;

	@Autowired
	private CountryRepository countryRepository;
	
	@Autowired
	private CountrySearchRepository countrySearchRepository;

	public RestCountries(Environment environment, CountryRepository countryRepository, CountrySearchRepository countrySearchRepository) {
		super();
		this.environment = environment;
		this.url = this.environment.getProperty("restcountries.url");
		this.countryRepository = countryRepository;

	}

	public List<Country> getCountrysFromApi(String search) {
		List<Country> result = new ArrayList<Country>();
		RestTemplate restTemplate = new RestTemplate();
		String field = search;
		if (search.contains("=")) {
			String[] searchArray = search.split("=");
			field = searchArray[1];
		}

		ResponseEntity<CountryInfo[]> response = restTemplate.getForEntity(url + "/name/" + field, CountryInfo[].class);
		CountryInfo[] countryInfoArray = response.getBody();

		for (CountryInfo countryInfo : countryInfoArray) {
			Country country = new Country();
			Optional<Country> countryOptional = countryRepository.findOneByCountryCode(countryInfo.getAlpha2Code());
			if (countryOptional.isEmpty()) {
				country.setCountryCode(countryInfo.getAlpha2Code());
				country.setCountryName(countryInfo.getName());
				country.setRegion(countryInfo.getRegion());
				countryRepository.save(country);
				countrySearchRepository.save(country);
			} else {
				country = countryOptional.get();
			}
			
			result.add(country);
		}

		return result;
	}

}

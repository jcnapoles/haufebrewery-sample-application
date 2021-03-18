package com.haufegroup.haufebrewery.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.haufegroup.haufebrewery.repository.BeerRepository;
import com.haufegroup.haufebrewery.repository.ManufacturerRepository;
import com.haufegroup.haufebrewery.repository.search.BeerSearchRepository;
import com.haufegroup.haufebrewery.repository.search.ManufacturerSearchRepository;

class PunkApiTest {
	
	@Autowired
	private Environment environment;

	private String url;

	@Autowired
	private BeerRepository beerRepository;
	
	@Autowired
	private BeerSearchRepository beerSearchRepository;

	@Autowired
	private ManufacturerRepository manufacturerRepository;
	
	@Autowired
	private ManufacturerSearchRepository manufacturerSearchRepository;

	@BeforeEach
	void setUp() throws Exception {
		/*Setop for testing*/
	}

	@Test
	void testPunkApi() {
		//fail("Not yet implemented");
	}

	@Test
	void testGetBeersFromPunkApi() {
		//fail("Not yet implemented");
	}

	@Test
	void testUrlValidator() {
		//fail("Not yet implemented");
	}

}

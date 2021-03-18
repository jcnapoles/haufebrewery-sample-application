package com.haufegroup.haufebrewery.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CountryInfoTest {
	
	 @Test
	    public void equalsVerifier() throws Exception {	        
	        CountryInfo country1 = new CountryInfo();
	        country1.setName("Cuba");
	        CountryInfo country2 = new CountryInfo();
	        country2.setName("Cuba");
	        assertThat(country1).isEqualTo(country2);
	        country2.setName("España");
	        assertThat(country1).isNotEqualTo(country2);
	        country1.setName(null);
	        assertThat(country1).isNotEqualTo(country2);
	    }

}

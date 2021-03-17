package com.haufegroup.haufebrewery.service.dto;

import com.haufegroup.haufebrewery.domain.Country;

public class ManufacturerDTO {
	
	private Long id;
	
	private String manufacturerName;
	
	private String email;
	
	private String password;
	
	private Country country;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "ManufacturerDTO [id=" + id + ", manufacturerName=" + manufacturerName + ", email=" + email
				+ ", password=" + password + ", country=" + country + "]";
	}

	
	
}

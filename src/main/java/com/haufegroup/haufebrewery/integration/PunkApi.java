package com.haufegroup.haufebrewery.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.haufegroup.haufebrewery.domain.Beer;
import com.haufegroup.haufebrewery.domain.Manufacturer;
import com.haufegroup.haufebrewery.repository.BeerRepository;
import com.haufegroup.haufebrewery.repository.ManufacturerRepository;
import com.haufegroup.haufebrewery.repository.search.BeerSearchRepository;
import com.haufegroup.haufebrewery.repository.search.ManufacturerSearchRepository;

public class PunkApi {

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

	public PunkApi(Environment environment, BeerRepository beerRepository, BeerSearchRepository beerSearchRepository,
			ManufacturerRepository manufacturerRepository, ManufacturerSearchRepository manufacturerSearchRepository) {
		super();
		this.environment = environment;
		this.url = this.environment.getProperty("punkapi.url");
		this.beerRepository = beerRepository;
		this.beerSearchRepository = beerSearchRepository;
		this.manufacturerRepository = manufacturerRepository;
		this.manufacturerSearchRepository = manufacturerSearchRepository;
	}

	public List<Beer> getBeersFromPunkApi(String search) {
		List<Beer> result = new ArrayList<Beer>();
		RestTemplate restTemplate = new RestTemplate();
		String field = search;
		if (search.contains("=")) {
			String[] searchArray = search.split("=");
			field = searchArray[1];
		}

		ResponseEntity<String> response = restTemplate.getForEntity(url + "?beer_name=" + field.replaceAll(" ", "_"),
				String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root;
		JsonNode nameNode = null;
		JsonNode descriptionNode = null;
		JsonNode imageNode = null;
		JsonNode abvNode = null;
		JsonNode taglineNode = null;

		try {
			root = mapper.readTree(response.getBody());
			Manufacturer manufacturer = new Manufacturer();
			Optional<Manufacturer> manufacturerOptional = manufacturerRepository.findOneByManufacturerName("External");
			if (manufacturerOptional.isEmpty()) {
				manufacturer.setManufacturerName("External");
				manufacturerRepository.save(manufacturer);
				manufacturerSearchRepository.save(manufacturer);
			} else {
				manufacturer = manufacturerOptional.get();
			}

			for (JsonNode jsonNode : root) {
				nameNode = jsonNode.path("name");
				descriptionNode = jsonNode.path("description");
				imageNode = jsonNode.path("image_url");
				abvNode = jsonNode.path("abv");
				taglineNode = jsonNode.path("tagline");
				Beer beer = new Beer();
				beer.setBeerName(nameNode.textValue());
				beer.setDescription(descriptionNode.textValue());
				beer.setGraduation(abvNode.asDouble());
				beer.setType(taglineNode.textValue());
				byte[] image = convertUrlToByte(imageNode.textValue());
				beer.setImage(image);
				String extension = imageNode.textValue();
				if (extension != null && !"".equalsIgnoreCase(extension)) {
					extension = extension.substring(imageNode.textValue().lastIndexOf(".") + 1);
					beer.setImageContentType("image/" + extension);
				}
				beer.setManufaturer(manufacturer);
				beerRepository.save(beer);
				beerSearchRepository.save(beer);
				result.add(beer);
			}

		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	private byte[] convertUrlToByte(String textValue) throws IOException {
		byte[] img = null;
		if (textValue != null && !"".equalsIgnoreCase(textValue)) {
			if (urlValidator(textValue)) {
				URL url = new URL(textValue);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				URLConnection conn = url.openConnection();

				try (InputStream inputStream = conn.getInputStream()) {
					int n = 0;
					byte[] buffer = new byte[1024];
					while (-1 != (n = inputStream.read(buffer))) {
						output.write(buffer, 0, n);
					}
				}
				img = output.toByteArray();
			}

		}

		return img;

	}

	public boolean urlValidator(String url) {
		UrlValidator defaultValidator = new UrlValidator();
		return defaultValidator.isValid(url);
	}

}

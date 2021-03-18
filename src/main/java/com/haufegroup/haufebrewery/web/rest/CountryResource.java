package com.haufegroup.haufebrewery.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.haufegroup.haufebrewery.domain.Country;
import com.haufegroup.haufebrewery.integration.RestCountries;
import com.haufegroup.haufebrewery.repository.CountryRepository;
import com.haufegroup.haufebrewery.repository.search.CountrySearchRepository;
import com.haufegroup.haufebrewery.security.AuthoritiesConstants;
import com.haufegroup.haufebrewery.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.haufegroup.haufebrewery.domain.Country}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class CountryResource {

    private final Logger log = LoggerFactory.getLogger(CountryResource.class);

    private static final String ENTITY_NAME = "country";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CountryRepository countryRepository;

    private final CountrySearchRepository countrySearchRepository;
    
    @Autowired
    private Environment environment;

    public CountryResource(CountryRepository countryRepository, CountrySearchRepository countrySearchRepository) {
        this.countryRepository = countryRepository;
        this.countrySearchRepository = countrySearchRepository;
    }

    /**
     * {@code POST  /countries} : Create a new country.
     *
     * @param country the country to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new country, or with status {@code 400 (Bad Request)} if the country has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/countries")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Country> createCountry(@RequestBody Country country) throws URISyntaxException {
        log.debug("REST request to save Country : {}", country);
        if (country.getId() != null) {
            throw new BadRequestAlertException("A new country cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Country result = countryRepository.save(country);
        countrySearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/countries/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /countries} : Updates an existing country.
     *
     * @param country the country to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated country,
     * or with status {@code 400 (Bad Request)} if the country is not valid,
     * or with status {@code 500 (Internal Server Error)} if the country couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/countries")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Country> updateCountry(@RequestBody Country country) throws URISyntaxException {
        log.debug("REST request to update Country : {}", country);
        if (country.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Country result = countryRepository.save(country);
        countrySearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, country.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /countries} : get all the countries.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of countries in body.
     */
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries(Pageable pageable) {
        log.debug("REST request to get a page of Countries");
        Page<Country> page = countryRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /countries/:id} : get the "id" country.
     *
     * @param id the id of the country to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the country, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/countries/{id}")
    public ResponseEntity<Country> getCountry(@PathVariable Long id) {
        log.debug("REST request to get Country : {}", id);
        Optional<Country> country = countryRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(country);
    }

    /**
     * {@code DELETE  /countries/:id} : delete the "id" country.
     *
     * @param id the id of the country to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/countries/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id) {
        log.debug("REST request to delete Country : {}", id);
        countryRepository.deleteById(id);
        countrySearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code SEARCH  /_search/countries?query=:query} : search for the country corresponding
     * to the query.
     *
     * @param query the query of the country search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/countries")
    public ResponseEntity<List<Country>> searchCountries(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Countries for query {}", query);
        Page<Country> page = countrySearchRepository.search(queryStringQuery(query), pageable);
        
        if (page.getTotalElements() == 0) {        	
            RestCountries restCountries = new RestCountries(environment,countryRepository,countrySearchRepository);
            List<Country> countryList = restCountries.getCountrysFromApi(query);
            page = new PageImpl<Country>(countryList);
		}
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
        }
}

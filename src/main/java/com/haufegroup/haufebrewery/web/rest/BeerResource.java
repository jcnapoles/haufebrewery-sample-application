package com.haufegroup.haufebrewery.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.zalando.problem.Status;

import com.haufegroup.haufebrewery.domain.Beer;
import com.haufegroup.haufebrewery.domain.Manufacturer;
import com.haufegroup.haufebrewery.domain.User;
import com.haufegroup.haufebrewery.integration.PunkApi;
import com.haufegroup.haufebrewery.repository.BeerRepository;
import com.haufegroup.haufebrewery.repository.ManufacturerRepository;
import com.haufegroup.haufebrewery.repository.search.BeerSearchRepository;
import com.haufegroup.haufebrewery.repository.search.ManufacturerSearchRepository;
import com.haufegroup.haufebrewery.security.AuthoritiesConstants;
import com.haufegroup.haufebrewery.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.haufegroup.haufebrewery.domain.Beer}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class BeerResource {

    private final Logger log = LoggerFactory.getLogger(BeerResource.class);

    private static final String ENTITY_NAME = "beer";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BeerRepository beerRepository;

    private final BeerSearchRepository beerSearchRepository;  
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private ManufacturerRepository manufacturerRepository;
    
    @Autowired
    private ManufacturerSearchRepository manufacturerSearchRepository;

    public BeerResource(BeerRepository beerRepository, BeerSearchRepository beerSearchRepository) {
        this.beerRepository = beerRepository;
        this.beerSearchRepository = beerSearchRepository;
    }

    /**
     * {@code POST  /beers} : Create a new beer.
     *
     * @param beer the beer to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new beer, or with status {@code 400 (Bad Request)} if the beer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/beers")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Beer> createBeer(@Valid @RequestBody Beer beer) throws URISyntaxException {
        log.debug("REST request to save Beer : {}", beer);
        if (beer.getId() != null) {
            throw new BadRequestAlertException("A new beer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = auth.getName();
        Manufacturer manufacturer = beer.getManufaturer();
        if (manufacturer != null) {
        	Optional<Manufacturer> manufacturerBD = manufacturerRepository.findById(manufacturer.getId());
			if (!manufacturerBD.isEmpty()) {
				manufacturer = manufacturerBD.get();
				User internalUser = manufacturer.getInternalUser();
				if (internalUser != null) {
					if (user.equalsIgnoreCase(internalUser.getLogin())) {						
							beer.setManufaturer(manufacturer);
						 	Beer result = beerRepository.save(beer);
					        beerSearchRepository.save(result);
					        return ResponseEntity.created(new URI("/api/beers/" + result.getId()))
					            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
					            .body(result);
					}
				}

			}
		} else {			
			/*Search Manufacturer by user*/
			Optional<Manufacturer> manufacturerBD = manufacturerRepository.findOneByManufacturerName(user);
			
			manufacturer = manufacturerBD.get();
			if (!manufacturerBD.isEmpty()) {
				User internalUser = manufacturer.getInternalUser();
				if (internalUser != null) {
					if (user.equalsIgnoreCase(internalUser.getLogin())) {
						beer.setManufaturer(manufacturer);
						Beer result = beerRepository.save(beer);
						beerSearchRepository.save(result);
						return ResponseEntity.created(new URI("/api/beers/" + result.getId()))
								.headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
										result.getId().toString()))
								.body(result);
					}
				}

			}
			
		}
        
        throw new AccessDeniedException(Status.FORBIDDEN.name() +  " does not have the necessary permissions " + ENTITY_NAME); 
        
    }

    /**
     * {@code PUT  /beers} : Updates an existing beer.
     *
     * @param beer the beer to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated beer,
     * or with status {@code 400 (Bad Request)} if the beer is not valid,
     * or with status {@code 500 (Internal Server Error)} if the beer couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/beers")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Beer> updateBeer(@Valid @RequestBody Beer beer) throws URISyntaxException {
        log.debug("REST request to update Beer : {}", beer);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String user = auth.getName();        
        Optional<Beer> optionalBeer = beerRepository.findById(beer.getId());
        Beer beerDB = new Beer();
        if (!optionalBeer.isEmpty()) {
        	beerDB = optionalBeer.get();
			Manufacturer manufacturer = beerDB.getManufaturer();
			Optional<Manufacturer> manufacturerBD = manufacturerRepository.findById(manufacturer.getId());
			if (!manufacturerBD.isEmpty()) {
				manufacturer = manufacturerBD.get();
				User internalUser = manufacturer.getInternalUser();
				if (internalUser != null) {
					if (user.equalsIgnoreCase(internalUser.getLogin())) {
						
						 if (beer.getId() == null) {
					            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
					        }
					        Beer result = beerRepository.save(beer);
					        beerSearchRepository.save(result);
					        return ResponseEntity.ok()
					            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, beer.getId().toString()))
					            .body(result);
					}
				}

			}
		} else {

		}
        		
        throw new AccessDeniedException(Status.FORBIDDEN.name() +  " forbidden to modify this " + ENTITY_NAME);
    }

    /**
     * {@code GET  /beers} : get all the beers.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of beers in body.
     */
    @GetMapping("/beers")
    public ResponseEntity<List<Beer>> getAllBeers(Pageable pageable) {
        log.debug("REST request to get a page of Beers");
        Page<Beer> page = beerRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /beers/:id} : get the "id" beer.
     *
     * @param id the id of the beer to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the beer, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/beers/{id}")
    public ResponseEntity<Beer> getBeer(@PathVariable Long id) {
        log.debug("REST request to get Beer : {}", id);
        Optional<Beer> beer = beerRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(beer);
    }

    /**
     * {@code DELETE  /beers/:id} : delete the "id" beer.
     *
     * @param id the id of the beer to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/beers/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Void> deleteBeer(@PathVariable Long id) {
        log.debug("REST request to delete Beer : {}", id);
        beerRepository.deleteById(id);
        beerSearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code SEARCH  /_search/beers?query=:query} : search for the beer corresponding
     * to the query.
     *
     * @param query the query of the beer search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/beers")
    public ResponseEntity<List<Beer>> searchBeers(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Beers for query {}", query);
        Page<Beer> page = beerSearchRepository.search(queryStringQuery(query), pageable);
        
        if (page.getTotalElements() == 0) {        	
            PunkApi punkApi = new PunkApi(environment,beerRepository,beerSearchRepository,manufacturerRepository,manufacturerSearchRepository);
            List<Beer> beerList = punkApi.getBeersFromPunkApi(query);
            page = new PageImpl<Beer>(beerList);
		}
       
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
        }
}

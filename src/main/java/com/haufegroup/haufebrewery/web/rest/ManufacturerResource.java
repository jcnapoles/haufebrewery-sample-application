package com.haufegroup.haufebrewery.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

import com.haufegroup.haufebrewery.domain.Manufacturer;
import com.haufegroup.haufebrewery.domain.User;
import com.haufegroup.haufebrewery.repository.ManufacturerRepository;
import com.haufegroup.haufebrewery.repository.search.ManufacturerSearchRepository;
import com.haufegroup.haufebrewery.security.AuthoritiesConstants;
import com.haufegroup.haufebrewery.service.UserService;
import com.haufegroup.haufebrewery.service.dto.UserDTO;
import com.haufegroup.haufebrewery.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.haufegroup.haufebrewery.domain.Manufacturer}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class ManufacturerResource {

    private final Logger log = LoggerFactory.getLogger(ManufacturerResource.class);

    private static final String ENTITY_NAME = "manufacturer";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ManufacturerRepository manufacturerRepository;

    private final ManufacturerSearchRepository manufacturerSearchRepository;      
   
    @Autowired
    private UserService userService;
    

    public ManufacturerResource(ManufacturerRepository manufacturerRepository, ManufacturerSearchRepository manufacturerSearchRepository) {
        this.manufacturerRepository = manufacturerRepository;
        this.manufacturerSearchRepository = manufacturerSearchRepository;
    }

    /**
     * {@code POST  /manufacturers} : Create a new manufacturer.
     *
     * @param manufacturer the manufacturer to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new manufacturer, or with status {@code 400 (Bad Request)} if the manufacturer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/manufacturers")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Manufacturer> createManufacturer(@RequestBody Manufacturer manufacturer) throws URISyntaxException {
        log.debug("REST request to save Manufacturer : {}", manufacturer);
        if (manufacturer.getId() != null) {
            throw new BadRequestAlertException("A new manufacturer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String userLogged = auth.getName();		
		        
        UserDTO userDto = new UserDTO();
		userDto.setActivated(true);
		userDto.setCreatedBy(userLogged);       
		userDto.setCreatedDate(Instant.now());
		userDto.setEmail(manufacturer.getManufacturerName() + "@gmail.com");
		userDto.setFirstName(manufacturer.getManufacturerName());
		userDto.setLastName(manufacturer.getManufacturerName());
		userDto.setLogin(manufacturer.getManufacturerName());		
        Set<String> authorities = new HashSet<String>();     
        authorities.add("ROLE_USER");
        userDto.setAuthorities(authorities);        
        User user = userService.registerUser(userDto, manufacturer.getManufacturerName());
        user.setActivated(true);
        manufacturer.setInternalUser(user);
        Manufacturer result = manufacturerRepository.save(manufacturer);
        manufacturerRepository.save(result);
        manufacturerSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/manufacturers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /manufacturers} : Updates an existing manufacturer.
     *
     * @param manufacturer the manufacturer to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated manufacturer,
     * or with status {@code 400 (Bad Request)} if the manufacturer is not valid,
     * or with status {@code 500 (Internal Server Error)} if the manufacturer couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
	@PutMapping("/manufacturers")
	@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER + "\") or hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
	public ResponseEntity<Manufacturer> updateManufacturer(@RequestBody Manufacturer manufacturer)
			throws URISyntaxException {
		log.debug("REST request to update Manufacturer : {}", manufacturer);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();		
		String userLogged = auth.getName();
		Collection<? extends GrantedAuthority> grantedAutorities =  auth.getAuthorities();		
		Boolean granted = false;
		for (Iterator<? extends GrantedAuthority> iterator = grantedAutorities.iterator(); iterator.hasNext();) {
			GrantedAuthority grantedAuthority = (GrantedAuthority) iterator.next();
			if (grantedAuthority.getAuthority().equalsIgnoreCase("ROLE_ADMIN")) {
				granted = true;
				break;
			}
		}
		if (manufacturer.getId() == null) {			
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		} 
		Optional<Manufacturer> manufacturerBD = manufacturerRepository.findById(manufacturer.getId());
		
		if (!manufacturerBD.isEmpty()) {
			User internalUser = manufacturerBD.get().getInternalUser();
			if (internalUser != null) {
				if (userLogged.equalsIgnoreCase(internalUser.getLogin()) || granted) {
					if (manufacturer.getId() == null) {
						throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
					}
					manufacturer.setInternalUser(internalUser);
					Manufacturer result = manufacturerRepository.save(manufacturer);
					manufacturerSearchRepository.save(result);
					return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true,
							ENTITY_NAME, manufacturer.getId().toString())).body(result);
				}
			} else {
				UserDTO userDto = new UserDTO();
				userDto.setActivated(true);
				userDto.setCreatedBy(userLogged);       
				userDto.setCreatedDate(Instant.now());
				userDto.setEmail(manufacturer.getManufacturerName() + "@gmail.com");
				userDto.setFirstName(manufacturer.getManufacturerName());
				userDto.setLastName(manufacturer.getManufacturerName());
				userDto.setLogin(manufacturer.getManufacturerName());		
		        Set<String> authorities = new HashSet<String>();     
		        authorities.add("ROLE_USER");
		        userDto.setAuthorities(authorities);        
		        User user = userService.registerUser(userDto, manufacturer.getManufacturerName());
		        user.setActivated(true);		     
		        
		        if (userLogged.equalsIgnoreCase(user.getLogin()) || granted) {
					if (manufacturer.getId() == null) {
						throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
					}
					manufacturer.setInternalUser(user);
					Manufacturer result = manufacturerRepository.save(manufacturer);
					manufacturerSearchRepository.save(result);
					return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true,
							ENTITY_NAME, manufacturer.getId().toString())).body(result);
				}
			}

		}

		throw new AccessDeniedException(Status.FORBIDDEN.name());

	}

    /**
     * {@code GET  /manufacturers} : get all the manufacturers.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of manufacturers in body.
     */
    @GetMapping("/manufacturers")
    public ResponseEntity<List<Manufacturer>> getAllManufacturers(Pageable pageable) {
        log.debug("REST request to get a page of Manufacturers");
        Page<Manufacturer> page = manufacturerRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /manufacturers/:id} : get the "id" manufacturer.
     *
     * @param id the id of the manufacturer to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the manufacturer, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/manufacturers/{id}")
    public ResponseEntity<Manufacturer> getManufacturer(@PathVariable Long id) {
        log.debug("REST request to get Manufacturer : {}", id);
        Optional<Manufacturer> manufacturer = manufacturerRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(manufacturer);
    }

    /**
     * {@code DELETE  /manufacturers/:id} : delete the "id" manufacturer.
     *
     * @param id the id of the manufacturer to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/manufacturers/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteManufacturer(@PathVariable Long id) {
        log.debug("REST request to delete Manufacturer : {}", id);
        manufacturerRepository.deleteById(id);
        manufacturerSearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code SEARCH  /_search/manufacturers?query=:query} : search for the manufacturer corresponding
     * to the query.
     *
     * @param query the query of the manufacturer search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search/manufacturers")
    public ResponseEntity<List<Manufacturer>> searchManufacturers(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Manufacturers for query {}", query);
        Page<Manufacturer> page = manufacturerSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
        }
}

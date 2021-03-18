package com.haufegroup.haufebrewery.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import com.haufegroup.haufebrewery.HaufebreweryApp;
import com.haufegroup.haufebrewery.domain.Beer;
import com.haufegroup.haufebrewery.repository.BeerRepository;
import com.haufegroup.haufebrewery.repository.search.BeerSearchRepository;

/**
 * Integration tests for the {@link BeerResource} REST controller.
 */
@SpringBootTest(classes = HaufebreweryApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class BeerResourceIT {

    private static final String DEFAULT_BEER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_BEER_NAME = "BBBBBBBBBB";

    private static final Double DEFAULT_GRADUATION = 1D;
    private static final Double UPDATED_GRADUATION = 2D;

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_CONTENT_TYPE = "image/png";

    @Autowired
    private BeerRepository beerRepository;

    /**
     * This repository is mocked in the com.haufegroup.haufebrewery.repository.search test package.
     *
     * @see com.haufegroup.haufebrewery.repository.search.BeerSearchRepositoryMockConfiguration
     */
    @Autowired
    private BeerSearchRepository mockBeerSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBeerMockMvc;

    private Beer beer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Beer createEntity(EntityManager em) {
        Beer beer = new Beer()
            .beerName(DEFAULT_BEER_NAME)
            .graduation(DEFAULT_GRADUATION)
            .type(DEFAULT_TYPE)
            .description(DEFAULT_DESCRIPTION)
            .image(DEFAULT_IMAGE)
            .imageContentType(DEFAULT_IMAGE_CONTENT_TYPE);
        return beer;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Beer createUpdatedEntity(EntityManager em) {
        Beer beer = new Beer()
            .beerName(UPDATED_BEER_NAME)
            .graduation(UPDATED_GRADUATION)
            .type(UPDATED_TYPE)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE);
        return beer;
    }

    @BeforeEach
    public void initTest() {
        beer = createEntity(em);
    }

    @Test
    @Transactional
    public void createBeer() throws Exception {
        int databaseSizeBeforeCreate = beerRepository.findAll().size();
        // Create the Beer
        restBeerMockMvc.perform(post("/api/beers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(beer)))
            .andExpect(status().isCreated());

        // Validate the Beer in the database
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeCreate + 1);
        Beer testBeer = beerList.get(beerList.size() - 1);
        assertThat(testBeer.getBeerName()).isEqualTo(DEFAULT_BEER_NAME);
        assertThat(testBeer.getGraduation()).isEqualTo(DEFAULT_GRADUATION);
        assertThat(testBeer.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testBeer.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
        assertThat(testBeer.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testBeer.getImageContentType()).isEqualTo(DEFAULT_IMAGE_CONTENT_TYPE);

        // Validate the Beer in Elasticsearch
        verify(mockBeerSearchRepository, times(1)).save(testBeer);
    }

    @Test
    @Transactional
    public void createBeerWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = beerRepository.findAll().size();

        // Create the Beer with an existing ID
        beer.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restBeerMockMvc.perform(post("/api/beers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(beer)))
            .andExpect(status().isBadRequest());

        // Validate the Beer in the database
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeCreate);

        // Validate the Beer in Elasticsearch
        verify(mockBeerSearchRepository, times(0)).save(beer);
    }


    @Test
    @Transactional
    public void checkBeerNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = beerRepository.findAll().size();
        // set the field null
        beer.setBeerName(null);

        // Create the Beer, which fails.


        restBeerMockMvc.perform(post("/api/beers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(beer)))
            .andExpect(status().isBadRequest());

        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllBeers() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        // Get all the beerList
        restBeerMockMvc.perform(get("/api/beers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(beer.getId().intValue())))
            .andExpect(jsonPath("$.[*].beerName").value(hasItem(DEFAULT_BEER_NAME)))
            .andExpect(jsonPath("$.[*].graduation").value(hasItem(DEFAULT_GRADUATION.doubleValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))));
    }
    
    @Test
    @Transactional
    public void getBeer() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        // Get the beer
        restBeerMockMvc.perform(get("/api/beers/{id}", beer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(beer.getId().intValue()))
            .andExpect(jsonPath("$.beerName").value(DEFAULT_BEER_NAME))
            .andExpect(jsonPath("$.graduation").value(DEFAULT_GRADUATION.doubleValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.imageContentType").value(DEFAULT_IMAGE_CONTENT_TYPE))
            .andExpect(jsonPath("$.image").value(Base64Utils.encodeToString(DEFAULT_IMAGE)));
    }
    @Test
    @Transactional
    public void getNonExistingBeer() throws Exception {
        // Get the beer
        restBeerMockMvc.perform(get("/api/beers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateBeer() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        int databaseSizeBeforeUpdate = beerRepository.findAll().size();

        // Update the beer
        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        // Disconnect from session so that the updates on updatedBeer are not directly saved in db
        em.detach(updatedBeer);
        updatedBeer
            .beerName(UPDATED_BEER_NAME)
            .graduation(UPDATED_GRADUATION)
            .type(UPDATED_TYPE)
            .description(UPDATED_DESCRIPTION)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE);

        restBeerMockMvc.perform(put("/api/beers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedBeer)))
            .andExpect(status().isOk());

        // Validate the Beer in the database
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeUpdate);
        Beer testBeer = beerList.get(beerList.size() - 1);
        assertThat(testBeer.getBeerName()).isEqualTo(UPDATED_BEER_NAME);
        assertThat(testBeer.getGraduation()).isEqualTo(UPDATED_GRADUATION);
        assertThat(testBeer.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testBeer.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
        assertThat(testBeer.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testBeer.getImageContentType()).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE);

        // Validate the Beer in Elasticsearch
        verify(mockBeerSearchRepository, times(1)).save(testBeer);
    }

    @Test
    @Transactional
    public void updateNonExistingBeer() throws Exception {
        int databaseSizeBeforeUpdate = beerRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBeerMockMvc.perform(put("/api/beers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(beer)))
            .andExpect(status().isBadRequest());

        // Validate the Beer in the database
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Beer in Elasticsearch
        verify(mockBeerSearchRepository, times(0)).save(beer);
    }

    @Test
    @Transactional
    public void deleteBeer() throws Exception {
        // Initialize the database
        beerRepository.saveAndFlush(beer);

        int databaseSizeBeforeDelete = beerRepository.findAll().size();

        // Delete the beer
        restBeerMockMvc.perform(delete("/api/beers/{id}", beer.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Beer> beerList = beerRepository.findAll();
        assertThat(beerList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Beer in Elasticsearch
        verify(mockBeerSearchRepository, times(1)).deleteById(beer.getId());
    }

    @Test
    @Transactional
    public void searchBeer() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        beerRepository.saveAndFlush(beer);
        when(mockBeerSearchRepository.search(queryStringQuery("id:" + beer.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(beer), PageRequest.of(0, 1), 1));

        // Search the beer
        restBeerMockMvc.perform(get("/api/_search/beers?query=id:" + beer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(beer.getId().intValue())))
            .andExpect(jsonPath("$.[*].beerName").value(hasItem(DEFAULT_BEER_NAME)))
            .andExpect(jsonPath("$.[*].graduation").value(hasItem(DEFAULT_GRADUATION.doubleValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))));
    }
}

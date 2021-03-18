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

import com.haufegroup.haufebrewery.HaufebreweryApp;
import com.haufegroup.haufebrewery.domain.Manufacturer;
import com.haufegroup.haufebrewery.repository.ManufacturerRepository;
import com.haufegroup.haufebrewery.repository.search.ManufacturerSearchRepository;

/**
 * Integration tests for the {@link ManufacturerResource} REST controller.
 */
@SpringBootTest(classes = HaufebreweryApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class ManufacturerResourceIT {

    private static final String DEFAULT_MANUFACTURER_NAME = "AAAAAAAAAA";
    private static final String UPDATED_MANUFACTURER_NAME = "BBBBBBBBBB";

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    /**
     * This repository is mocked in the com.haufegroup.haufebrewery.repository.search test package.
     *
     * @see com.haufegroup.haufebrewery.repository.search.ManufacturerSearchRepositoryMockConfiguration
     */
    @Autowired
    private ManufacturerSearchRepository mockManufacturerSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restManufacturerMockMvc;

    private Manufacturer manufacturer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Manufacturer createEntity(EntityManager em) {
        Manufacturer manufacturer = new Manufacturer()
            .manufacturerName(DEFAULT_MANUFACTURER_NAME);
        return manufacturer;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Manufacturer createUpdatedEntity(EntityManager em) {
        Manufacturer manufacturer = new Manufacturer()
            .manufacturerName(UPDATED_MANUFACTURER_NAME);
        return manufacturer;
    }

    @BeforeEach
    public void initTest() {
        manufacturer = createEntity(em);
    }

    @Test
    @Transactional
    @WithMockUser(username = "Admin", password = "admin", authorities = "ROLE_ADMIN")
    public void createManufacturer() throws Exception {
        int databaseSizeBeforeCreate = manufacturerRepository.findAll().size();
        // Create the Manufacturer
        restManufacturerMockMvc.perform(post("/api/manufacturers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(manufacturer)))
            .andExpect(status().isCreated());

        // Validate the Manufacturer in the database
        List<Manufacturer> manufacturerList = manufacturerRepository.findAll();
        assertThat(manufacturerList).hasSize(databaseSizeBeforeCreate + 1);
        Manufacturer testManufacturer = manufacturerList.get(manufacturerList.size() - 1);
        assertThat(testManufacturer.getManufacturerName()).isEqualTo(DEFAULT_MANUFACTURER_NAME);

        // Validate the Manufacturer in Elasticsearch
        verify(mockManufacturerSearchRepository, times(1)).save(testManufacturer);
    }

    @Test
    @Transactional
    @WithMockUser(username = "Admin", password = "admin", authorities = "ROLE_ADMIN")
    public void createManufacturerWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = manufacturerRepository.findAll().size();

        // Create the Manufacturer with an existing ID
        manufacturer.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restManufacturerMockMvc.perform(post("/api/manufacturers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(manufacturer)))
            .andExpect(status().isBadRequest());

        // Validate the Manufacturer in the database
        List<Manufacturer> manufacturerList = manufacturerRepository.findAll();
        assertThat(manufacturerList).hasSize(databaseSizeBeforeCreate);

        // Validate the Manufacturer in Elasticsearch
        verify(mockManufacturerSearchRepository, times(0)).save(manufacturer);
    }


    @Test
    @Transactional
    public void getAllManufacturers() throws Exception {
        // Initialize the database
        manufacturerRepository.saveAndFlush(manufacturer);

        // Get all the manufacturerList
        restManufacturerMockMvc.perform(get("/api/manufacturers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(manufacturer.getId().intValue())))
            .andExpect(jsonPath("$.[*].manufacturerName").value(hasItem(DEFAULT_MANUFACTURER_NAME)));
    }
    
    @Test
    @Transactional
    public void getManufacturer() throws Exception {
        // Initialize the database
        manufacturerRepository.saveAndFlush(manufacturer);

        // Get the manufacturer
        restManufacturerMockMvc.perform(get("/api/manufacturers/{id}", manufacturer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(manufacturer.getId().intValue()))
            .andExpect(jsonPath("$.manufacturerName").value(DEFAULT_MANUFACTURER_NAME));
    }
    @Test
    @Transactional
    public void getNonExistingManufacturer() throws Exception {
        // Get the manufacturer
        restManufacturerMockMvc.perform(get("/api/manufacturers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(username = "Admin", password = "admin", authorities = "ROLE_ADMIN")
    public void updateManufacturer() throws Exception {
        // Initialize the database
        manufacturerRepository.saveAndFlush(manufacturer);

        int databaseSizeBeforeUpdate = manufacturerRepository.findAll().size();

        // Update the manufacturer
        Manufacturer updatedManufacturer = manufacturerRepository.findById(manufacturer.getId()).get();
        // Disconnect from session so that the updates on updatedManufacturer are not directly saved in db
        em.detach(updatedManufacturer);
        updatedManufacturer
            .manufacturerName(UPDATED_MANUFACTURER_NAME);

        restManufacturerMockMvc.perform(put("/api/manufacturers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedManufacturer)))
            .andExpect(status().isOk());

        // Validate the Manufacturer in the database
        List<Manufacturer> manufacturerList = manufacturerRepository.findAll();
        assertThat(manufacturerList).hasSize(databaseSizeBeforeUpdate);
        Manufacturer testManufacturer = manufacturerList.get(manufacturerList.size() - 1);
        assertThat(testManufacturer.getManufacturerName()).isEqualTo(UPDATED_MANUFACTURER_NAME);

        // Validate the Manufacturer in Elasticsearch
        verify(mockManufacturerSearchRepository, times(1)).save(testManufacturer);
    }

    @Test
    @Transactional
    @WithMockUser(username = "Admin", password = "admin", authorities = "ROLE_ADMIN")
    public void updateNonExistingManufacturer() throws Exception {
        int databaseSizeBeforeUpdate = manufacturerRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restManufacturerMockMvc.perform(put("/api/manufacturers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(manufacturer)))
            .andExpect(status().isBadRequest());

        // Validate the Manufacturer in the database
        List<Manufacturer> manufacturerList = manufacturerRepository.findAll();
        assertThat(manufacturerList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Manufacturer in Elasticsearch
        verify(mockManufacturerSearchRepository, times(0)).save(manufacturer);
    }

    @Test
    @Transactional
    @WithMockUser(username = "Admin", password = "admin", authorities = "ROLE_ADMIN")
    public void deleteManufacturer() throws Exception {
        // Initialize the database
        manufacturerRepository.saveAndFlush(manufacturer);

        int databaseSizeBeforeDelete = manufacturerRepository.findAll().size();

        // Delete the manufacturer
        restManufacturerMockMvc.perform(delete("/api/manufacturers/{id}", manufacturer.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Manufacturer> manufacturerList = manufacturerRepository.findAll();
        assertThat(manufacturerList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Manufacturer in Elasticsearch
        verify(mockManufacturerSearchRepository, times(1)).deleteById(manufacturer.getId());
    }

    @Test
    @Transactional
    @WithMockUser(username = "Admin", password = "admin", authorities = "ROLE_ADMIN")
    public void searchManufacturer() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        manufacturerRepository.saveAndFlush(manufacturer);
        when(mockManufacturerSearchRepository.search(queryStringQuery("id:" + manufacturer.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(manufacturer), PageRequest.of(0, 1), 1));

        // Search the manufacturer
        restManufacturerMockMvc.perform(get("/api/_search/manufacturers?query=id:" + manufacturer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(manufacturer.getId().intValue())))
            .andExpect(jsonPath("$.[*].manufacturerName").value(hasItem(DEFAULT_MANUFACTURER_NAME)));
    }
}

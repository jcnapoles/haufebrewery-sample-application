package com.haufegroup.haufebrewery.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link ManufacturerSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class ManufacturerSearchRepositoryMockConfiguration {

    @MockBean
    private ManufacturerSearchRepository mockManufacturerSearchRepository;

}

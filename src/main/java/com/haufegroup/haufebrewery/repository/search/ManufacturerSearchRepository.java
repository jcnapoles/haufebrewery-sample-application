package com.haufegroup.haufebrewery.repository.search;

import com.haufegroup.haufebrewery.domain.Manufacturer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Manufacturer} entity.
 */
public interface ManufacturerSearchRepository extends ElasticsearchRepository<Manufacturer, Long> {
}

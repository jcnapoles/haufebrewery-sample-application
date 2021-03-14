package com.haufegroup.haufebrewery.repository.search;

import com.haufegroup.haufebrewery.domain.Beer;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


/**
 * Spring Data Elasticsearch repository for the {@link Beer} entity.
 */
public interface BeerSearchRepository extends ElasticsearchRepository<Beer, Long> {
}

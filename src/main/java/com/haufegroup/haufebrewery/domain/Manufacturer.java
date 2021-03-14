package com.haufegroup.haufebrewery.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A Manufacturer.
 */
@Entity
@Table(name = "manufacturer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "manufacturer")
public class Manufacturer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

    @OneToOne
    @JoinColumn(unique = true)
    private User internalUser;

    @OneToMany(mappedBy = "manufaturer")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Beer> beers = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = "manufacturers", allowSetters = true)
    private Country country;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public Manufacturer manufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
        return this;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public User getInternalUser() {
        return internalUser;
    }

    public Manufacturer internalUser(User user) {
        this.internalUser = user;
        return this;
    }

    public void setInternalUser(User user) {
        this.internalUser = user;
    }

    public Set<Beer> getBeers() {
        return beers;
    }

    public Manufacturer beers(Set<Beer> beers) {
        this.beers = beers;
        return this;
    }

    public Manufacturer addBeer(Beer beer) {
        this.beers.add(beer);
        beer.setManufaturer(this);
        return this;
    }

    public Manufacturer removeBeer(Beer beer) {
        this.beers.remove(beer);
        beer.setManufaturer(null);
        return this;
    }

    public void setBeers(Set<Beer> beers) {
        this.beers = beers;
    }

    public Country getCountry() {
        return country;
    }

    public Manufacturer country(Country country) {
        this.country = country;
        return this;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Manufacturer)) {
            return false;
        }
        return id != null && id.equals(((Manufacturer) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Manufacturer{" +
            "id=" + getId() +
            ", manufacturerName='" + getManufacturerName() + "'" +
            "}";
    }
}

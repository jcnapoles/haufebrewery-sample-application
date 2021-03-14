import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate, ICrudGetAction, ICrudGetAllAction, setFileData, openFile, byteSize, ICrudPutAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IManufacturer } from 'app/shared/model/manufacturer.model';
import { getEntities as getManufacturers } from 'app/entities/manufacturer/manufacturer.reducer';
import { getEntity, updateEntity, createEntity, setBlob, reset } from './beer.reducer';
import { IBeer } from 'app/shared/model/beer.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IBeerUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BeerUpdate = (props: IBeerUpdateProps) => {
  const [manufaturerId, setManufaturerId] = useState('0');
  const [isNew, setIsNew] = useState(!props.match.params || !props.match.params.id);

  const { beerEntity, manufacturers, loading, updating } = props;

  const { image, imageContentType } = beerEntity;

  const handleClose = () => {
    props.history.push('/beer' + props.location.search);
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getManufacturers();
  }, []);

  const onBlobChange = (isAnImage, name) => event => {
    setFileData(event, (contentType, data) => props.setBlob(name, data, contentType), isAnImage);
  };

  const clearBlob = name => () => {
    props.setBlob(name, undefined, undefined);
  };

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...beerEntity,
        ...values,
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="haufebreweryApp.beer.home.createOrEditLabel">
            <Translate contentKey="haufebreweryApp.beer.home.createOrEditLabel">Create or edit a Beer</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : beerEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="beer-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="beer-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="beerNameLabel" for="beer-beerName">
                  <Translate contentKey="haufebreweryApp.beer.beerName">Beer Name</Translate>
                </Label>
                <AvField
                  id="beer-beerName"
                  type="text"
                  name="beerName"
                  validate={{
                    required: { value: true, errorMessage: translate('entity.validation.required') },
                  }}
                />
              </AvGroup>
              <AvGroup>
                <Label id="graduationLabel" for="beer-graduation">
                  <Translate contentKey="haufebreweryApp.beer.graduation">Graduation</Translate>
                </Label>
                <AvField id="beer-graduation" type="string" className="form-control" name="graduation" />
              </AvGroup>
              <AvGroup>
                <Label id="typeLabel" for="beer-type">
                  <Translate contentKey="haufebreweryApp.beer.type">Type</Translate>
                </Label>
                <AvField id="beer-type" type="text" name="type" />
              </AvGroup>
              <AvGroup>
                <Label id="descriptionLabel" for="beer-description">
                  <Translate contentKey="haufebreweryApp.beer.description">Description</Translate>
                </Label>
                <AvField id="beer-description" type="text" name="description" />
              </AvGroup>
              <AvGroup>
                <AvGroup>
                  <Label id="imageLabel" for="image">
                    <Translate contentKey="haufebreweryApp.beer.image">Image</Translate>
                  </Label>
                  <br />
                  {image ? (
                    <div>
                      {imageContentType ? (
                        <a onClick={openFile(imageContentType, image)}>
                          <Translate contentKey="entity.action.open">Open</Translate>
                        </a>
                      ) : null}
                      <br />
                      <Row>
                        <Col md="11">
                          <span>
                            {imageContentType}, {byteSize(image)}
                          </span>
                        </Col>
                        <Col md="1">
                          <Button color="danger" onClick={clearBlob('image')}>
                            <FontAwesomeIcon icon="times-circle" />
                          </Button>
                        </Col>
                      </Row>
                    </div>
                  ) : null}
                  <input id="file_image" type="file" onChange={onBlobChange(false, 'image')} />
                  <AvInput type="hidden" name="image" value={image} />
                </AvGroup>
              </AvGroup>
              <AvGroup>
                <Label for="beer-manufaturer">
                  <Translate contentKey="haufebreweryApp.beer.manufaturer">Manufaturer</Translate>
                </Label>
                <AvInput id="beer-manufaturer" type="select" className="form-control" name="manufaturer.id">
                  <option value="" key="0" />
                  {manufacturers
                    ? manufacturers.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.id}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/beer" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  manufacturers: storeState.manufacturer.entities,
  beerEntity: storeState.beer.entity,
  loading: storeState.beer.loading,
  updating: storeState.beer.updating,
  updateSuccess: storeState.beer.updateSuccess,
});

const mapDispatchToProps = {
  getManufacturers,
  getEntity,
  updateEntity,
  setBlob,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BeerUpdate);

import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, ICrudGetAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './manufacturer.reducer';
import { IManufacturer } from 'app/shared/model/manufacturer.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IManufacturerDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const ManufacturerDetail = (props: IManufacturerDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { manufacturerEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2>
          <Translate contentKey="haufebreweryApp.manufacturer.detail.title">Manufacturer</Translate> [<b>{manufacturerEntity.id}</b>]
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="manufacturerName">
              <Translate contentKey="haufebreweryApp.manufacturer.manufacturerName">Manufacturer Name</Translate>
            </span>
          </dt>
          <dd>{manufacturerEntity.manufacturerName}</dd>
          <dt>
            <Translate contentKey="haufebreweryApp.manufacturer.internalUser">Internal User</Translate>
          </dt>
          <dd>{manufacturerEntity.internalUser ? manufacturerEntity.internalUser.id : ''}</dd>
          <dt>
            <Translate contentKey="haufebreweryApp.manufacturer.country">Country</Translate>
          </dt>
          <dd>{manufacturerEntity.country ? manufacturerEntity.country.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/manufacturer" replace color="info">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/manufacturer/${manufacturerEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ manufacturer }: IRootState) => ({
  manufacturerEntity: manufacturer.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(ManufacturerDetail);

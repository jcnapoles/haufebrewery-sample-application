import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, ICrudGetAction, openFile, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './beer.reducer';
import { IBeer } from 'app/shared/model/beer.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IBeerDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BeerDetail = (props: IBeerDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { beerEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2>
          <Translate contentKey="haufebreweryApp.beer.detail.title">Beer</Translate> [<b>{beerEntity.id}</b>]
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="beerName">
              <Translate contentKey="haufebreweryApp.beer.beerName">Beer Name</Translate>
            </span>
          </dt>
          <dd>{beerEntity.beerName}</dd>
          <dt>
            <span id="graduation">
              <Translate contentKey="haufebreweryApp.beer.graduation">Graduation</Translate>
            </span>
          </dt>
          <dd>{beerEntity.graduation}</dd>
          <dt>
            <span id="type">
              <Translate contentKey="haufebreweryApp.beer.type">Type</Translate>
            </span>
          </dt>
          <dd>{beerEntity.type}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="haufebreweryApp.beer.description">Description</Translate>
            </span>
          </dt>
          <dd>{beerEntity.description}</dd>
          <dt>
            <span id="image">
              <Translate contentKey="haufebreweryApp.beer.image">Image</Translate>
            </span>
          </dt>
          <dd>
            {beerEntity.image ? (
              <div>
                {beerEntity.imageContentType ? (
                  <a onClick={openFile(beerEntity.imageContentType, beerEntity.image)}>
                    <Translate contentKey="entity.action.open">Open</Translate>&nbsp;
                  </a>
                ) : null}
                <span>
                  {beerEntity.imageContentType}, {byteSize(beerEntity.image)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <Translate contentKey="haufebreweryApp.beer.manufaturer">Manufaturer</Translate>
          </dt>
          <dd>{beerEntity.manufaturer ? beerEntity.manufaturer.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/beer" replace color="info">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/beer/${beerEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ beer }: IRootState) => ({
  beerEntity: beer.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BeerDetail);

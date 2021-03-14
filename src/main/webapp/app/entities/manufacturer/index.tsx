import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Manufacturer from './manufacturer';
import ManufacturerDetail from './manufacturer-detail';
import ManufacturerUpdate from './manufacturer-update';
import ManufacturerDeleteDialog from './manufacturer-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={ManufacturerUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={ManufacturerUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={ManufacturerDetail} />
      <ErrorBoundaryRoute path={match.url} component={Manufacturer} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={ManufacturerDeleteDialog} />
  </>
);

export default Routes;

import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Beer from './beer';
import BeerDetail from './beer-detail';
import BeerUpdate from './beer-update';
import BeerDeleteDialog from './beer-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={BeerUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={BeerUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={BeerDetail} />
      <ErrorBoundaryRoute path={match.url} component={Beer} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={BeerDeleteDialog} />
  </>
);

export default Routes;

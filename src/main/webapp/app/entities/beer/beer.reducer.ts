import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IBeer, defaultValue } from 'app/shared/model/beer.model';

export const ACTION_TYPES = {
  SEARCH_BEERS: 'beer/SEARCH_BEERS',
  FETCH_BEER_LIST: 'beer/FETCH_BEER_LIST',
  FETCH_BEER: 'beer/FETCH_BEER',
  CREATE_BEER: 'beer/CREATE_BEER',
  UPDATE_BEER: 'beer/UPDATE_BEER',
  DELETE_BEER: 'beer/DELETE_BEER',
  SET_BLOB: 'beer/SET_BLOB',
  RESET: 'beer/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IBeer>,
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

export type BeerState = Readonly<typeof initialState>;

// Reducer

export default (state: BeerState = initialState, action): BeerState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_BEERS):
    case REQUEST(ACTION_TYPES.FETCH_BEER_LIST):
    case REQUEST(ACTION_TYPES.FETCH_BEER):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_BEER):
    case REQUEST(ACTION_TYPES.UPDATE_BEER):
    case REQUEST(ACTION_TYPES.DELETE_BEER):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.SEARCH_BEERS):
    case FAILURE(ACTION_TYPES.FETCH_BEER_LIST):
    case FAILURE(ACTION_TYPES.FETCH_BEER):
    case FAILURE(ACTION_TYPES.CREATE_BEER):
    case FAILURE(ACTION_TYPES.UPDATE_BEER):
    case FAILURE(ACTION_TYPES.DELETE_BEER):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.SEARCH_BEERS):
    case SUCCESS(ACTION_TYPES.FETCH_BEER_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
        totalItems: parseInt(action.payload.headers['x-total-count'], 10),
      };
    case SUCCESS(ACTION_TYPES.FETCH_BEER):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_BEER):
    case SUCCESS(ACTION_TYPES.UPDATE_BEER):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_BEER):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.SET_BLOB: {
      const { name, data, contentType } = action.payload;
      return {
        ...state,
        entity: {
          ...state.entity,
          [name]: data,
          [name + 'ContentType']: contentType,
        },
      };
    }
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/beers';
const apiSearchUrl = 'api/_search/beers';

// Actions

export const getSearchEntities: ICrudSearchAction<IBeer> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_BEERS,
  payload: axios.get<IBeer>(`${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`),
});

export const getEntities: ICrudGetAllAction<IBeer> = (page, size, sort) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return {
    type: ACTION_TYPES.FETCH_BEER_LIST,
    payload: axios.get<IBeer>(`${requestUrl}${sort ? '&' : '?'}cacheBuster=${new Date().getTime()}`),
  };
};

export const getEntity: ICrudGetAction<IBeer> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_BEER,
    payload: axios.get<IBeer>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IBeer> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_BEER,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IBeer> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_BEER,
    payload: axios.put(apiUrl, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IBeer> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_BEER,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const setBlob = (name, data, contentType?) => ({
  type: ACTION_TYPES.SET_BLOB,
  payload: {
    name,
    data,
    contentType,
  },
});

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});

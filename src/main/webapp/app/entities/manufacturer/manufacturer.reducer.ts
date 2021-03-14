import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IManufacturer, defaultValue } from 'app/shared/model/manufacturer.model';

export const ACTION_TYPES = {
  SEARCH_MANUFACTURERS: 'manufacturer/SEARCH_MANUFACTURERS',
  FETCH_MANUFACTURER_LIST: 'manufacturer/FETCH_MANUFACTURER_LIST',
  FETCH_MANUFACTURER: 'manufacturer/FETCH_MANUFACTURER',
  CREATE_MANUFACTURER: 'manufacturer/CREATE_MANUFACTURER',
  UPDATE_MANUFACTURER: 'manufacturer/UPDATE_MANUFACTURER',
  DELETE_MANUFACTURER: 'manufacturer/DELETE_MANUFACTURER',
  RESET: 'manufacturer/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IManufacturer>,
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
};

export type ManufacturerState = Readonly<typeof initialState>;

// Reducer

export default (state: ManufacturerState = initialState, action): ManufacturerState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_MANUFACTURERS):
    case REQUEST(ACTION_TYPES.FETCH_MANUFACTURER_LIST):
    case REQUEST(ACTION_TYPES.FETCH_MANUFACTURER):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_MANUFACTURER):
    case REQUEST(ACTION_TYPES.UPDATE_MANUFACTURER):
    case REQUEST(ACTION_TYPES.DELETE_MANUFACTURER):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.SEARCH_MANUFACTURERS):
    case FAILURE(ACTION_TYPES.FETCH_MANUFACTURER_LIST):
    case FAILURE(ACTION_TYPES.FETCH_MANUFACTURER):
    case FAILURE(ACTION_TYPES.CREATE_MANUFACTURER):
    case FAILURE(ACTION_TYPES.UPDATE_MANUFACTURER):
    case FAILURE(ACTION_TYPES.DELETE_MANUFACTURER):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.SEARCH_MANUFACTURERS):
    case SUCCESS(ACTION_TYPES.FETCH_MANUFACTURER_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
        totalItems: parseInt(action.payload.headers['x-total-count'], 10),
      };
    case SUCCESS(ACTION_TYPES.FETCH_MANUFACTURER):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_MANUFACTURER):
    case SUCCESS(ACTION_TYPES.UPDATE_MANUFACTURER):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_MANUFACTURER):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/manufacturers';
const apiSearchUrl = 'api/_search/manufacturers';

// Actions

export const getSearchEntities: ICrudSearchAction<IManufacturer> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_MANUFACTURERS,
  payload: axios.get<IManufacturer>(`${apiSearchUrl}?query=${query}${sort ? `&page=${page}&size=${size}&sort=${sort}` : ''}`),
});

export const getEntities: ICrudGetAllAction<IManufacturer> = (page, size, sort) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return {
    type: ACTION_TYPES.FETCH_MANUFACTURER_LIST,
    payload: axios.get<IManufacturer>(`${requestUrl}${sort ? '&' : '?'}cacheBuster=${new Date().getTime()}`),
  };
};

export const getEntity: ICrudGetAction<IManufacturer> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_MANUFACTURER,
    payload: axios.get<IManufacturer>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IManufacturer> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_MANUFACTURER,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IManufacturer> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_MANUFACTURER,
    payload: axios.put(apiUrl, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IManufacturer> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_MANUFACTURER,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});

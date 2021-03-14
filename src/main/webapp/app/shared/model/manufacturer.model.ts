import { IUser } from 'app/shared/model/user.model';
import { IBeer } from 'app/shared/model/beer.model';
import { ICountry } from 'app/shared/model/country.model';

export interface IManufacturer {
  id?: number;
  manufacturerName?: string;
  internalUser?: IUser;
  beers?: IBeer[];
  country?: ICountry;
}

export const defaultValue: Readonly<IManufacturer> = {};

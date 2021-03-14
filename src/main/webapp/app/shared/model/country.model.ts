import { IManufacturer } from 'app/shared/model/manufacturer.model';

export interface ICountry {
  id?: number;
  countryName?: string;
  countryCode?: string;
  region?: string;
  manufacturers?: IManufacturer[];
}

export const defaultValue: Readonly<ICountry> = {};

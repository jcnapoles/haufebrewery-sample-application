import { IManufacturer } from 'app/shared/model/manufacturer.model';

export interface IBeer {
  id?: number;
  beerName?: string;
  graduation?: number;
  type?: string;
  description?: string;
  imageContentType?: string;
  image?: any;
  manufaturer?: IManufacturer;
}

export const defaultValue: Readonly<IBeer> = {};

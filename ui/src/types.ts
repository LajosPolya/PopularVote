export interface Citizen {
    id: number;
    givenName: string;
    surname: string;
    middleName: string | null;
    politicalAffiliationId: number | null;
    role: string;
    postalCodeId: number | null;
    postalCode: PostalCode | null;
}

export interface CitizenProfile extends Citizen {
    citizenPoliticalDetailsId: number | null;
    levelOfPoliticsName: string | null;
    electoralDistrictName: string | null;
    policyCount: number;
    voteCount: number;
}

export interface CitizenSelf extends CitizenProfile {
    isVerificationPending: boolean;
}

export interface LevelOfPolitics {
    id: number;
    name: string;
    description: string | null;
}

export interface DeclarePolitician {
    levelOfPoliticsId: number;
    electoralDistrictId: number;
    politicalAffiliationId: number;
}

export interface Policy {
    id: number;
    description: string;
    publisherCitizenId: number;
    levelOfPoliticsId: number;
    publisherName: string;
    isBookmarked: boolean;
    closeDate: string;
    creationDate: string;
    publisherPoliticalPartyId: number | null;
}

export interface OpinionDetails {
    id: number;
    description: string;
    authorId: number;
    authorName: string;
    authorPoliticalAffiliationId: number | null;
    policyId: number;
}

export interface PolicyDetails extends Policy {
    publisherPoliticalAffiliationId: number | null;
    coAuthorCitizens: Citizen[];
    opinions: OpinionDetails[];
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    pageNumber: number;
    pageSize: number;
}

export interface PoliticalParty {
    id: number;
    displayName: string;
    hexColor: string;
    description: string | null;
    levelOfPoliticsId: number;
    provinceAndTerritoryId: number | null;
}

export interface OpinionLikeCount {
    opinionId: number;
    likeCount: number;
}

export interface ProvinceAndTerritory {
    id: number;
    name: string;
    municipalities: Municipality[];
    electoralDistricts: ElectoralDistrict[];
}

export interface Municipality {
    id: number;
    name: string;
    provinceTerritoryId: number;
    postalCodes: PostalCode[];
}

export interface ElectoralDistrict {
    id: number;
    name: string;
    code: number;
    provinceTerritoryId: number;
}

export interface PostalCode {
    id: number;
    name: string;
    code: number;
    municipalityId: number;
    electoralDistrictId: number;
    electoralDistrict: ElectoralDistrict | null;
}

export interface GeoData {
    provincesAndTerritories: ProvinceAndTerritory[];
}

export const getFullName = (citizen: { givenName: string; surname: string } | Citizen): string => {
    return `${citizen.givenName} ${citizen.surname}`;
};

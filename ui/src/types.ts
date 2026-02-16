export interface Citizen {
    id: number;
    givenName: string;
    surname: string;
    middleName: string | null;
    politicalAffiliationId: number | null;
    role: string;
}

export interface CitizenProfile extends Citizen {
    citizenPoliticalDetailsId: number | null;
    levelOfPoliticsName: string | null;
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
    geographicLocation: string | null;
    politicalAffiliation: string;
}

export interface Policy {
    id: number;
    description: string;
    publisherCitizenId: number;
    levelOfPoliticsId: number;
    citizenPoliticalDetailsId: number;
    publisherName: string;
    isBookmarked: boolean;
    closeDate: string;
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

export interface PoliticalParty {
    id: number;
    displayName: string;
    hexColor: string;
    description: string | null;
    levelOfPoliticsId: number;
}

export interface OpinionLikeCount {
    opinionId: number;
    likeCount: number;
}

export const getFullName = (citizen: { givenName: string; surname: string } | Citizen): string => {
    return `${citizen.givenName} ${citizen.surname}`;
};

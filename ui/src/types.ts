export interface Citizen {
    id: number;
    givenName: string;
    surname: string;
    middleName: string | null;
    politicalAffiliation: string;
    role: string;
}

export interface CitizenProfile extends Citizen {
    policyCount: number;
    voteCount: number;
}

export interface CitizenSelf extends CitizenProfile {
    isVerificationPending: boolean;
}

export interface Policy {
    id: number;
    description: string;
    publisherCitizenId: number;
    publisherName: string;
    isBookmarked: boolean;
}

export interface OpinionDetails {
    id: number;
    description: string;
    authorId: number;
    authorName: string;
    authorPoliticalAffiliation: string;
    policyId: number;
}

export interface PolicyDetails extends Policy {
    publisherCitizenId: number;
    publisherName: string;
    publisherPoliticalAffiliation: string;
    coAuthorCitizens: Citizen[];
    opinions: OpinionDetails[];
}

export interface PoliticalParty {
    id: number;
    displayName: string;
    hexColor: string;
    description: string | null;
}

export const getFullName = (citizen: { givenName: string; surname: string } | Citizen): string => {
    return `${citizen.givenName} ${citizen.surname}`;
};

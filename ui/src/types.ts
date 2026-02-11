export interface Citizen {
    id: number;
    givenName: string;
    surname: string;
    middleName: string | null;
    politicalAffiliation: string;
    role: string;
}

export interface CitizenSelf extends Citizen {
    policyCount: number;
    voteCount: number;
    isVerificationPending: boolean;
}

export interface Policy {
    id: number;
    description: string;
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
    opinions: OpinionDetails[];
}

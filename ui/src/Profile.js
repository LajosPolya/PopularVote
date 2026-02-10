import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { affiliations, affiliationColors } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Profile({ citizenId, onBack }) {
    const { getAccessTokenSilently } = useAuth0();
    const [citizen, setCitizen] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [declaring, setDeclaring] = useState(false);

    const fetchCitizen = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const endpoint = citizenId ? `${popularVoteApiUrl}/citizens/${citizenId}` : `${popularVoteApiUrl}/citizens/self`;
            const response = await fetch(endpoint, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch citizen profile');
            }
            const data = await response.json();
            setCitizen(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCitizen();
    }, [getAccessTokenSilently, citizenId]);

    const handleDeclarePolitician = async () => {
        setDeclaring(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/self/declare-politician`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.status !== 202) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to declare as politician');
            }

            // Refresh profile data or show success message
            // Since it's now just a declaration, the role won't change immediately in the UI until verified
            alert("Your declaration has been submitted for verification.");
            await fetchCitizen();
        } catch (err) {
            setError(err.message);
        } finally {
            setDeclaring(false);
        }
    };

    if (loading) return <div style={{ padding: '20px' }}>Loading profile...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>Error: {error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto', textAlign: 'left' }}>
            <button onClick={onBack} style={{ marginBottom: '20px' }}>&larr; Back</button>
            <h2>{citizenId ? 'Citizen Profile' : 'Your Profile'}</h2>
            <div style={{ backgroundColor: '#f9f9f9', padding: '20px', borderRadius: '8px', border: '1px solid #ddd' }}>
                <p><strong>Given Name:</strong> {citizen.givenName}</p>
                <p><strong>Middle Name:</strong> {citizen.middleName || 'N/A'}</p>
                <p><strong>Surname:</strong> {citizen.surname}</p>
                <p><strong>Role:</strong> {citizen.role.charAt(0) + citizen.role.slice(1).toLowerCase()}</p>
                <p>
                    <strong>Political Affiliation:</strong>{' '}
                    <span>
                        {affiliations[citizen.politicalAffiliation] || citizen.politicalAffiliation}
                    </span>
                    <span style={{ 
                        display: 'inline-block',
                        width: '12px',
                        height: '12px',
                        backgroundColor: affiliationColors[citizen.politicalAffiliation] || 'grey',
                        marginLeft: '8px',
                        borderRadius: '2px'
                    }}></span>
                </p>
                <hr style={{ margin: '20px 0', border: '0', borderTop: '1px solid #eee' }} />
                <p><strong>Policies Created:</strong> {citizen.policyCount}</p>
                <p><strong>Votes Cast:</strong> {citizen.voteCount}</p>

                {!citizenId && citizen.role === 'CITIZEN' && (
                    <div style={{ marginTop: '20px' }}>
                        <button 
                            onClick={handleDeclarePolitician} 
                            disabled={declaring || citizen.isVerificationPending}
                            style={{ 
                                padding: '10px 20px', 
                                backgroundColor: (declaring || citizen.isVerificationPending) ? '#6c757d' : '#28a745', 
                                color: 'white', 
                                border: 'none', 
                                borderRadius: '4px',
                                cursor: (declaring || citizen.isVerificationPending) ? 'not-allowed' : 'pointer'
                            }}
                        >
                            {declaring ? 'Declaring...' : citizen.isVerificationPending ? 'Verification Pending' : 'Get Verified as a Politician'}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Profile;

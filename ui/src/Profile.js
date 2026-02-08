import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { affiliations, affiliationColors } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Profile({ onBack }) {
    const { getAccessTokenSilently } = useAuth0();
    const [citizen, setCitizen] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);


    useEffect(() => {
        const fetchCitizen = async () => {
            setLoading(true);
            try {
                const token = await getAccessTokenSilently();
                const response = await fetch(`${popularVoteApiUrl}/citizens/self`, {
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

        fetchCitizen();
    }, [getAccessTokenSilently]);

    if (loading) return <div style={{ padding: '20px' }}>Loading profile...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>Error: {error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '600px', margin: '0 auto', textAlign: 'left' }}>
            <button onClick={onBack} style={{ marginBottom: '20px' }}>&larr; Back to Policies</button>
            <h2>User Profile</h2>
            <div style={{ backgroundColor: '#f9f9f9', padding: '20px', borderRadius: '8px', border: '1px solid #ddd' }}>
                <p><strong>Given Name:</strong> {citizen.givenName}</p>
                <p><strong>Middle Name:</strong> {citizen.middleName || 'N/A'}</p>
                <p><strong>Surname:</strong> {citizen.surname}</p>
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
            </div>
        </div>
    );
}

export default Profile;

import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Citizens() {
    const { getAccessTokenSilently } = useAuth0();
    const [citizens, setCitizens] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const affiliations = {
        'LIBERAL_PARTY_OF_CANADA': 'Liberal Party of Canada',
        'CONSERVATIVE_PARTY_OF_CANADA': 'Conservative Party of Canada',
        'BLOC_QUEBECOIS': 'Bloc Québécois',
        'NEW_DEMOCRATIC_PARTY': 'New Democratic Party',
        'GREEN_PARTY_OF_CANADA': 'Green Party of Canada',
        'INDEPENDENT': 'Independent',
    };

    const affiliationColors = {
        'LIBERAL_PARTY_OF_CANADA': 'red',
        'CONSERVATIVE_PARTY_OF_CANADA': 'blue',
        'BLOC_QUEBECOIS': 'darkblue',
        'NEW_DEMOCRATIC_PARTY': 'orange',
        'GREEN_PARTY_OF_CANADA': 'green',
        'INDEPENDENT': 'black',
    };

    const fetchCitizens = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch citizens');
            }
            const data = await response.json();
            setCitizens(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCitizens();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div style={{ padding: '20px' }}>
            <h2>Citizens</h2>

            {error && <p style={{ color: 'red' }}>Error: {error}</p>}

            {loading && citizens.length === 0 ? (
                <p>Loading citizens...</p>
            ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {citizens.map((citizen) => (
                        <li key={citizen.id} 
                            style={{ 
                                padding: '10px', 
                                border: '1px solid #eee', 
                                marginBottom: '5px', 
                                textAlign: 'left',
                                borderRadius: '4px'
                            }}
                        >
                            <strong>Name:</strong> {citizen.givenName} {citizen.surname}
                            <span style={{ marginLeft: '12px', fontSize: '0.9em', color: '#666' }}>
                                ({affiliations[citizen.politicalAffiliation] || citizen.politicalAffiliation})
                            </span>
                            <span style={{ 
                                display: 'inline-block',
                                width: '12px',
                                height: '12px',
                                backgroundColor: affiliationColors[citizen.politicalAffiliation] || 'grey',
                                marginLeft: '8px',
                                borderRadius: '2px',
                                verticalAlign: 'middle'
                            }}></span>
                        </li>
                    ))}
                </ul>
            )}
            
            {citizens.length === 0 && !loading && <p>No citizens found.</p>}
        </div>
    );
}

export default Citizens;

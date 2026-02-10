import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { affiliations, affiliationColors } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Citizens({ onCitizenClick }) {
    const { getAccessTokenSilently } = useAuth0();
    const [citizens, setCitizens] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);


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
                            onClick={() => onCitizenClick && onCitizenClick(citizen.id)}
                            style={{ 
                                padding: '10px', 
                                border: '1px solid #eee', 
                                marginBottom: '5px', 
                                textAlign: 'left',
                                borderRadius: '4px',
                                cursor: 'pointer'
                            }}
                            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                            <strong>Name:</strong> {citizen.givenName} {citizen.surname}
                            <span style={{ marginLeft: '12px', fontSize: '0.9em', color: '#666' }}>
                                [{citizen.role.charAt(0) + citizen.role.slice(1).toLowerCase()}]
                            </span>
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

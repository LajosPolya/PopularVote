import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { affiliations, affiliationColors } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function VerifyPoliticians({ onCitizenClick }) {
    const { getAccessTokenSilently } = useAuth0();
    const [verifications, setVerifications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [verifyingId, setVerifyingId] = useState(null);

    const fetchVerifications = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/verify-politician`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch politician verifications');
            }
            const data = await response.json();
            setVerifications(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchVerifications();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleVerify = async (e, id) => {
        e.stopPropagation();
        setVerifyingId(id);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/${id}/verify-politician`, {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to verify politician');
            }

            alert("Politician verified successfully!");
            fetchVerifications();
        } catch (err) {
            setError(err.message);
        } finally {
            setVerifyingId(null);
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Verify Politicians</h2>

            {error && <p style={{ color: 'red' }}>Error: {error}</p>}

            {loading && verifications.length === 0 ? (
                <p>Loading verifications...</p>
            ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {verifications.map((citizen) => (
                        <li key={citizen.id} 
                            onClick={() => onCitizenClick && onCitizenClick(citizen.id)}
                            style={{ 
                                padding: '15px', 
                                border: '1px solid #eee', 
                                marginBottom: '10px', 
                                textAlign: 'left',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center'
                            }}
                            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                            <div>
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
                            </div>
                            <button 
                                onClick={(e) => handleVerify(e, citizen.id)}
                                disabled={verifyingId === citizen.id}
                                style={{
                                    padding: '8px 16px',
                                    backgroundColor: verifyingId === citizen.id ? '#6c757d' : '#28a745',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '4px',
                                    cursor: verifyingId === citizen.id ? 'not-allowed' : 'pointer'
                                }}
                            >
                                {verifyingId === citizen.id ? 'Verifying...' : 'Verify'}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
            
            {verifications.length === 0 && !loading && <p>No pending verifications found.</p>}
        </div>
    );
}

export default VerifyPoliticians;

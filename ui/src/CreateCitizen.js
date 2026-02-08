import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { affiliations } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function CreateCitizen({ onCreateSuccess }) {
    const { getAccessTokenSilently } = useAuth0();
    const [givenName, setGivenName] = useState('');
    const [surname, setSurname] = useState('');
    const [middleName, setMiddleName] = useState('');
    const [politicalAffiliation, setPoliticalAffiliation] = useState('INDEPENDENT');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);


    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!givenName.trim() || !surname.trim()) {
            setError('Given Name and Surname are required');
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    givenName,
                    surname,
                    middleName: middleName || null,
                    politicalAffiliation,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to create citizen');
            }

            onCreateSuccess();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '500px', margin: '0 auto' }}>
            <h2>Complete Your Profile</h2>
            <p>It looks like you're new here. Please provide some details to continue.</p>
            
            <form onSubmit={handleSubmit} style={{ textAlign: 'left' }}>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="givenName" style={{ display: 'block', marginBottom: '5px' }}>Given Name:</label>
                    <input
                        id="givenName"
                        type="text"
                        value={givenName}
                        onChange={(e) => setGivenName(e.target.value)}
                        required
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="middleName" style={{ display: 'block', marginBottom: '5px' }}>Middle Name (Optional):</label>
                    <input
                        id="middleName"
                        type="text"
                        value={middleName}
                        onChange={(e) => setMiddleName(e.target.value)}
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="surname" style={{ display: 'block', marginBottom: '5px' }}>Surname:</label>
                    <input
                        id="surname"
                        type="text"
                        value={surname}
                        onChange={(e) => setSurname(e.target.value)}
                        required
                        style={{ width: '100%', padding: '8px' }}
                    />
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label htmlFor="politicalAffiliation" style={{ display: 'block', marginBottom: '5px' }}>Political Affiliation:</label>
                    <select
                        id="politicalAffiliation"
                        value={politicalAffiliation}
                        onChange={(e) => setPoliticalAffiliation(e.target.value)}
                        style={{ width: '100%', padding: '8px' }}
                    >
                        {Object.entries(affiliations).map(([value, label]) => (
                            <option key={value} value={value}>
                                {label}
                            </option>
                        ))}
                    </select>
                </div>
                
                {error && <p style={{ color: 'red' }}>{error}</p>}
                
                <button 
                    type="submit" 
                    disabled={loading}
                    style={{ 
                        padding: '10px 20px', 
                        backgroundColor: '#007bff', 
                        color: 'white', 
                        border: 'none', 
                        borderRadius: '4px',
                        cursor: loading ? 'not-allowed' : 'pointer'
                    }}
                >
                    {loading ? 'Saving...' : 'Complete Registration'}
                </button>
            </form>
        </div>
    );
}

export default CreateCitizen;

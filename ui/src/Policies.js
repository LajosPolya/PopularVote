import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function Policies({ onPolicyClick }) {
    const { getAccessTokenSilently } = useAuth0();
    const [policies, setPolicies] = useState([]);
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchPolicies = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch('/policies', {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch policies');
            }
            const data = await response.json();
            setPolicies(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPolicies();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!description.trim()) return;

        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch('/policies', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ description }),
            });

            if (!response.ok) {
                throw new Error('Failed to create policy');
            }

            setDescription('');
            fetchPolicies();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Policies</h2>
            
            <form onSubmit={handleSubmit} style={{ marginBottom: '20px' }}>
                <div>
                    <label htmlFor="description">Policy Description: </label>
                    <input
                        id="description"
                        type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Enter policy description"
                        style={{ width: '300px', marginRight: '10px' }}
                    />
                    <button type="submit" disabled={loading || !description.trim()}>
                        {loading ? 'Creating...' : 'Create Policy'}
                    </button>
                </div>
            </form>

            {error && <p style={{ color: 'red' }}>Error: {error}</p>}

            {loading && policies.length === 0 ? (
                <p>Loading policies...</p>
            ) : (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {policies.map((policy) => (
                        <li key={policy.id} 
                            onClick={() => onPolicyClick && onPolicyClick(policy.id)}
                            style={{ 
                                padding: '10px', 
                                border: '1px solid #eee', 
                                marginBottom: '5px', 
                                cursor: 'pointer',
                                textAlign: 'left',
                                borderRadius: '4px'
                            }}
                            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f0f0f0'}
                            onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                            <strong>Description:</strong> {policy.description}
                        </li>
                    ))}
                </ul>
            )}
            
            {policies.length === 0 && !loading && <p>No policies found.</p>}
        </div>
    );
}

export default Policies;

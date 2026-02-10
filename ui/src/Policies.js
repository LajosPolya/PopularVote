import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Policies({ onPolicyClick, onCreatePolicy }) {
    const { getAccessTokenSilently } = useAuth0();
    const [policies, setPolicies] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [canCreatePolicy, setCanCreatePolicy] = useState(false);

    const checkPermissions = async () => {
        try {
            const token = await getAccessTokenSilently();
            const payload = JSON.parse(atob(token.split('.')[1]));
            // Permissions are in 'scope' claim by default in Auth0 (unless RBAC is enabled)
            const permissions = payload.scope?.split(' ') || [];
            setCanCreatePolicy(permissions.includes('write:policies'));
        } catch (err) {
            console.error("Error checking permissions:", err);
            setCanCreatePolicy(false);
        }
    };

    const fetchPolicies = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/policies`, {
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
        checkPermissions();
        fetchPolicies();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <div style={{ padding: '20px' }}>
            <h2>Policies</h2>
            
            {canCreatePolicy && (
                <div style={{ marginBottom: '20px' }}>
                    <button onClick={onCreatePolicy}>
                        Create Policy
                    </button>
                </div>
            )}

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

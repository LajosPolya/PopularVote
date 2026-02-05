import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function CreateOpinion({ initialPolicyId, onBack }) {
    const { user, getAccessTokenSilently } = useAuth0();
    const [selectedPolicyId, setSelectedPolicyId] = useState(initialPolicyId || '');
    const [policy, setPolicy] = useState(null);
    const [description, setDescription] = useState('');
    const [citizen, setCitizen] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                const token = await getAccessTokenSilently();
                const headers = {
                    Authorization: `Bearer ${token}`,
                };

                const requests = [];
                if (initialPolicyId) {
                    requests.push(fetch(`/policies/${initialPolicyId}`, { headers }));
                }
                requests.push(fetch('/citizens/self', { headers }));

                const responses = await Promise.all(requests);
                
                let policyData = null;
                let citizenData = null;

                if (initialPolicyId) {
                    const policyRes = responses[0];
                    const citizenRes = responses[1];
                    if (!policyRes.ok) throw new Error('Failed to fetch policy');
                    if (!citizenRes.ok) throw new Error('Failed to fetch citizen profile');
                    policyData = await policyRes.json();
                    citizenData = await citizenRes.json();
                } else {
                    const citizenRes = responses[0];
                    if (!citizenRes.ok) throw new Error('Failed to fetch citizen profile');
                    citizenData = await citizenRes.json();
                }

                if (policyData) {
                    setPolicy(policyData);
                    setSelectedPolicyId(initialPolicyId);
                }
                setCitizen(citizenData);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [initialPolicyId, getAccessTokenSilently]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedPolicyId || !description.trim() || !citizen) {
            setError('Please fill in all fields and ensure you have a citizen profile.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(false);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch('/opinions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    policyId: parseInt(selectedPolicyId),
                    description,
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to create opinion');
            }

            setSuccess(true);
            setDescription('');
            
            // Automatically go back to Policy Details after a short delay to show success message
            // or immediately if preferred. The requirement says "automatically go back".
            setTimeout(() => {
                onBack();
            }, 1500);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <div style={{ marginBottom: '20px' }}>
                <button onClick={onBack}>&larr; Back to Policy Details</button>
            </div>
            <h2>Create Opinion</h2>
            
            {error && <p style={{ color: 'red' }}>Error: {error}</p>}
            {success && <p style={{ color: 'green' }}>Opinion created successfully!</p>}

            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', maxWidth: '400px', gap: '10px' }}>
                <div>
                    <label>Policy: </label>
                    <span>{policy ? `${policy.description} (ID: ${policy.id})` : 'Loading...'}</span>
                </div>

                <div>
                    <label>Author: </label>
                    <span>{citizen ? `${citizen.givenName} ${citizen.surname} (ID: ${citizen.id})` : 'Loading citizen profile...'}</span>
                </div>

                <div>
                    <label htmlFor="description">Opinion: </label>
                    <textarea
                        id="description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Enter your opinion"
                        style={{ width: '100%', height: '80px' }}
                    />
                </div>

                <button type="submit" disabled={loading || !policy}>
                    {loading ? 'Creating...' : 'Create Opinion'}
                </button>
            </form>

            {loading && !policy && <p>Loading policy...</p>}
        </div>
    );
}

export default CreateOpinion;

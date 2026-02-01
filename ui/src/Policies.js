import React, { useState, useEffect } from 'react';

function Policies() {
    const [policies, setPolicies] = useState([]);
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const fetchPolicies = async () => {
        setLoading(true);
        try {
            const response = await fetch('/policies');
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
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!description.trim()) return;

        setLoading(true);
        try {
            const response = await fetch('/policies', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
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
                    <button type="submit" disabled={loading}>
                        {loading ? 'Creating...' : 'Create Policy'}
                    </button>
                </div>
            </form>

            {error && <p style={{ color: 'red' }}>Error: {error}</p>}

            {loading && policies.length === 0 ? (
                <p>Loading policies...</p>
            ) : (
                <ul>
                    {policies.map((policy) => (
                        <li key={policy.id}>
                            <strong>ID:</strong> {policy.id} | <strong>Description:</strong> {policy.description}
                        </li>
                    ))}
                </ul>
            )}
            
            {policies.length === 0 && !loading && <p>No policies found.</p>}
        </div>
    );
}

export default Policies;

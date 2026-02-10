import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function CreatePolicy({ onBack, onCreateSuccess }) {
    const { getAccessTokenSilently } = useAuth0();
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!description.trim()) return;

        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/policies`, {
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
            if (onCreateSuccess) {
                onCreateSuccess();
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px' }}>
            <button onClick={onBack} style={{ marginBottom: '20px' }}>Back to Policies</button>
            <h2>Create New Policy</h2>
            
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
        </div>
    );
}

export default CreatePolicy;

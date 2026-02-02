import React, { useState, useEffect } from 'react';

function CreateOpinion({ initialPolicyId, onBack }) {
    const [selectedPolicyId, setSelectedPolicyId] = useState(initialPolicyId || '');
    const [policy, setPolicy] = useState(null);
    const [description, setDescription] = useState('');
    const [author, setAuthor] = useState('');
    const [politicalSpectrum, setPoliticalSpectrum] = useState('CENTER');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        const fetchPolicy = async () => {
            if (!initialPolicyId) return;
            setLoading(true);
            try {
                const response = await fetch(`/policies/${initialPolicyId}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch policy');
                }
                const data = await response.json();
                setPolicy(data);
                setSelectedPolicyId(initialPolicyId);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchPolicy();
    }, [initialPolicyId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedPolicyId || !description.trim() || !author.trim()) {
            setError('Please fill in all fields.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(false);
        try {
            const response = await fetch('/opinions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    policyId: parseInt(selectedPolicyId),
                    description,
                    author,
                    politicalSpectrum
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to create opinion');
            }

            setSuccess(true);
            setDescription('');
            setAuthor('');
            setPoliticalSpectrum('CENTER');
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
                    <label htmlFor="author">Author: </label>
                    <input
                        id="author"
                        type="text"
                        value={author}
                        onChange={(e) => setAuthor(e.target.value)}
                        placeholder="Your name"
                        style={{ width: '100%' }}
                    />
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

                <div>
                    <label htmlFor="spectrum">Political Spectrum: </label>
                    <select 
                        id="spectrum" 
                        value={politicalSpectrum} 
                        onChange={(e) => setPoliticalSpectrum(e.target.value)}
                        style={{ width: '100%' }}
                    >
                        <option value="LEFT">Left</option>
                        <option value="CENTER">Center</option>
                        <option value="RIGHT">Right</option>
                    </select>
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

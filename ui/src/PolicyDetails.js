import React, { useState, useEffect } from 'react';

function PolicyDetails({ policyId, onBack }) {
    const [policy, setPolicy] = useState(null);
    const [opinions, setOpinions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchPolicyAndOpinions = async () => {
            setLoading(true);
            try {
                const [policyRes, opinionsRes] = await Promise.all([
                    fetch(`/policies/${policyId}`),
                    fetch(`/policies/${policyId}/opinions`)
                ]);

                if (!policyRes.ok) throw new Error('Failed to fetch policy');
                if (!opinionsRes.ok) throw new Error('Failed to fetch opinions');

                const policyData = await policyRes.json();
                const opinionsData = await opinionsRes.json();

                setPolicy(policyData);
                setOpinions(opinionsData);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchPolicyAndOpinions();
    }, [policyId]);

    if (loading) return <div style={{ padding: '20px' }}>Loading policy details...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>Error: {error}</div>;
    if (!policy) return <div style={{ padding: '20px' }}>Policy not found.</div>;

    return (
        <div style={{ padding: '20px', textAlign: 'left', maxWidth: '800px', margin: '0 auto' }}>
            <button onClick={onBack} style={{ marginBottom: '20px' }}>&larr; Back to Policies</button>
            
            <section style={{ marginBottom: '40px', padding: '20px', border: '1px solid #ddd', borderRadius: '8px' }}>
                <h2>Policy Details</h2>
                <p><strong>ID:</strong> {policy.id}</p>
                <p><strong>Description:</strong> {policy.description}</p>
            </section>

            <section>
                <h3>Opinions</h3>
                {opinions.length === 0 ? (
                    <p>No opinions yet for this policy.</p>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        {opinions.map(opinion => (
                            <div key={opinion.id} style={{ padding: '15px', borderLeft: '5px solid #61dafb', backgroundColor: '#f9f9f9', borderRadius: '4px' }}>
                                <p style={{ margin: '0 0 10px 0' }}>{opinion.description}</p>
                                <div style={{ fontSize: '0.9em', color: '#666' }}>
                                    <span><strong>Author:</strong> {opinion.author}</span>
                                    <span style={{ marginLeft: '20px' }}><strong>Spectrum:</strong> {opinion.politicalSpectrum}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

export default PolicyDetails;

import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

function PolicyDetails({ policyId, onBack, onCreateOpinion }) {
    const { getAccessTokenSilently } = useAuth0();
    const [policy, setPolicy] = useState(null);
    const [opinions, setOpinions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchPolicyDetails = async () => {
            setLoading(true);
            try {
                const token = await getAccessTokenSilently();
                const headers = {
                    Authorization: `Bearer ${token}`,
                };
                const response = await fetch(`/policies/${policyId}/details`, { headers });

                if (!response.ok) throw new Error('Failed to fetch policy details');

                const data = await response.json();

                setPolicy(data);
                setOpinions(data.opinions);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchPolicyDetails();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [policyId]);

    if (loading) return <div style={{ padding: '20px' }}>Loading policy details...</div>;
    if (error) return <div style={{ padding: '20px', color: 'red' }}>Error: {error}</div>;
    if (!policy) return <div style={{ padding: '20px' }}>Policy not found.</div>;

    return (
        <div style={{ padding: '20px', textAlign: 'left', maxWidth: '800px', margin: '0 auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <button onClick={onBack}>&larr; Back to Policies</button>
            </div>
            
            <section style={{ marginBottom: '40px', padding: '20px', border: '1px solid #ddd', borderRadius: '8px' }}>
                <h2>Policy Details</h2>
                <p><strong>Author:</strong> {policy.publisherName}</p>
                <p><strong>Description:</strong> {policy.description}</p>
                <button 
                    onClick={onCreateOpinion}
                    style={{ 
                        marginTop: '10px', 
                        padding: '8px 16px', 
                        backgroundColor: '#61dafb', 
                        border: 'none', 
                        borderRadius: '4px', 
                        cursor: 'pointer',
                        fontWeight: 'bold'
                    }}
                >
                    Create Opinion for this Policy
                </button>
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
                                    <span><strong>Author:</strong> {opinion.authorName}</span>
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

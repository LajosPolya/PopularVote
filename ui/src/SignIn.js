import React, { useState } from 'react';

function SignIn({ onSignIn, onSignUpClick }) {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!firstName.trim() || !lastName.trim()) {
            setError('Please enter both first and last name.');
            return;
        }

        setLoading(true);
        setError('');
        try {
            // Fetch the citizen by givenName and surname using the new search endpoint
            const response = await fetch(`/citizens/search?givenName=${encodeURIComponent(firstName.trim())}&surname=${encodeURIComponent(lastName.trim())}`);
            
            if (response.status === 404) {
                setError('Citizen not found. Please sign up.');
                return;
            }
            if (!response.ok) throw new Error('Failed to fetch citizen');
            
            const text = await response.text();
            if (!text) {
                setError('Citizen not found. Please sign up.');
                return;
            }
            const citizen = JSON.parse(text);

            if (citizen) {
                onSignIn(citizen);
            } else {
                setError('Citizen not found. Please sign up.');
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
            <h2>Sign In</h2>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <div>
                    <label htmlFor="firstName">First Name: </label>
                    <input
                        id="firstName"
                        type="text"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        style={{ width: '100%' }}
                    />
                </div>
                <div>
                    <label htmlFor="lastName">Last Name: </label>
                    <input
                        id="lastName"
                        type="text"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        style={{ width: '100%' }}
                    />
                </div>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <button type="submit" disabled={loading}>
                    {loading ? 'Signing In...' : 'Sign In'}
                </button>
            </form>
            <hr style={{ margin: '20px 0' }} />
            <button onClick={onSignUpClick} style={{ width: '100%' }}>
                Sign Up
            </button>
        </div>
    );
}

export default SignIn;

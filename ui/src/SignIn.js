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
            // We search for the citizen by name. 
            // The API doesn't seem to have a dedicated sign-in/search endpoint by name,
            // so we'll fetch all citizens and find the match.
            // In a real app, this would be a specific search or auth endpoint.
            const response = await fetch('/citizens');
            if (!response.ok) throw new Error('Failed to fetch citizens');
            
            const citizens = await response.json();
            const citizen = citizens.find(c => 
                c.givenName.toLowerCase() === firstName.trim().toLowerCase() && 
                c.surname.toLowerCase() === lastName.trim().toLowerCase()
            );

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

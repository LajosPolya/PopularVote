import React, { useState } from 'react';

function SignUp({ onSignUpSuccess, onBackToSignIn }) {
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [middleName, setMiddleName] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!firstName.trim() || !lastName.trim()) {
            setError('First name and last name are required.');
            return;
        }

        setLoading(true);
        setError('');
        try {
            const response = await fetch('/citizens', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    givenName: firstName.trim(),
                    surname: lastName.trim(),
                    middleName: middleName.trim() || null
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to sign up');
            }

            const newCitizen = await response.json();
            onSignUpSuccess(newCitizen);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
            <h2>Sign Up</h2>
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
                    <label htmlFor="middleName">Middle Name (Optional): </label>
                    <input
                        id="middleName"
                        type="text"
                        value={middleName}
                        onChange={(e) => setMiddleName(e.target.value)}
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
                    {loading ? 'Signing Up...' : 'Sign Up'}
                </button>
            </form>
            <div style={{ marginTop: '10px' }}>
                <button onClick={onBackToSignIn} style={{ width: '100%' }}>
                    Back to Sign In
                </button>
            </div>
        </div>
    );
}

export default SignUp;

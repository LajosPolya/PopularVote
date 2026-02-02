import React, { useState } from 'react';
import logo from './logo.svg';
import './App.css';
import Policies from './Policies';
import CreateOpinion from './CreateOpinion';
import PolicyDetails from './PolicyDetails';
import SignIn from './SignIn';
import SignUp from './SignUp';

function App() {
  const [view, setView] = useState('signin');
  const [user, setUser] = useState(null);
  const [selectedPolicyId, setSelectedPolicyId] = useState(null);
  const [initialPolicyIdForOpinion, setInitialPolicyIdForOpinion] = useState(null);

  const handleSignIn = (userData) => {
    setUser(userData);
    setView('policies');
  };

  const handleSignOut = () => {
    setUser(null);
    setView('signin');
  };

  const navigateToPolicy = (id) => {
    setSelectedPolicyId(id);
    setView('policy-details');
  };

  const navigateToCreateOpinion = (policyId) => {
    setInitialPolicyIdForOpinion(policyId);
    setView('create-opinion');
  };

  const renderView = () => {
    switch (view) {
      case 'signin':
        return <SignIn onSignIn={handleSignIn} onSignUpClick={() => setView('signup')} />;
      case 'signup':
        return (
          <SignUp 
            onSignUpSuccess={handleSignIn} 
            onBackToSignIn={() => setView('signin')} 
          />
        );
      case 'policies':
        return <Policies onPolicyClick={navigateToPolicy} />;
      case 'create-opinion':
        return (
          <CreateOpinion 
            initialPolicyId={initialPolicyIdForOpinion} 
            onBack={() => navigateToPolicy(initialPolicyIdForOpinion)}
          />
        );
      case 'policy-details':
        return (
          <PolicyDetails 
            policyId={selectedPolicyId} 
            onBack={() => setView('policies')} 
            onCreateOpinion={() => navigateToCreateOpinion(selectedPolicyId)}
          />
        );
      default:
        return <SignIn onSignIn={handleSignIn} onSignUpClick={() => setView('signup')} />;
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', padding: '0 20px' }}>
          <h1>Popular Vote System</h1>
          {user && (
            <div style={{ fontSize: '0.8em' }}>
              <span>Welcome, {user.givenName} {user.surname}!</span>
              <button onClick={handleSignOut} style={{ marginLeft: '10px' }}>Sign Out</button>
            </div>
          )}
        </div>
        {user && (
          <nav>
            <button onClick={() => setView('policies')} style={{ marginRight: '10px' }}>Policies</button>
          </nav>
        )}
      </header>
      <main>
        {renderView()}
      </main>
    </div>
  );
}

export default App;

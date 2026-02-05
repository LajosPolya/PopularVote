import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import './App.css';
import Policies from './Policies';
import CreateOpinion from './CreateOpinion';
import PolicyDetails from './PolicyDetails';
import CreateCitizen from './CreateCitizen';

function App() {
  const {
    isLoading,
    isAuthenticated,
    error,
    user,
    loginWithRedirect,
    logout,
    getAccessTokenSilently,
  } = useAuth0();

  const [view, setView] = useState('policies');
  const [selectedPolicyId, setSelectedPolicyId] = useState(null);
  const [initialPolicyIdForOpinion, setInitialPolicyIdForOpinion] = useState(null);
  const [isCheckingCitizen, setIsCheckingCitizen] = useState(false);
  const [hasCitizen, setHasCitizen] = useState(false);

  useEffect(() => {
    const checkCitizen = async () => {
      if (isAuthenticated && user) {
        setIsCheckingCitizen(true);
        try {
          const token = await getAccessTokenSilently();
          const response = await fetch(`/citizens/auth/${user.sub}`, {
            method: 'HEAD',
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          if (response.status === 204) {
            setHasCitizen(true);
          } else if (response.status === 404) {
            setHasCitizen(false);
          }
        } catch (err) {
          console.error("Error checking citizen existence:", err);
        } finally {
          setIsCheckingCitizen(false);
        }
      }
    };
    checkCitizen();
  }, [isAuthenticated, user, getAccessTokenSilently]);

  const navigateToPolicy = (id) => {
    setSelectedPolicyId(id);
    setView('policy-details');
  };

  const navigateToCreateOpinion = (policyId) => {
    setInitialPolicyIdForOpinion(policyId);
    setView('create-opinion');
  };

  const renderView = () => {
    if (isLoading || isCheckingCitizen) {
      return <div>Loading...</div>;
    }

    if (!isAuthenticated) {
      return (
        <div style={{ padding: '20px' }}>
          <h2>Please sign in to continue</h2>
          <button onClick={() => loginWithRedirect()}>Sign In / Sign Up</button>
        </div>
      );
    }

    if (!hasCitizen) {
      return (
        <CreateCitizen 
          onCreateSuccess={() => setHasCitizen(true)} 
        />
      );
    }

    switch (view) {
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
        return <Policies onPolicyClick={navigateToPolicy} />;
    }
  };

  if (error) {
    return <div>Oops... {error.message}</div>;
  }

  return (
    <div className="App">
      <header className="App-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', padding: '0 20px' }}>
          <h1>Popular Vote System</h1>
          {isAuthenticated && (
            <div style={{ fontSize: '0.8em' }}>
              <span>Welcome, {user.name}!</span>
              <button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })} style={{ marginLeft: '10px' }}>Sign Out</button>
            </div>
          )}
        </div>
        {isAuthenticated && (
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

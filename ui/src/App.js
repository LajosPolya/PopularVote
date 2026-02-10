import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import './App.css';
import Policies from './Policies';
import CreateOpinion from './CreateOpinion';
import PolicyDetails from './PolicyDetails';
import CreateCitizen from './CreateCitizen';
import Profile from './Profile';
import Citizens from './Citizens';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

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
  const [citizenCheckError, setCitizenCheckError] = useState(null);

  useEffect(() => {
    const checkCitizen = async () => {
      if (isAuthenticated && user) {
        setIsCheckingCitizen(true);
        try {
          const token = await getAccessTokenSilently();
          const response = await fetch(`${popularVoteApiUrl}/citizens/self`, {
            method: 'HEAD',
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          if (response.status === 204) {
            setHasCitizen(true);
          } else if (response.status === 404) {
            setHasCitizen(false);
          } else if (response.status === 401 || response.status === 403) {
            setCitizenCheckError("You are not authorized to access this resource. Please ensure your account has the necessary permissions.");
          } else {
            setCitizenCheckError(`An unexpected error occurred: ${JSON.stringify(response)}, ${popularVoteApiUrl}`);
          }
        } catch (err) {
          console.error("Error checking citizen existence:", err);
          setCitizenCheckError(`Failed to check citizen status. Please try again later. ${err}, ${popularVoteApiUrl}`);
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

    if (citizenCheckError) {
      return (
        <div style={{ padding: '20px', color: 'red' }}>
          <h2>Error</h2>
          <p>{citizenCheckError}</p>
          <button onClick={() => window.location.reload()}>Retry</button>
        </div>
      );
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
      case 'profile':
        return (
          <Profile 
            onBack={() => setView('policies')} 
          />
        );
      case 'citizens':
        return (
          <Citizens />
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
              {hasCitizen && <button onClick={() => setView('profile')} style={{ marginLeft: '10px' }}>Profile</button>}
              <button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })} style={{ marginLeft: '10px' }}>Sign Out</button>
            </div>
          )}
        </div>
        {isAuthenticated && hasCitizen && (
          <nav>
            <button onClick={() => setView('policies')} style={{ marginRight: '10px' }}>Policies</button>
            <button onClick={() => setView('citizens')} style={{ marginRight: '10px' }}>Citizens</button>
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

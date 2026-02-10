import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import './App.css';
import Policies from './Policies';
import CreatePolicy from './CreatePolicy';
import CreateOpinion from './CreateOpinion';
import PolicyDetails from './PolicyDetails';
import CreateCitizen from './CreateCitizen';
import Profile from './Profile';
import Citizens from './Citizens';
import VerifyPoliticians from './VerifyPoliticians';

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
  const [selectedCitizenId, setSelectedCitizenId] = useState(null);
  const [isCheckingCitizen, setIsCheckingCitizen] = useState(false);
  const [hasCitizen, setHasCitizen] = useState(false);
  const [citizenCheckError, setCitizenCheckError] = useState(null);
  const [canVerifyPolitician, setCanVerifyPolitician] = useState(false);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      loginWithRedirect();
    }
  }, [isLoading, isAuthenticated, loginWithRedirect]);

  useEffect(() => {
    const checkCitizen = async () => {
      if (isAuthenticated && user) {
        setIsCheckingCitizen(true);
        try {
          const token = await getAccessTokenSilently();
          const payload = JSON.parse(atob(token.split('.')[1]));
          const permissions = payload.scope?.split(' ') || [];
          setCanVerifyPolitician(permissions.includes('read:verify-politician'));

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

  const navigateToCreatePolicy = () => {
    setView('create-policy');
  };

  const navigateToCitizenProfile = (id) => {
    setSelectedCitizenId(id);
    setView('profile');
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
      return null;
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
        return <Policies onPolicyClick={navigateToPolicy} onCreatePolicy={navigateToCreatePolicy} />;
      case 'create-policy':
        return (
          <CreatePolicy 
            onBack={() => setView('policies')} 
            onCreateSuccess={() => setView('policies')} 
          />
        );
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
            citizenId={selectedCitizenId}
            onBack={() => {
              if (selectedCitizenId) {
                setView('citizens');
                setSelectedCitizenId(null);
              } else {
                setView('policies');
              }
            }} 
          />
        );
      case 'citizens':
        return (
          <Citizens onCitizenClick={navigateToCitizenProfile} />
        );
      case 'verify-politicians':
        return (
          <VerifyPoliticians onCitizenClick={navigateToCitizenProfile} />
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
              {hasCitizen && (
                <button 
                  onClick={() => {
                    setSelectedCitizenId(null);
                    setView('profile');
                  }} 
                  style={{ marginLeft: '10px' }}
                >
                  Profile
                </button>
              )}
              <button onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })} style={{ marginLeft: '10px' }}>Sign Out</button>
            </div>
          )}
        </div>
        {isAuthenticated && hasCitizen && (
          <nav>
            <button onClick={() => setView('policies')} style={{ marginRight: '10px' }}>Policies</button>
            <button onClick={() => setView('citizens')} style={{ marginRight: '10px' }}>Citizens</button>
            {canVerifyPolitician && (
              <button onClick={() => setView('verify-politicians')} style={{ marginRight: '10px' }}>Verify Politicians</button>
            )}
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

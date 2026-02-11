import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Button, 
  Container, 
  Box, 
  CircularProgress, 
  Alert,
  IconButton,
  Menu,
  MenuItem,
  Tooltip,
  Avatar
} from '@mui/material';
import AccountCircle from '@mui/icons-material/AccountCircle';
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
      return (
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
          <CircularProgress />
          <Typography variant="h6" sx={{ ml: 2 }}>Loading...</Typography>
        </Box>
      );
    }

    if (citizenCheckError) {
      return (
        <Box sx={{ mt: 4 }}>
          <Alert severity="error" action={
            <Button color="inherit" size="small" onClick={() => window.location.reload()}>
              RETRY
            </Button>
          }>
            <Typography variant="h6">Error</Typography>
            <Typography>{citizenCheckError}</Typography>
          </Alert>
        </Box>
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
    return (
      <Container>
        <Alert severity="error" sx={{ mt: 4 }}>
          Oops... {error.message}
        </Alert>
      </Container>
    );
  }

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, cursor: 'pointer' }} onClick={() => setView('policies')}>
            Popular Vote System
          </Typography>
          {isAuthenticated && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" sx={{ mr: 2 }}>
                Welcome, {user.name}!
              </Typography>
              {hasCitizen && (
                <>
                  <Button color="inherit" onClick={() => setView('policies')}>Policies</Button>
                  <Button color="inherit" onClick={() => setView('citizens')}>Citizens</Button>
                  {canVerifyPolitician && (
                    <Button color="inherit" onClick={() => setView('verify-politicians')}>Verify Politicians</Button>
                  )}
                  <Tooltip title="Profile">
                    <IconButton
                      size="large"
                      color="inherit"
                      onClick={() => {
                        setSelectedCitizenId(null);
                        setView('profile');
                      }}
                    >
                      <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
                        {user.name[0]}
                      </Avatar>
                    </IconButton>
                  </Tooltip>
                </>
              )}
              <Button color="inherit" onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}>
                Sign Out
              </Button>
            </Box>
          )}
        </Toolbar>
      </AppBar>
      <Container component="main" sx={{ mt: 4, mb: 4 }}>
        {renderView()}
      </Container>
    </Box>
  );
}

export default App;

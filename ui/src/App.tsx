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
  Avatar,
  Select,
  FormControl
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
import PoliticianSearch from './PoliticianSearch';
import VerifyPoliticians from './VerifyPoliticians';
import BookmarkedPolicies from './BookmarkedPolicies';
import PoliticalParties from './PoliticalParties';
import PoliticalPartyDetails from './PoliticalPartyDetails';
import CreatePoliticalParty from './CreatePoliticalParty';
import PoliticianDeclaration from './PoliticianDeclaration';
import IdVerification from './IdVerification';
import {Citizen, LevelOfPolitics, PoliticalParty} from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

const App: React.FC = () => {
  const {
    isLoading,
    isAuthenticated,
    error,
    user,
    loginWithRedirect,
    logout,
    getAccessTokenSilently,
  } = useAuth0();

  const [view, setView] = useState<string>('policies');
  const [selectedPolicyId, setSelectedPolicyId] = useState<number | null>(null);
  const [initialPolicyIdForOpinion, setInitialPolicyIdForOpinion] = useState<number | null>(null);
  const [selectedCitizenId, setSelectedCitizenId] = useState<number | null>(null);
  const [isCheckingCitizen, setIsCheckingCitizen] = useState<boolean>(false);
  const [hasCitizen, setHasCitizen] = useState<boolean>(false);
  const [citizenCheckError, setCitizenCheckError] = useState<string | null>(null);
  const [canVerifyPolitician, setCanVerifyPolitician] = useState<boolean>(false);
  const [canReadPoliticalParties, setCanReadPoliticalParties] = useState<boolean>(false);
  const [canWritePoliticalParties, setCanWritePoliticalParties] = useState<boolean>(false);
  const [canWriteVotes, setCanWriteVotes] = useState<boolean>(false);
  const [selectedPoliticalPartyId, setSelectedPoliticalPartyId] = useState<number | null>(null);
  const [levelsOfPolitics, setLevelsOfPolitics] = useState<LevelOfPolitics[]>([]);
  const [selectedLevelOfPolitics, setSelectedLevelOfPolitics] = useState<number>(1);
  const [parties, setParties] = useState<Map<number, PoliticalParty>>(new Map());
  const [self, setSelf] = useState<Citizen | null>(null);

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      loginWithRedirect();
    }
  }, [isLoading, isAuthenticated, loginWithRedirect]);

  useEffect(() => {
    const fetchParties = async () => {
      if (isAuthenticated) {
        try {
          const token = await getAccessTokenSilently();
          const response = await fetch(`${popularVoteApiUrl}/political-parties`, {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          if (!response.ok) {
            throw new Error('Failed to fetch political parties');
          }
          const data: PoliticalParty[] = await response.json();
          const politicalPartyMap = new Map<number, PoliticalParty>();
          data.map(party => politicalPartyMap.set(party.id, party));
          setParties(politicalPartyMap);
        } catch (err: any) {
          console.error("Error fetching political parties:", err);
        }
      }
    };
    fetchParties();
  }, [isAuthenticated, getAccessTokenSilently]);

  useEffect(() => {
    const fetchLevelsOfPolitics = async () => {
      if (isAuthenticated) {
        try {
          const token = await getAccessTokenSilently();
          const response = await fetch(`${popularVoteApiUrl}/levels-of-politics`, {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          if (response.ok) {
            const levels = await response.json();
            setLevelsOfPolitics(levels);
            if (levels.length > 0) {
              setSelectedLevelOfPolitics(levels[0].id);
            }
          }
        } catch (err) {
          console.error("Error fetching levels of politics:", err);
        }
      }
    };
    fetchLevelsOfPolitics();
  }, [isAuthenticated, getAccessTokenSilently]);

  useEffect(() => {
    const checkCitizen = async () => {
      if (isAuthenticated && user) {
        setIsCheckingCitizen(true);
        try {
          const token = await getAccessTokenSilently();
          const payload = JSON.parse(atob(token.split('.')[1] || ''));
          const permissions = payload.scope?.split(' ') || [];
          setCanVerifyPolitician(permissions.includes('read:verify-politician'));
          setCanReadPoliticalParties(permissions.includes('read:political-parties'));
          setCanWritePoliticalParties(permissions.includes('write:political-parties'));
          setCanWriteVotes(permissions.includes('write:votes'));

          const response = await fetch(`${popularVoteApiUrl}/citizens/self`, {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          if (response.status === 200) {
            setHasCitizen(true);
            setSelf(await response.json());
          } else if (response.status === 404) {
            setHasCitizen(false);
          } else if (response.status === 401 || response.status === 403) {
            setCitizenCheckError("You are not authorized to access this resource. Please ensure your account has the necessary permissions.");
          } else {
            setCitizenCheckError(`An unexpected error occurred: ${response.status}, ${popularVoteApiUrl}`);
          }
        } catch (err: any) {
          console.error("Error checking citizen existence:", err);
          setCitizenCheckError(`Failed to check citizen status. Please try again later. ${err.message}, ${popularVoteApiUrl}`);
        } finally {
          setIsCheckingCitizen(false);
        }
      }
    };
    checkCitizen();
  }, [isAuthenticated, user, getAccessTokenSilently]);

  const navigateToPolicy = (id: number) => {
    setSelectedPolicyId(id);
    setView('policy-details');
  };

  const navigateToCreateOpinion = (policyId: number | null) => {
    setInitialPolicyIdForOpinion(policyId);
    setView('create-opinion');
  };

  const navigateToCreatePolicy = () => {
    setView('create-policy');
  };

  const navigateToCitizenProfile = (id: number | null) => {
    setSelectedCitizenId(id);
    setView('profile');
  };

  const navigateToPoliticianProfile = (id: number) => {
    setSelectedCitizenId(id);
    setView('profile');
  };

  const navigateToPoliticalParty = (id: number) => {
    setSelectedPoliticalPartyId(id);
    setView('political-party-details');
  };

  const navigateToCreatePoliticalParty = () => {
    setView('create-political-party');
  };

  const navigateToPoliticianDeclaration = () => {
    setView('politician-declaration');
  };

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleClose();
    logout({ logoutParams: { returnTo: window.location.origin } });
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
        return <Policies
            onPolicyClick={navigateToPolicy}
            onCitizenClick={navigateToCitizenProfile}
            onCreatePolicy={navigateToCreatePolicy}
            levelOfPoliticsId={selectedLevelOfPolitics}
            politicalParties={parties}
        />;
      case 'create-policy':
        return (
          <CreatePolicy 
            onBack={() => setView('policies')} 
            onCreateSuccess={() => setView('policies')}
            self={self}
            politicalParties={parties}
            levelOfPoliticsId={selectedLevelOfPolitics}
          />
        );
      case 'create-opinion':
        return (
          <CreateOpinion 
            initialPolicyId={initialPolicyIdForOpinion} 
            onBack={() => navigateToPolicy(initialPolicyIdForOpinion as number)}
          />
        );
      case 'policy-details':
        return (
          <PolicyDetails 
            policyId={selectedPolicyId} 
            onBack={() => setView('policies')} 
            onCitizenClick={navigateToCitizenProfile}
            onCreateOpinion={() => navigateToCreateOpinion(selectedPolicyId)}
            politicalParties={parties}
            canWriteVotes={canWriteVotes}
            onVerifyIdentity={() => setView('id-verification')}
          />
        );
      case 'profile':
        return (
          <Profile 
            citizenId={selectedCitizenId}
            onDeclarePolitician={navigateToPoliticianDeclaration}
            onPolicyClick={navigateToPolicy}
            onBack={() => {
              if (selectedCitizenId) {
                // If we were at politician-search or citizens, we should go back there
                // However, the 'view' state is currently 'profile' here.
                // We need to know where we came from.
                // For now, let's just default to citizens if it was a general profile view
                // but navigateToPoliticianProfile and navigateToCitizenProfile could set a 'previousView'
                setView('citizens');
                setSelectedCitizenId(null);
              } else {
                setView('policies');
              }
            }}
            politicalParties={parties}
          />
        );
      case 'citizens':
        return (
          <Citizens onCitizenClick={navigateToCitizenProfile} politicalParties={parties}/>
        );
      case 'politician-search':
        return (
          <PoliticianSearch onPoliticianClick={navigateToPoliticianProfile} levelOfPoliticsId={selectedLevelOfPolitics} politicalParties={parties}/>
        );
      case 'verify-politicians':
        return (
          <VerifyPoliticians onCitizenClick={navigateToCitizenProfile} politicalParties={parties} />
        );
      case 'bookmarked-policies':
        return (
          <BookmarkedPolicies 
            onPolicyClick={navigateToPolicy} 
            onCitizenClick={navigateToCitizenProfile}
            onBack={() => setView('policies')} 
          />
        );
      case 'political-parties':
        return (
          <PoliticalParties 
            onPartyClick={navigateToPoliticalParty} 
            canCreateParty={canWritePoliticalParties}
            onCreateParty={navigateToCreatePoliticalParty}
            levelOfPoliticsId={selectedLevelOfPolitics || 1}
          />
        );
      case 'create-political-party':
        return (
          <CreatePoliticalParty
            onBack={() => setView('political-parties')}
            onCreateSuccess={() => setView('political-parties')}
            levelOfPoliticsId={selectedLevelOfPolitics || 1}
          />
        );
      case 'political-party-details':
        return (
          <PoliticalPartyDetails 
            partyId={selectedPoliticalPartyId} 
            onBack={() => setView('political-parties')} 
            onCitizenClick={navigateToCitizenProfile}
            onPolicyClick={navigateToPolicy}
          />
        );
      case 'politician-declaration':
        return (
          <PoliticianDeclaration 
            onSuccess={() => setView('profile')}
            onCancel={() => setView('profile')}
          />
        );
      case 'id-verification':
        return <IdVerification onVerificationSuccess={(updatedCitizen) => setSelf(updatedCitizen)} />;
      default:
        return <Policies
            onPolicyClick={navigateToPolicy}
            onCitizenClick={navigateToCitizenProfile}
            onCreatePolicy={navigateToCreatePolicy}
            levelOfPoliticsId={selectedLevelOfPolitics}
            politicalParties={parties}
        />;
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
          <Typography
            variant="h6"
            component="div"
            sx={{ cursor: 'pointer', mr: 2 }}
            onClick={() => setView('policies')}
          >
            Popular Vote System
          </Typography>

          { hasCitizen && isAuthenticated && levelsOfPolitics.length > 0 && (
            <FormControl size="small" sx={{ minWidth: 150, mr: 'auto' }}>
              <Select
                value={selectedLevelOfPolitics || ''}
                onChange={(e) => setSelectedLevelOfPolitics(Number(e.target.value))}
                sx={{
                  color: 'white',
                  '.MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255, 255, 255, 0.3)' },
                  '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255, 255, 255, 0.5)' },
                  '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: 'white' },
                  '.MuiSvgIcon-root': { color: 'white' }
                }}
              >
                {levelsOfPolitics.map((level) => (
                  <MenuItem key={level.id} value={level.id}>
                    {level.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}

          {hasCitizen && isAuthenticated && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body1" sx={{ mr: 2, display: { xs: 'none', md: 'block' } }}>
                Welcome, {user?.name}!
              </Typography>
              <Button color="inherit" onClick={() => setView('policies')}>Policies</Button>
              <Button color="inherit" onClick={() => setView('politician-search')}>Politicians</Button>
              {canReadPoliticalParties && (
                <Button color="inherit" onClick={() => setView('political-parties')}>Parties</Button>
              )}
              <Button color="inherit" onClick={() => setView('citizens')}>Citizens</Button>
              {canVerifyPolitician && (
                <Button color="inherit" onClick={() => setView('verify-politicians')}>Verify</Button>
              )}
              
              <Tooltip title="Account settings">
                <IconButton
                  size="large"
                  aria-label="account of current user"
                  aria-controls="menu-appbar"
                  aria-haspopup="true"
                  onClick={handleMenu}
                  color="inherit"
                >
                  {user?.picture ? (
                    <Avatar alt={user.name} src={user.picture} sx={{ width: 32, height: 32 }} />
                  ) : (
                    <AccountCircle />
                  )}
                </IconButton>
              </Tooltip>
              <Menu
                id="menu-appbar"
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <Box sx={{ px: 2, py: 1 }}>
                  <Typography variant="subtitle2">{user?.name}</Typography>
                  <Typography variant="body2" color="text.secondary">{user?.email}</Typography>
                </Box>
                <MenuItem onClick={() => { handleClose(); navigateToCitizenProfile(null); }}>Profile</MenuItem>
                {!self?.postalCodeId && (
                  <MenuItem onClick={() => { handleClose(); setView('id-verification'); }}>ID Verification</MenuItem>
                )}
                <MenuItem onClick={() => { handleClose(); setView('bookmarked-policies'); }}>Bookmarks</MenuItem>
                <MenuItem onClick={handleLogout}>Logout</MenuItem>
              </Menu>
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {hasCitizen && isAuthenticated && !self?.postalCodeId && view !== 'id-verification' && (
        <Alert
          severity="warning"
          sx={{
            cursor: 'pointer',
            borderRadius: 0,
            '&:hover': {
              backgroundColor: 'info.light',
            },
          }}
          onClick={() => setView('id-verification')}
        >
          Verify your identity to unlock the ability to vote on policies.
        </Alert>
      )}

      <Container sx={{ py: 4 }}>
        {renderView()}
      </Container>
    </Box>
  );
}

export default App;

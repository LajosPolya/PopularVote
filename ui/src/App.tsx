import { useAuth0 } from "@auth0/auth0-react";
import AccountCircle from "@mui/icons-material/AccountCircle";
import Brightness4Icon from "@mui/icons-material/Brightness4";
import Brightness7Icon from "@mui/icons-material/Brightness7";
import {
  Alert,
  AppBar,
  Avatar,
  Box,
  Button,
  CircularProgress,
  Container,
  FormControl,
  IconButton,
  InputLabel,
  Menu,
  MenuItem,
  Select,
  Toolbar,
  Tooltip,
  Typography,
} from "@mui/material";
import React, { useEffect, useState } from "react";
import "./App.css";
import BookmarkedPolicies from "./BookmarkedPolicies";
import Citizens from "./Citizens";
import { useColorMode } from "./ColorModeProvider";
import CreateCitizen from "./CreateCitizen";
import CreateOpinion from "./CreateOpinion";
import CreatePolicy from "./CreatePolicy";
import CreatePoliticalParty from "./CreatePoliticalParty";
import IdVerification from "./IdVerification";
import LandingPage from "./LandingPage";
import Policies from "./Policies";
import PolicyDetails from "./PolicyDetails";
import PoliticalParties from "./PoliticalParties";
import PoliticalPartyDetails from "./PoliticalPartyDetails";
import PoliticianDeclaration from "./PoliticianDeclaration";
import PoliticianSearch from "./PoliticianSearch";
import Profile from "./Profile";
import {
  Citizen,
  LevelOfPolitics,
  PoliticalParty,
  ProvinceAndTerritory,
} from "./types";
import VerifyPoliticians from "./VerifyPoliticians";

interface HistoryEntry {
  view: string;
  selectedPolicyId: number | null;
  selectedCitizenId: number | null;
  selectedPoliticalPartyId: number | null;
  initialPolicyIdForOpinion: number | null;
}

const VIEW_LABELS: Record<string, string> = {
  policies: "Policies",
  "policy-details": "Policy Details",
  profile: "Profile",
  citizens: "Citizens",
  "politician-search": "Politicians",
  "verify-politicians": "Verify Politicians",
  "bookmarked-policies": "Bookmarks",
  "political-parties": "Parties",
  "political-party-details": "Party Details",
  "politician-declaration": "Politician Declaration",
  "id-verification": "ID Verification",
  "create-policy": "Create Policy",
  "create-opinion": "Create Opinion",
  "create-political-party": "Create Party",
};

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

const App: React.FC = () => {
  const { mode, toggleColorMode } = useColorMode();
  const {
    isLoading,
    isAuthenticated,
    error,
    user,
    loginWithRedirect,
    logout,
    getAccessTokenSilently,
  } = useAuth0();

  const [view, setView] = useState<string>("policies");
  const [history, setHistory] = useState<HistoryEntry[]>([]);
  const [selectedPolicyId, setSelectedPolicyId] = useState<number | null>(null);
  const [initialPolicyIdForOpinion, setInitialPolicyIdForOpinion] = useState<
    number | null
  >(null);
  const [selectedCitizenId, setSelectedCitizenId] = useState<number | null>(
    null,
  );
  const [isCheckingCitizen, setIsCheckingCitizen] = useState<boolean>(false);
  const [hasCitizen, setHasCitizen] = useState<boolean>(false);
  const [citizenCheckError, setCitizenCheckError] = useState<string | null>(
    null,
  );
  const [canVerifyPolitician, setCanVerifyPolitician] =
    useState<boolean>(false);
  const [canReadPoliticalParties, setCanReadPoliticalParties] =
    useState<boolean>(false);
  const [canWritePoliticalParties, setCanWritePoliticalParties] =
    useState<boolean>(false);
  const [canWriteVotes, setCanWriteVotes] = useState<boolean>(false);
  const [selectedPoliticalPartyId, setSelectedPoliticalPartyId] = useState<
    number | null
  >(null);
  const [levelsOfPolitics, setLevelsOfPolitics] = useState<LevelOfPolitics[]>(
    [],
  );
  const [selectedLevelOfPolitics, setSelectedLevelOfPolitics] =
    useState<number>(1);
  const [provincesAndTerritories, setProvincesAndTerritories] = useState<
    ProvinceAndTerritory[]
  >([]);
  const [selectedProvinceAndTerritory, setSelectedProvinceAndTerritory] =
    useState<number>(0);
  const [parties, setParties] = useState<Map<number, PoliticalParty>>(
    new Map(),
  );
  const [self, setSelf] = useState<Citizen | null>(null);

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  useEffect(() => {
    // We used to redirect to login automatically, but now we show a landing page
  }, [isLoading, isAuthenticated, loginWithRedirect]);

  useEffect(() => {
    const fetchParties = async () => {
      if (isAuthenticated) {
        try {
          const token = await getAccessTokenSilently();
          const response = await fetch(
            `${popularVoteApiUrl}/political-parties`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            },
          );
          if (response.ok) {
            const data: PoliticalParty[] = await response.json();
            const politicalPartyMap = new Map<number, PoliticalParty>();
            data.map((party) => politicalPartyMap.set(party.id, party));
            setParties(politicalPartyMap);
          }
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
          const response = await fetch(
            `${popularVoteApiUrl}/levels-of-politics`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            },
          );
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
    const fetchProvincesAndTerritories = async () => {
      if (isAuthenticated) {
        try {
          const token = await getAccessTokenSilently();
          const response = await fetch(
            `${popularVoteApiUrl}/provinces-and-territories`,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            },
          );
          if (response.ok) {
            const data = await response.json();
            setProvincesAndTerritories(data);
          }
        } catch (err) {
          console.error("Error fetching provinces and territories:", err);
        }
      }
    };
    fetchProvincesAndTerritories();
  }, [isAuthenticated, getAccessTokenSilently]);

  useEffect(() => {
    const checkCitizen = async () => {
      if (isAuthenticated && user) {
        setIsCheckingCitizen(true);
        try {
          const token = await getAccessTokenSilently();
          const payload = JSON.parse(atob(token.split(".")[1] || ""));
          const permissions = payload.scope?.split(" ") || [];
          setCanVerifyPolitician(
            permissions.includes("read:verify-politician"),
          );
          setCanReadPoliticalParties(
            permissions.includes("read:political-parties"),
          );
          setCanWritePoliticalParties(
            permissions.includes("write:political-parties"),
          );
          setCanWriteVotes(permissions.includes("write:votes"));

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
            setCitizenCheckError(
              "You are not authorized to access this resource. Please ensure your account has the necessary permissions.",
            );
          } else {
            setCitizenCheckError(
              `An unexpected error occurred: ${response.status}, ${popularVoteApiUrl}`,
            );
          }
        } catch (err: any) {
          console.error("Error checking citizen existence:", err);
          setCitizenCheckError(
            `Failed to check citizen status. Please try again later. ${err.message}, ${popularVoteApiUrl}`,
          );
        } finally {
          setIsCheckingCitizen(false);
        }
      }
    };
    checkCitizen();
  }, [isAuthenticated, user, getAccessTokenSilently]);

  const navigateToPolicy = (id: number) => {
    pushHistory();
    setSelectedPolicyId(id);
    setView("policy-details");
  };

  const navigateToCreateOpinion = (policyId: number | null) => {
    pushHistory();
    setInitialPolicyIdForOpinion(policyId);
    setView("create-opinion");
  };

  const navigateToCreatePolicy = () => {
    pushHistory();
    setView("create-policy");
  };

  const navigateToCitizenProfile = (id: number | null) => {
    pushHistory();
    setSelectedCitizenId(id);
    setView("profile");
  };

  const navigateToPoliticianProfile = (id: number) => {
    pushHistory();
    setSelectedCitizenId(id);
    setView("profile");
  };

  const navigateToPoliticalParty = (id: number) => {
    pushHistory();
    setSelectedPoliticalPartyId(id);
    setView("political-party-details");
  };

  const navigateToCreatePoliticalParty = () => {
    pushHistory();
    setView("create-political-party");
  };

  const navigateToPoliticianDeclaration = () => {
    pushHistory();
    setView("politician-declaration");
  };

  const pushHistory = () => {
    setHistory((prev) => [
      ...prev,
      {
        view,
        selectedPolicyId,
        selectedCitizenId,
        selectedPoliticalPartyId,
        initialPolicyIdForOpinion,
      },
    ]);
  };

  const popHistory = () => {
    if (history.length === 0) {
      setView("policies");
      return;
    }

    const lastEntry = history[history.length - 1];
    setHistory((prev) => prev.slice(0, -1));
    setView(lastEntry.view);
    setSelectedPolicyId(lastEntry.selectedPolicyId);
    setSelectedCitizenId(lastEntry.selectedCitizenId);
    setSelectedPoliticalPartyId(lastEntry.selectedPoliticalPartyId);
    setInitialPolicyIdForOpinion(lastEntry.initialPolicyIdForOpinion);
  };

  const navigateTo = (newView: string) => {
    if (newView !== view) {
      pushHistory();
      setView(newView);
    }
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

  if (isLoading) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        minHeight="100vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <LandingPage />;
  }

  const renderView = () => {
    if (isLoading || isCheckingCitizen) {
      return (
        <Box
          display="flex"
          justifyContent="center"
          alignItems="center"
          minHeight="50vh"
        >
          <CircularProgress />
          <Typography variant="h6" sx={{ ml: 2 }}>
            Loading...
          </Typography>
        </Box>
      );
    }

    if (citizenCheckError) {
      return (
        <Box sx={{ mt: 4 }}>
          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                onClick={() => window.location.reload()}
              >
                RETRY
              </Button>
            }
          >
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
      return <CreateCitizen onCreateSuccess={() => setHasCitizen(true)} />;
    }

    const lastEntry = history.length > 0 ? history[history.length - 1] : null;
    const backLabel = lastEntry
      ? VIEW_LABELS[lastEntry.view] || "Back"
      : "Back";

    switch (view) {
      case "policies":
        return (
          <Policies
            onPolicyClick={navigateToPolicy}
            onCitizenClick={navigateToCitizenProfile}
            onPartyClick={navigateToPoliticalParty}
            onCreatePolicy={navigateToCreatePolicy}
            levelOfPoliticsId={selectedLevelOfPolitics}
            provinceAndTerritoryId={
              selectedLevelOfPolitics === 2 &&
              selectedProvinceAndTerritory !== 0
                ? selectedProvinceAndTerritory
                : null
            }
            politicalParties={parties}
          />
        );
      case "create-policy":
        return (
          <CreatePolicy
            onBack={popHistory}
            backLabel={backLabel}
            onCreateSuccess={() => {
              setHistory([]);
              setView("policies");
            }}
            self={self}
            politicalParties={parties}
            levelOfPoliticsId={selectedLevelOfPolitics}
          />
        );
      case "create-opinion":
        return (
          <CreateOpinion
            initialPolicyId={initialPolicyIdForOpinion}
            onBack={popHistory}
            backLabel={backLabel}
          />
        );
      case "policy-details":
        return (
          <PolicyDetails
            policyId={selectedPolicyId}
            onBack={popHistory}
            backLabel={backLabel}
            onCitizenClick={navigateToCitizenProfile}
            onPartyClick={navigateToPoliticalParty}
            onCreateOpinion={() => navigateToCreateOpinion(selectedPolicyId)}
            politicalParties={parties}
            canWriteVotes={canWriteVotes}
            onVerifyIdentity={() => navigateTo("id-verification")}
          />
        );
      case "profile":
        return (
          <Profile
            citizenId={selectedCitizenId}
            onDeclarePolitician={navigateToPoliticianDeclaration}
            onPolicyClick={navigateToPolicy}
            onPartyClick={navigateToPoliticalParty}
            onBack={popHistory}
            backLabel={backLabel}
            politicalParties={parties}
          />
        );
      case "citizens":
        return (
          <Citizens
            onCitizenClick={navigateToCitizenProfile}
            politicalParties={parties}
          />
        );
      case "politician-search":
        return (
          <PoliticianSearch
            onPoliticianClick={navigateToPoliticianProfile}
            levelOfPoliticsId={selectedLevelOfPolitics}
            provinceAndTerritoryId={
              selectedLevelOfPolitics === 2 &&
              selectedProvinceAndTerritory !== 0
                ? selectedProvinceAndTerritory
                : null
            }
            politicalParties={parties}
          />
        );
      case "verify-politicians":
        return (
          <VerifyPoliticians
            onCitizenClick={navigateToCitizenProfile}
            politicalParties={parties}
          />
        );
      case "bookmarked-policies":
        return (
          <BookmarkedPolicies
            onPolicyClick={navigateToPolicy}
            onCitizenClick={navigateToCitizenProfile}
            onBack={popHistory}
            backLabel={backLabel}
          />
        );
      case "political-parties":
        return (
          <PoliticalParties
            onPartyClick={navigateToPoliticalParty}
            canCreateParty={canWritePoliticalParties}
            onCreateParty={navigateToCreatePoliticalParty}
            levelOfPoliticsId={selectedLevelOfPolitics || 1}
            provinceAndTerritoryId={
              selectedLevelOfPolitics === 2 &&
              selectedProvinceAndTerritory !== 0
                ? selectedProvinceAndTerritory
                : null
            }
          />
        );
      case "create-political-party":
        return (
          <CreatePoliticalParty
            onBack={popHistory}
            backLabel={backLabel}
            onCreateSuccess={() => {
              setHistory([]);
              setView("political-parties");
            }}
            levelOfPoliticsId={selectedLevelOfPolitics || 1}
            provinceAndTerritoryId={
              selectedLevelOfPolitics === 2 &&
              selectedProvinceAndTerritory !== 0
                ? selectedProvinceAndTerritory
                : null
            }
          />
        );
      case "political-party-details":
        return (
          <PoliticalPartyDetails
            partyId={selectedPoliticalPartyId}
            onBack={popHistory}
            backLabel={backLabel}
            onCitizenClick={navigateToCitizenProfile}
            onPolicyClick={navigateToPolicy}
          />
        );
      case "politician-declaration":
        return (
          <PoliticianDeclaration onSuccess={popHistory} onCancel={popHistory} />
        );
      case "id-verification":
        return (
          <IdVerification
            onVerificationSuccess={(updatedCitizen) => setSelf(updatedCitizen)}
          />
        );
      default:
        return (
          <Policies
            onPolicyClick={navigateToPolicy}
            onCitizenClick={navigateToCitizenProfile}
            onPartyClick={navigateToPoliticalParty}
            onCreatePolicy={navigateToCreatePolicy}
            levelOfPoliticsId={selectedLevelOfPolitics}
            provinceAndTerritoryId={
              selectedLevelOfPolitics === 2 &&
              selectedProvinceAndTerritory !== 0
                ? selectedProvinceAndTerritory
                : null
            }
            politicalParties={parties}
          />
        );
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
            sx={{ cursor: "pointer", mr: 2 }}
            onClick={() => {
              setHistory([]);
              setView("policies");
            }}
          >
            Popular Vote System
          </Typography>

          {hasCitizen && isAuthenticated && levelsOfPolitics.length > 0 && (
            <Box sx={{ display: "flex", mr: "auto", gap: 2 }}>
              <FormControl size="small" sx={{ minWidth: 150 }}>
                <InputLabel
                  id="level-of-politics-label"
                  sx={{
                    color: "white",
                    "&.Mui-focused": { color: "white" },
                  }}
                >
                  Level of Politics
                </InputLabel>
                <Select
                  labelId="level-of-politics-label"
                  label="Level of Politics"
                  value={selectedLevelOfPolitics || ""}
                  onChange={(e) => {
                    const newLevel = Number(e.target.value);
                    setSelectedLevelOfPolitics(newLevel);
                    if (newLevel === 1) {
                      setSelectedProvinceAndTerritory(0);
                    }
                  }}
                  sx={{
                    color: "white",
                    ".MuiOutlinedInput-notchedOutline": {
                      borderColor: "rgba(255, 255, 255, 0.3)",
                    },
                    "&:hover .MuiOutlinedInput-notchedOutline": {
                      borderColor: "rgba(255, 255, 255, 0.5)",
                    },
                    "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
                      borderColor: "white",
                    },
                    ".MuiSvgIcon-root": { color: "white" },
                  }}
                >
                  {levelsOfPolitics.map((level) => (
                    <MenuItem key={level.id} value={level.id}>
                      {level.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>

              {selectedLevelOfPolitics === 2 &&
                provincesAndTerritories.length > 0 && (
                  <FormControl size="small" sx={{ minWidth: 150 }}>
                    <InputLabel
                      id="province-territory-label"
                      sx={{
                        color: "white",
                        "&.Mui-focused": { color: "white" },
                      }}
                    >
                      Province/Territory
                    </InputLabel>
                    <Select
                      labelId="province-territory-label"
                      label="Province/Territory"
                      value={selectedProvinceAndTerritory}
                      onChange={(e) =>
                        setSelectedProvinceAndTerritory(Number(e.target.value))
                      }
                      sx={{
                        color: "white",
                        ".MuiOutlinedInput-notchedOutline": {
                          borderColor: "rgba(255, 255, 255, 0.3)",
                        },
                        "&:hover .MuiOutlinedInput-notchedOutline": {
                          borderColor: "rgba(255, 255, 255, 0.5)",
                        },
                        "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
                          borderColor: "white",
                        },
                        ".MuiSvgIcon-root": { color: "white" },
                      }}
                    >
                      <MenuItem value={0}>All</MenuItem>
                      {provincesAndTerritories.map((province) => (
                        <MenuItem key={province.id} value={province.id}>
                          {province.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
            </Box>
          )}

          {hasCitizen && isAuthenticated && (
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Typography
                variant="body1"
                sx={{ mr: 2, display: { xs: "none", md: "block" } }}
              >
                Welcome, {user?.name}!
              </Typography>
              <Button
                color="inherit"
                onClick={() => {
                  setHistory([]);
                  setView("policies");
                }}
              >
                Policies
              </Button>
              <Button
                color="inherit"
                onClick={() => navigateTo("politician-search")}
              >
                Politicians
              </Button>
              {canReadPoliticalParties && (
                <Button
                  color="inherit"
                  onClick={() => navigateTo("political-parties")}
                >
                  Parties
                </Button>
              )}
              <Button color="inherit" onClick={() => navigateTo("citizens")}>
                Citizens
              </Button>
              {canVerifyPolitician && (
                <Button
                  color="inherit"
                  onClick={() => navigateTo("verify-politicians")}
                >
                  Verify
                </Button>
              )}

              <Tooltip title="Toggle light/dark mode">
                <IconButton onClick={toggleColorMode} color="inherit">
                  {mode === "dark" ? <Brightness7Icon /> : <Brightness4Icon />}
                </IconButton>
              </Tooltip>

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
                    <Avatar
                      alt={user.name}
                      src={user.picture}
                      sx={{ width: 32, height: 32 }}
                    />
                  ) : (
                    <AccountCircle />
                  )}
                </IconButton>
              </Tooltip>
              <Menu
                id="menu-appbar"
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: "bottom",
                  horizontal: "right",
                }}
                keepMounted
                transformOrigin={{
                  vertical: "top",
                  horizontal: "right",
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <Box sx={{ px: 2, py: 1 }}>
                  <Typography variant="subtitle2">{user?.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {user?.email}
                  </Typography>
                </Box>
                <MenuItem
                  onClick={() => {
                    handleClose();
                    navigateToCitizenProfile(null);
                  }}
                >
                  Profile
                </MenuItem>
                {!self?.postalCodeId && (
                  <MenuItem
                    onClick={() => {
                      handleClose();
                      navigateTo("id-verification");
                    }}
                  >
                    ID Verification
                  </MenuItem>
                )}
                <MenuItem
                  onClick={() => {
                    handleClose();
                    navigateTo("bookmarked-policies");
                  }}
                >
                  Bookmarks
                </MenuItem>
                <MenuItem onClick={handleLogout}>Logout</MenuItem>
              </Menu>
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {hasCitizen &&
        isAuthenticated &&
        !self?.postalCodeId &&
        view !== "id-verification" && (
          <Alert
            severity="warning"
            sx={{
              cursor: "pointer",
              borderRadius: 0,
              "&:hover": {
                backgroundColor: "info.light",
              },
            }}
            onClick={() => navigateTo("id-verification")}
          >
            Verify your identity to unlock the ability to vote on policies.
          </Alert>
        )}

      <Container sx={{ py: 4 }}>{renderView()}</Container>
    </Box>
  );
};

export default App;

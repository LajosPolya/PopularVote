import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  Button, 
  Paper, 
  Box, 
  CircularProgress, 
  Alert, 
  Divider, 
  Grid, 
  Avatar, 
  Chip,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  ListItemButton
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PersonIcon from '@mui/icons-material/Person';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import { CitizenProfile, CitizenSelf, PoliticalParty, Policy } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface ProfileProps {
    citizenId: number | null;
    onBack: () => void;
    onDeclarePolitician: () => void;
    onPolicyClick: (id: number) => void;
    onPartyClick: (id: number) => void;
    politicalParties: Map<number, PoliticalParty>;
}

const Profile: React.FC<ProfileProps> = ({ citizenId, onBack, onDeclarePolitician, onPolicyClick, onPartyClick, politicalParties }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [citizen, setCitizen] = useState<CitizenProfile | CitizenSelf | null>(null);
    const [policies, setPolicies] = useState<Policy[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [policiesLoading, setPoliciesLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const fetchCitizen = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const endpoint = citizenId ? `${popularVoteApiUrl}/citizens/${citizenId}` : `${popularVoteApiUrl}/citizens/self`;
            const response = await fetch(endpoint, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch citizen profile');
            }
            const data: CitizenProfile | CitizenSelf = await response.json();
            setCitizen(data);
            setError(null);
            
            if (data.role !== 'CITIZEN') {
                fetchPolicies(data.id);
            }
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const fetchPolicies = async (id: number) => {
        setPoliciesLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/${id}/policies`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (response.ok) {
                const data: Policy[] = await response.json();
                setPolicies(data);
            }
        } catch (err: any) {
            console.error('Failed to fetch policies:', err);
        } finally {
            setPoliciesLoading(false);
        }
    };

    useEffect(() => {
        fetchCitizen();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [citizenId, getAccessTokenSilently]);


    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
                <CircularProgress />
                <Typography sx={{ ml: 2 }}>Loading profile...</Typography>
            </Box>
        );
    }

    if (error) {
        return (
            <Box sx={{ mt: 2 }}>
                <Button startIcon={<ArrowBackIcon />} onClick={onBack} sx={{ mb: 2 }}>Back</Button>
                <Alert severity="error">{error}</Alert>
            </Box>
        );
    }

    if (!citizen) {
        return (
            <Box sx={{ mt: 2 }}>
                <Button startIcon={<ArrowBackIcon />} onClick={onBack} sx={{ mb: 2 }}>Back</Button>
                <Alert severity="warning">Citizen profile not found.</Alert>
            </Box>
        );
    }

    return (
        <Box sx={{ maxWidth: 800, mx: 'auto' }}>
            <Button 
                startIcon={<ArrowBackIcon />} 
                onClick={onBack} 
                sx={{ mb: 3 }}
            >
                Back
            </Button>

            <Paper elevation={3} sx={{ p: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                    <Avatar sx={{ width: 80, height: 80, bgcolor: 'primary.main', mr: 3 }}>
                        <PersonIcon sx={{ fontSize: 50 }} />
                    </Avatar>
                    <Box>
                        <Typography variant="h4" gutterBottom>
                            {citizen.givenName} {citizen.surname}
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <Chip
                                label={citizen.role.charAt(0) + citizen.role.slice(1).toLowerCase()}
                                color="primary"
                                variant="outlined"
                            />
                            {(() => {
                                const party = citizen.politicalAffiliationId ? politicalParties.get(citizen.politicalAffiliationId) : null;
                                if (!party) return null;

                                return (
                                    <Chip
                                        label={party.displayName || "Unknown Party"}
                                        sx={{ 
                                            bgcolor: party.hexColor || 'grey.500', 
                                            color: 'white',
                                            cursor: 'pointer'
                                        }}
                                        onClick={() => onPartyClick(party.id)}
                                    />
                                );
                            })()}
                            {citizen.levelOfPoliticsName && (
                                <Chip 
                                    label={citizen.levelOfPoliticsName}
                                    color="secondary"
                                    variant="outlined"
                                />
                            )}
                        </Box>
                    </Box>
                </Box>

                <Divider sx={{ mb: 4 }} />

                <Grid container spacing={3}>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <Typography variant="subtitle2" color="text.secondary">Given Name</Typography>
                        <Typography variant="body1" gutterBottom>{citizen.givenName}</Typography>
                        
                        <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Middle Name</Typography>
                        <Typography variant="body1" gutterBottom>{citizen.middleName || 'N/A'}</Typography>
                        
                        <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Surname</Typography>
                        <Typography variant="body1" gutterBottom>{citizen.surname}</Typography>
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                        <Card variant="outlined">
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Statistics</Typography>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                    <Typography variant="body2">Policies Created:</Typography>
                                    <Typography variant="body2" fontWeight="bold">{citizen.policyCount}</Typography>
                                </Box>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <Typography variant="body2">Votes Cast:</Typography>
                                    <Typography variant="body2" fontWeight="bold">{citizen.voteCount}</Typography>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>

                {citizen.role !== 'CITIZEN' && (
                    <Box sx={{ mt: 4 }}>
                        <Typography variant="h6" gutterBottom>Published Policies</Typography>
                        <Divider sx={{ mb: 2 }} />
                        {policiesLoading ? (
                            <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
                                <CircularProgress size={24} />
                            </Box>
                        ) : policies.length > 0 ? (
                            <List disablePadding>
                                {policies.map((policy) => (
                                    <ListItem key={policy.id} disablePadding sx={{ mb: 1 }}>
                                        <ListItemButton 
                                            onClick={() => onPolicyClick(policy.id)}
                                            sx={{ 
                                                border: '1px solid',
                                                borderColor: 'divider',
                                                borderRadius: 1,
                                                '&:hover': {
                                                    borderColor: 'primary.main',
                                                    bgcolor: 'action.hover'
                                                }
                                            }}
                                        >
                                            <ListItemText 
                                                primary={policy.description}
                                                secondary={`Published on ${new Date(policy.creationDate).toLocaleDateString()}`}
                                            />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                        ) : (
                            <Typography variant="body2" color="text.secondary">
                                No policies published yet.
                            </Typography>
                        )}
                    </Box>
                )}

                {!citizenId && citizen && 'isVerificationPending' in citizen && citizen.role === 'CITIZEN' && (
                    <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
                        <Button 
                            variant="contained" 
                            color="success"
                            size="large"
                            startIcon={<VerifiedUserIcon />}
                            onClick={onDeclarePolitician} 
                            disabled={citizen.isVerificationPending}
                        >
                            {citizen.isVerificationPending ? 'Verification Pending' : 'Get Verified as a Politician'}
                        </Button>
                    </Box>
                )}
            </Paper>
        </Box>
    );
}

export default Profile;

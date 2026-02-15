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
  CardContent
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PersonIcon from '@mui/icons-material/Person';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import { affiliations, affiliationColors } from './constants';
import { CitizenProfile, CitizenSelf } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface ProfileProps {
    citizenId: number | null;
    onBack: () => void;
    onDeclarePolitician: () => void;
}

const Profile: React.FC<ProfileProps> = ({ citizenId, onBack, onDeclarePolitician }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [citizen, setCitizen] = useState<CitizenProfile | CitizenSelf | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
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
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
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
                            {citizen.politicalAffiliation && (
                                <Chip
                                    label={affiliations[citizen.politicalAffiliation] || citizen.politicalAffiliation}
                                    sx={{
                                        bgcolor: affiliationColors[citizen.politicalAffiliation] || 'grey.500',
                                        color: 'white'
                                    }}
                                />
                            )}
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

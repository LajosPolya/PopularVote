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

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Profile({ citizenId, onBack }) {
    const { getAccessTokenSilently } = useAuth0();
    const [citizen, setCitizen] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [declaring, setDeclaring] = useState(false);

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
            const data = await response.json();
            setCitizen(data);
            setError(null);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCitizen();
    }, [getAccessTokenSilently, citizenId]);

    const handleDeclarePolitician = async () => {
        setDeclaring(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/self/declare-politician`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.status !== 202) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to declare as politician');
            }

            // Refresh profile data or show success message
            // Since it's now just a declaration, the role won't change immediately in the UI until verified
            alert("Your declaration has been submitted for verification.");
            await fetchCitizen();
        } catch (err) {
            setError(err.message);
        } finally {
            setDeclaring(false);
        }
    };

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
                            <Chip 
                                label={affiliations[citizen.politicalAffiliation] || citizen.politicalAffiliation}
                                sx={{ 
                                    bgcolor: affiliationColors[citizen.politicalAffiliation] || 'grey.500', 
                                    color: 'white' 
                                }}
                            />
                        </Box>
                    </Box>
                </Box>

                <Divider sx={{ mb: 4 }} />

                <Grid container spacing={3}>
                    <Grid item xs={12} md={6}>
                        <Typography variant="subtitle2" color="text.secondary">Given Name</Typography>
                        <Typography variant="body1" gutterBottom>{citizen.givenName}</Typography>
                        
                        <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Middle Name</Typography>
                        <Typography variant="body1" gutterBottom>{citizen.middleName || 'N/A'}</Typography>
                        
                        <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }}>Surname</Typography>
                        <Typography variant="body1" gutterBottom>{citizen.surname}</Typography>
                    </Grid>
                    <Grid item xs={12} md={6}>
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

                {!citizenId && citizen.role === 'CITIZEN' && (
                    <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
                        <Button 
                            variant="contained" 
                            color="success"
                            size="large"
                            startIcon={<VerifiedUserIcon />}
                            onClick={handleDeclarePolitician} 
                            disabled={declaring || citizen.isVerificationPending}
                        >
                            {declaring ? 'Declaring...' : citizen.isVerificationPending ? 'Verification Pending' : 'Get Verified as a Politician'}
                        </Button>
                    </Box>
                )}
            </Paper>
        </Box>
    );
}

export default Profile;

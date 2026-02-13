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
  ListItemIcon
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CircleIcon from '@mui/icons-material/Circle';
import { PoliticalParty } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticalPartyDetailsProps {
    partyId: number | null;
    onBack: () => void;
}

const PoliticalPartyDetails: React.FC<PoliticalPartyDetailsProps> = ({ partyId, onBack }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [party, setParty] = useState<PoliticalParty | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPartyDetails = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/political-parties/${partyId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch political party details');
            }

            const data: PoliticalParty = await response.json();
            setParty(data);
            setError(null);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (partyId) {
            fetchPartyDetails();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [partyId]);

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
                <CircularProgress />
                <Typography variant="h6" sx={{ ml: 2 }}>Loading party details...</Typography>
            </Box>
        );
    }

    if (error || !party) {
        return (
            <Box sx={{ mt: 4 }}>
                <Button startIcon={<ArrowBackIcon />} onClick={onBack} sx={{ mb: 2 }}>
                    Back to Parties
                </Button>
                <Alert severity="error">
                    {error || 'Political party not found'}
                </Alert>
            </Box>
        );
    }

    return (
        <Box>
            <Button startIcon={<ArrowBackIcon />} onClick={onBack} sx={{ mb: 3 }}>
                Back to Parties
            </Button>

            <Paper elevation={3} sx={{ p: 4, mb: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <CircleIcon sx={{ color: party.hexColor, fontSize: 40, mr: 2 }} />
                    <Typography variant="h4" component="h1">
                        {party.displayName}
                    </Typography>
                </Box>
                
                <Divider sx={{ my: 3 }} />

                <Typography variant="h6" gutterBottom>
                    Description
                </Typography>
                <Typography variant="body1" paragraph sx={{ fontSize: '1.1rem', lineHeight: 1.6 }}>
                    {party.description || 'No description available for this party.'}
                </Typography>
            </Paper>
        </Box>
    );
};

export default PoliticalPartyDetails;

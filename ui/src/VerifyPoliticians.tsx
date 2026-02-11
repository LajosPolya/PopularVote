import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  Button, 
  List, 
  ListItem, 
  ListItemText, 
  Paper, 
  Box, 
  CircularProgress, 
  Alert,
  Divider,
  ListItemButton,
  Chip
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { affiliations, affiliationColors } from './constants';
import { Citizen } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface VerifyPoliticiansProps {
    onCitizenClick: (id: number) => void;
}

const VerifyPoliticians: React.FC<VerifyPoliticiansProps> = ({ onCitizenClick }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [verifications, setVerifications] = useState<Citizen[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [verifyingId, setVerifyingId] = useState<number | null>(null);

    const fetchVerifications = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/verify-politician`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch politician verifications');
            }
            const data: Citizen[] = await response.json();
            setVerifications(data);
            setError(null);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchVerifications();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleVerify = async (e: React.MouseEvent, id: number) => {
        e.stopPropagation();
        setVerifyingId(id);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/${id}/verify-politician`, {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to verify politician');
            }

            alert("Politician verified successfully!");
            fetchVerifications();
        } catch (err: any) {
            setError(err.message);
        } finally {
            setVerifyingId(null);
        }
    };

    return (
        <Box>
            <Typography variant="h4" component="h2" sx={{ mb: 3 }}>
                Verify Politicians
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {loading && verifications.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                    <Typography sx={{ ml: 2 }}>Loading verifications...</Typography>
                </Box>
            ) : (
                <Paper elevation={2}>
                    <List sx={{ p: 0 }}>
                        {verifications.map((citizen, index) => (
                            <React.Fragment key={citizen.id}>
                                {index > 0 && <Divider />}
                                <ListItem
                                    disablePadding
                                    secondaryAction={
                                        <Button 
                                            variant="contained" 
                                            color="success"
                                            startIcon={<CheckCircleIcon />}
                                            onClick={(e) => handleVerify(e, citizen.id)}
                                            disabled={verifyingId === citizen.id}
                                            size="small"
                                        >
                                            {verifyingId === citizen.id ? 'Verifying...' : 'Verify'}
                                        </Button>
                                    }
                                >
                                    <ListItemButton onClick={() => onCitizenClick && onCitizenClick(citizen.id)}>
                                        <ListItemText 
                                            primary={`${citizen.givenName} ${citizen.surname}`}
                                            secondary={
                                                <Box component="span" sx={{ display: 'flex', alignItems: 'center', mt: 0.5 }}>
                                                    <Chip 
                                                        label={affiliations[citizen.politicalAffiliation] || citizen.politicalAffiliation}
                                                        size="small"
                                                        sx={{ 
                                                            bgcolor: affiliationColors[citizen.politicalAffiliation] || 'grey.300',
                                                            color: 'white',
                                                            fontWeight: 'bold'
                                                        }}
                                                    />
                                                </Box>
                                            }
                                        />
                                    </ListItemButton>
                                </ListItem>
                            </React.Fragment>
                        ))}
                    </List>
                </Paper>
            )}
            
            {verifications.length === 0 && !loading && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    No pending verifications found.
                </Typography>
            )}
        </Box>
    );
}

export default VerifyPoliticians;

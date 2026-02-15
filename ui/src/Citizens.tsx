import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
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
import { Citizen, getFullName, PoliticalParty } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface CitizensProps {
    onCitizenClick: (id: number) => void;
    politicalParties: Map<number, PoliticalParty>
}

const Citizens: React.FC<CitizensProps> = ({ onCitizenClick, politicalParties }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [citizens, setCitizens] = useState<Citizen[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);


    const fetchCitizens = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch citizens');
            }
            const data: Citizen[] = await response.json();
            setCitizens(data);
            setError(null);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCitizens();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <Box>
            <Typography variant="h4" component="h2" sx={{ mb: 3 }}>
                Citizens
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {loading && citizens.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                    <Typography sx={{ ml: 2 }}>Loading citizens...</Typography>
                </Box>
            ) : (
                <Paper elevation={2}>
                    <List sx={{ p: 0 }}>
                        {citizens.map((citizen, index) => (
                            <React.Fragment key={citizen.id}>
                                {index > 0 && <Divider />}
                                <ListItem disablePadding>
                                    <ListItemButton onClick={() => onCitizenClick && onCitizenClick(citizen.id)}>
                                        <ListItemText 
                                            primary={getFullName(citizen)}
                                            secondary={
                                                <Box component="span" sx={{ display: 'flex', alignItems: 'center', mt: 0.5, gap: 1 }}>
                                                    <Chip
                                                        label={citizen.role.charAt(0) + citizen.role.slice(1).toLowerCase()}
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                    {(() => {
                                                        const party = citizen.politicalAffiliationId ? politicalParties.get(citizen.politicalAffiliationId) : null;
                                                        if (!party) return null;

                                                        return (
                                                            <Chip
                                                                label={party.displayName || "Unknown Party"}
                                                                size="small"
                                                                sx={{
                                                                    bgcolor: party.hexColor || 'grey.500',
                                                                    color: 'white',
                                                                    fontWeight: 'bold'
                                                                }}
                                                            />
                                                        );
                                                    })()}
                                                </Box>
                                            }
                                            secondaryTypographyProps={{ component: 'div' }}
                                        />
                                    </ListItemButton>
                                </ListItem>
                            </React.Fragment>
                        ))}
                    </List>
                </Paper>
            )}
            
            {citizens.length === 0 && !loading && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    No citizens found.
                </Typography>
            )}
        </Box>
    );
}

export default Citizens;

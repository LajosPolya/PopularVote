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
  Chip,
  Avatar
} from '@mui/material';
import { affiliations, affiliationColors } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

function Citizens({ onCitizenClick }) {
    const { getAccessTokenSilently } = useAuth0();
    const [citizens, setCitizens] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);


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
            const data = await response.json();
            setCitizens(data);
            setError(null);
        } catch (err) {
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
                                            primary={`${citizen.givenName} ${citizen.surname}`}
                                            secondary={
                                                <Box component="span" sx={{ display: 'flex', alignItems: 'center', mt: 0.5, gap: 1 }}>
                                                    <Chip 
                                                        label={citizen.role.charAt(0) + citizen.role.slice(1).toLowerCase()} 
                                                        size="small" 
                                                        variant="outlined" 
                                                    />
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
            
            {citizens.length === 0 && !loading && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    No citizens found.
                </Typography>
            )}
        </Box>
    );
}

export default Citizens;

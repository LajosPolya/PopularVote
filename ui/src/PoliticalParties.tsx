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
  ListItemIcon,
  ListItemButton,
  Button
} from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import AddIcon from '@mui/icons-material/Add';
import { PoliticalParty } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticalPartiesProps {
    onPartyClick?: (id: number) => void;
    canCreateParty?: boolean;
    onCreateParty?: () => void;
    levelOfPoliticsId?: number | null;
}

const PoliticalParties: React.FC<PoliticalPartiesProps> = ({ onPartyClick, canCreateParty, onCreateParty, levelOfPoliticsId }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [parties, setParties] = useState<PoliticalParty[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const fetchParties = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const queryParams = levelOfPoliticsId ? `?levelOfPolitics=${levelOfPoliticsId}` : '';
            const response = await fetch(`${popularVoteApiUrl}/political-parties${queryParams}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch political parties');
            }
            const data: PoliticalParty[] = await response.json();
            setParties(data);
            setError(null);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchParties();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [levelOfPoliticsId]);

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h4" component="h2">
                    Political Parties
                </Typography>
                {canCreateParty && (
                    <Button 
                        variant="contained" 
                        startIcon={<AddIcon />} 
                        onClick={onCreateParty}
                    >
                        Create Party
                    </Button>
                )}
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {loading && parties.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                    <Typography sx={{ ml: 2 }}>Loading political parties...</Typography>
                </Box>
            ) : (
                <Paper elevation={2}>
                    <List sx={{ p: 0 }}>
                        {parties.map((party, index) => (
                            <React.Fragment key={party.id}>
                                {index > 0 && <Divider />}
                                <ListItem disablePadding>
                                    <ListItemButton onClick={() => onPartyClick && onPartyClick(party.id)}>
                                        <ListItemIcon>
                                            <CircleIcon sx={{ color: party.hexColor }} />
                                        </ListItemIcon>
                                        <ListItemText 
                                            primary={party.displayName}
                                            secondary={party.description}
                                        />
                                    </ListItemButton>
                                </ListItem>
                            </React.Fragment>
                        ))}
                    </List>
                </Paper>
            )}
            
            {parties.length === 0 && !loading && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    No political parties found.
                </Typography>
            )}
        </Box>
    );
}

export default PoliticalParties;

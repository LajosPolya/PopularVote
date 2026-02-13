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
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  Avatar,
  Stack
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import CircleIcon from '@mui/icons-material/Circle';
import PersonIcon from '@mui/icons-material/Person';
import PolicyIcon from '@mui/icons-material/Policy';
import { PoliticalParty, Citizen, getFullName, Policy } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticalPartyDetailsProps {
    partyId: number | null;
    onBack: () => void;
    onCitizenClick?: (id: number) => void;
    onPolicyClick?: (id: number) => void;
}

const PoliticalPartyDetails: React.FC<PoliticalPartyDetailsProps> = ({ partyId, onBack, onCitizenClick, onPolicyClick }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [party, setParty] = useState<PoliticalParty | null>(null);
    const [members, setMembers] = useState<Citizen[]>([]);
    const [policies, setPolicies] = useState<Policy[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [loadingMembers, setLoadingMembers] = useState<boolean>(false);
    const [loadingPolicies, setLoadingPolicies] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [membersError, setMembersError] = useState<string | null>(null);
    const [policiesError, setPoliciesError] = useState<string | null>(null);

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

    const fetchPartyMembers = async () => {
        setLoadingMembers(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/political-parties/${partyId}/members`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch party members');
            }

            const data: Citizen[] = await response.json();
            setMembers(data);
            setMembersError(null);
        } catch (err: any) {
            setMembersError(err.message);
        } finally {
            setLoadingMembers(false);
        }
    };

    const fetchPartyPolicies = async () => {
        setLoadingPolicies(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/political-parties/${partyId}/policies`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch party policies');
            }

            const data: Policy[] = await response.json();
            setPolicies(data);
            setPoliciesError(null);
        } catch (err: any) {
            setPoliciesError(err.message);
        } finally {
            setLoadingPolicies(false);
        }
    };

    useEffect(() => {
        if (partyId) {
            fetchPartyDetails();
            fetchPartyMembers();
            fetchPartyPolicies();
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

                <Divider sx={{ my: 3 }} />

                <Stack direction={{ xs: 'column', md: 'row' }} spacing={4}>
                    <Box sx={{ flex: 1 }}>
                        <Typography variant="h6" gutterBottom>
                            Party Members
                        </Typography>

                        {loadingMembers ? (
                            <Box sx={{ display: 'flex', alignItems: 'center', py: 2 }}>
                                <CircularProgress size={24} />
                                <Typography sx={{ ml: 2 }}>Loading members...</Typography>
                            </Box>
                        ) : membersError ? (
                            <Alert severity="error">{membersError}</Alert>
                        ) : members.length === 0 ? (
                            <Typography variant="body2" color="text.secondary">
                                No affiliated politicians found for this party.
                            </Typography>
                        ) : (
                            <List sx={{ width: '100%', bgcolor: 'background.paper', borderRadius: 1 }}>
                                {members.map((member, index) => (
                                    <React.Fragment key={member.id}>
                                        {index > 0 && <Divider variant="inset" component="li" />}
                                        <ListItem disablePadding>
                                            <ListItemButton onClick={() => onCitizenClick && onCitizenClick(member.id)}>
                                                <Box sx={{ mr: 2 }}>
                                                    <Avatar sx={{ bgcolor: party.hexColor }}>
                                                        <PersonIcon />
                                                    </Avatar>
                                                </Box>
                                                <ListItemText 
                                                    primary={getFullName(member)}
                                                    secondary={member.role.charAt(0).toUpperCase() + member.role.slice(1).toLowerCase()}
                                                />
                                            </ListItemButton>
                                        </ListItem>
                                    </React.Fragment>
                                ))}
                            </List>
                        )}
                    </Box>

                    <Box sx={{ flex: 1 }}>
                        <Typography variant="h6" gutterBottom>
                            Published Policies
                        </Typography>

                        {loadingPolicies ? (
                            <Box sx={{ display: 'flex', alignItems: 'center', py: 2 }}>
                                <CircularProgress size={24} />
                                <Typography sx={{ ml: 2 }}>Loading policies...</Typography>
                            </Box>
                        ) : policiesError ? (
                            <Alert severity="error">{policiesError}</Alert>
                        ) : policies.length === 0 ? (
                            <Typography variant="body2" color="text.secondary">
                                No policies published by this party yet.
                            </Typography>
                        ) : (
                            <List sx={{ width: '100%', bgcolor: 'background.paper', borderRadius: 1 }}>
                                {policies.map((policy, index) => (
                                    <React.Fragment key={policy.id}>
                                        {index > 0 && <Divider variant="inset" component="li" />}
                                        <ListItem disablePadding>
                                            <ListItemButton onClick={() => onPolicyClick && onPolicyClick(policy.id)}>
                                                <Box sx={{ mr: 2 }}>
                                                    <Avatar sx={{ bgcolor: 'primary.light' }}>
                                                        <PolicyIcon />
                                                    </Avatar>
                                                </Box>
                                                <ListItemText 
                                                    primary={policy.description.length > 100 ? policy.description.substring(0, 100) + '...' : policy.description}
                                                    secondary={`By ${policy.publisherName}`}
                                                />
                                            </ListItemButton>
                                        </ListItem>
                                    </React.Fragment>
                                ))}
                            </List>
                        )}
                    </Box>
                </Stack>
            </Paper>
        </Box>
    );
};

export default PoliticalPartyDetails;

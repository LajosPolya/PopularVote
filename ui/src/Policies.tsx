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
  IconButton,
  Tooltip
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import { Policy } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliciesProps {
    onPolicyClick: (id: number) => void;
    onCitizenClick: (id: number) => void;
    onCreatePolicy: () => void;
    levelOfPoliticsId: number | null;
}

const Policies: React.FC<PoliciesProps> = ({ onPolicyClick, onCitizenClick, onCreatePolicy, levelOfPoliticsId }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [policies, setPolicies] = useState<Policy[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [canCreatePolicy, setCanCreatePolicy] = useState<boolean>(false);
    const [bookmarkingId, setBookmarkingId] = useState<number | null>(null);

    const checkPermissions = async () => {
        try {
            const token = await getAccessTokenSilently();
            const payload = JSON.parse(atob(token.split('.')[1] || ''));
            const permissions = payload.scope?.split(' ') || [];
            setCanCreatePolicy(permissions.includes('write:policies'));
        } catch (err) {
            console.error("Error checking permissions:", err);
            setCanCreatePolicy(false);
        }
    };

    const fetchPolicies = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const queryParams = levelOfPoliticsId ? `?levelOfPolitics=${levelOfPoliticsId}` : '';
            const response = await fetch(`${popularVoteApiUrl}/policies${queryParams}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch policies');
            }
            const data: Policy[] = await response.json();
            setPolicies(data);
            setError(null);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleToggleBookmark = async (e: React.MouseEvent, policy: Policy) => {
        e.stopPropagation();
        if (bookmarkingId) return;
        
        setBookmarkingId(policy.id);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/policies/${policy.id}/bookmark`, {
                method: policy.isBookmarked ? 'DELETE' : 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setPolicies(prevPolicies => 
                    prevPolicies.map(p => 
                        p.id === policy.id ? { ...p, isBookmarked: !p.isBookmarked } : p
                    )
                );
            } else {
                console.error(`Failed to ${policy.isBookmarked ? 'unbookmark' : 'bookmark'} policy`);
            }
        } catch (err) {
            console.error(`Error toggling bookmark:`, err);
        } finally {
            setBookmarkingId(null);
        }
    };

    useEffect(() => {
        checkPermissions();
        fetchPolicies();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [levelOfPoliticsId]);

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h4" component="h2">
                    Policies
                </Typography>
                {canCreatePolicy && (
                    <Button 
                        variant="contained" 
                        startIcon={<AddIcon />} 
                        onClick={onCreatePolicy}
                    >
                        Create Policy
                    </Button>
                )}
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {loading && policies.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                    <Typography sx={{ ml: 2 }}>Loading policies...</Typography>
                </Box>
            ) : (
                <Paper elevation={2}>
                    <List sx={{ p: 0 }}>
                        {policies.map((policy, index) => (
                            <React.Fragment key={policy.id}>
                                {index > 0 && <Divider />}
                                <ListItem disablePadding>
                                    <ListItemButton onClick={() => onPolicyClick && onPolicyClick(policy.id)}>
                                        <ListItemText 
                                            primary={
                                                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                                                    <Typography variant="body1" fontWeight="medium">
                                                        {policy.description}
                                                    </Typography>
                                                    <Tooltip title={policy.isBookmarked ? "Remove Bookmark" : "Bookmark this policy"}>
                                                        <IconButton 
                                                            onClick={(e) => handleToggleBookmark(e, policy)}
                                                            color="primary"
                                                            size="small"
                                                            disabled={bookmarkingId === policy.id}
                                                            sx={{ ml: 1 }}
                                                        >
                                                            {policy.isBookmarked ? (
                                                                <BookmarkIcon fontSize="small" />
                                                            ) : (
                                                                <BookmarkBorderIcon fontSize="small" />
                                                            )}
                                                        </IconButton>
                                                    </Tooltip>
                                                </Box>
                                            }
                                            secondary={
                                                <span>
                                                    Published by{' '}
                                                    <Box
                                                        component="span"
                                                        sx={{ 
                                                            cursor: 'pointer', 
                                                            color: 'primary.main',
                                                            '&:hover': { textDecoration: 'underline' }
                                                        }}
                                                        onClick={(e) => {
                                                            e.stopPropagation();
                                                            onCitizenClick(policy.publisherCitizenId);
                                                        }}
                                                    >
                                                        {policy.publisherName}
                                                    </Box>
                                                </span>
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
            
            {policies.length === 0 && !loading && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    No policies found.
                </Typography>
            )}
        </Box>
    );
}

export default Policies;

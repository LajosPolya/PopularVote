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
  Chip,
  Stack
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AddCommentIcon from '@mui/icons-material/AddComment';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import RemoveCircleIcon from '@mui/icons-material/RemoveCircle';
import { affiliations, affiliationColors } from './constants';
import { PolicyDetails as PolicyDetailsType } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PolicyDetailsProps {
    policyId: number | null;
    onBack: () => void;
    onCreateOpinion: () => void;
}

const PolicyDetails: React.FC<PolicyDetailsProps> = ({ policyId, onBack, onCreateOpinion }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [policy, setPolicy] = useState<PolicyDetailsType | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [voting, setVoting] = useState<boolean>(false);
    const [hasVoted, setHasVoted] = useState<boolean>(false);
    const [voteMessage, setVoteMessage] = useState<string | null>(null);


    const checkHasVoted = async () => {
        try {
            const token = await getAccessTokenSilently();
            const headers = {
                Authorization: `Bearer ${token}`,
            };
            const response = await fetch(`${popularVoteApiUrl}/votes/policies/${policyId}/has-voted`, { headers });
            if (response.ok) {
                const alreadyVoted = await response.json();
                setHasVoted(alreadyVoted);
            }
        } catch (err) {
            console.error('Failed to check if user has voted:', err);
        }
    };

    const fetchPolicyDetails = async () => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const headers = {
                Authorization: `Bearer ${token}`,
            };
            const response = await fetch(`${popularVoteApiUrl}/policies/${policyId}/details`, { headers });

            if (!response.ok) throw new Error('Failed to fetch policy details');

            const data: PolicyDetailsType = await response.json();

            setPolicy(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (policyId) {
            fetchPolicyDetails();
            checkHasVoted();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [policyId]);

    const handleVote = async (selectionId: number) => {
        setVoting(true);
        setVoteMessage(null);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/votes`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    policyId: policyId,
                    selectionId: selectionId
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to cast vote');
            }

            setVoteMessage('Vote cast successfully!');
            setHasVoted(true);
        } catch (err: any) {
            setVoteMessage(`Error: ${err.message}`);
        } finally {
            setVoting(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
                <CircularProgress />
                <Typography sx={{ ml: 2 }}>Loading policy details...</Typography>
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

    if (!policy) {
        return (
            <Box sx={{ mt: 2 }}>
                <Button startIcon={<ArrowBackIcon />} onClick={onBack} sx={{ mb: 2 }}>Back</Button>
                <Alert severity="warning">Policy not found.</Alert>
            </Box>
        );
    }

    return (
        <Box sx={{ maxWidth: 900, mx: 'auto' }}>
            <Button 
                startIcon={<ArrowBackIcon />} 
                onClick={onBack} 
                sx={{ mb: 3 }}
            >
                Back to Policies
            </Button>
            
            <Paper elevation={3} sx={{ p: 4, mb: 4 }}>
                <Typography variant="h4" gutterBottom>Policy Details</Typography>
                
                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                    <Typography variant="subtitle1"><strong>Author:</strong> {policy.publisherName}</Typography>
                    <Chip 
                        label={affiliations[policy.publisherPoliticalAffiliation] || policy.publisherPoliticalAffiliation}
                        size="small"
                        sx={{ 
                            bgcolor: affiliationColors[policy.publisherPoliticalAffiliation] || 'grey.500', 
                            color: 'white',
                            fontWeight: 'bold'
                        }}
                    />
                </Box>

                <Typography variant="body1" paragraph sx={{ fontSize: '1.1rem', lineHeight: 1.6 }}>
                    {policy.description}
                </Typography>

                <Divider sx={{ my: 3 }} />

                <Button 
                    variant="outlined" 
                    startIcon={<AddCommentIcon />}
                    onClick={onCreateOpinion}
                >
                    Create Opinion for this Policy
                </Button>
            </Paper>

            <Paper elevation={3} sx={{ p: 4, mb: 4, bgcolor: 'background.default', border: '1px solid', borderColor: 'divider' }}>
                <Typography variant="h5" gutterBottom>
                    {hasVoted ? 'Your Vote' : 'Cast Your Vote'}
                </Typography>
                
                {hasVoted && (
                    <Alert severity="success" sx={{ mb: 3 }}>
                        You have already voted on this policy.
                    </Alert>
                )}

                <Stack direction="row" spacing={2} sx={{ mt: 2 }}>
                    <Button 
                        variant="contained" 
                        color="success"
                        startIcon={<ThumbUpIcon />}
                        disabled={voting || hasVoted}
                        onClick={() => handleVote(1)}
                        sx={{ flexGrow: 1 }}
                    >
                        Approve
                    </Button>
                    <Button 
                        variant="contained" 
                        color="error"
                        startIcon={<ThumbDownIcon />}
                        disabled={voting || hasVoted}
                        onClick={() => handleVote(2)}
                        sx={{ flexGrow: 1 }}
                    >
                        Disapprove
                    </Button>
                    <Button 
                        variant="contained" 
                        color="inherit"
                        startIcon={<RemoveCircleIcon />}
                        disabled={voting || hasVoted}
                        onClick={() => handleVote(3)}
                        sx={{ flexGrow: 1 }}
                    >
                        Abstain
                    </Button>
                </Stack>

                {voteMessage && (
                    <Alert 
                        severity={voteMessage.startsWith('Error') ? 'error' : 'success'} 
                        sx={{ mt: 3 }}
                    >
                        {voteMessage}
                    </Alert>
                )}
            </Paper>

            <Typography variant="h5" gutterBottom sx={{ mt: 4, mb: 2 }}>
                Opinions ({policy.opinions.length})
            </Typography>

            {policy.opinions.length === 0 ? (
                <Paper sx={{ p: 3, textAlign: 'center', bgcolor: 'grey.50' }}>
                    <Typography color="text.secondary">No opinions yet. Be the first to share your thoughts!</Typography>
                </Paper>
            ) : (
                <Stack spacing={2}>
                    {policy.opinions.map((opinion) => (
                        <Paper key={opinion.id} elevation={1} sx={{ p: 3 }}>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Typography variant="subtitle2" fontWeight="bold">
                                        {opinion.authorName}
                                    </Typography>
                                    <Chip 
                                        label={affiliations[opinion.authorPoliticalAffiliation] || opinion.authorPoliticalAffiliation}
                                        size="small"
                                        variant="outlined"
                                        sx={{ 
                                            height: 20, 
                                            fontSize: '0.7rem',
                                            borderColor: affiliationColors[opinion.authorPoliticalAffiliation] || 'grey.500',
                                            color: affiliationColors[opinion.authorPoliticalAffiliation] || 'grey.500'
                                        }}
                                    />
                                </Box>
                            </Box>
                            <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                                {opinion.description}
                            </Typography>
                        </Paper>
                    ))}
                </Stack>
            )}
        </Box>
    );
}

export default PolicyDetails;

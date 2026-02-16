import dayjs, { Dayjs } from 'dayjs';
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
  Stack,
  IconButton,
  Tooltip
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import AddCommentIcon from '@mui/icons-material/AddComment';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import RemoveCircleIcon from '@mui/icons-material/RemoveCircle';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import TimerOutlinedIcon from '@mui/icons-material/TimerOutlined';
import TimerOffOutlinedIcon from '@mui/icons-material/TimerOffOutlined';
import {PolicyDetails as PolicyDetailsType, Policy, getFullName, OpinionLikeCount, PoliticalParty} from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PolicyDetailsProps {
    policyId: number | null;
    onBack: () => void;
    onCitizenClick: (id: number) => void;
    onCreateOpinion: () => void;
    politicalParties: Map<number, PoliticalParty>
}

const PolicyDetails: React.FC<PolicyDetailsProps> = ({ policyId, onBack, onCitizenClick, onCreateOpinion, politicalParties }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [policy, setPolicy] = useState<PolicyDetailsType | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [voting, setVoting] = useState<boolean>(false);
    const [hasVoted, setHasVoted] = useState<boolean>(false);
    const [voteMessage, setVoteMessage] = useState<string | null>(null);
    const [isBookmarked, setIsBookmarked] = useState<boolean>(false);
    const [bookmarking, setBookmarking] = useState<boolean>(false);
    const [likedOpinionIds, setLikedOpinionIds] = useState<Set<number>>(new Set());
    const [likeCounts, setLikeCounts] = useState<Record<number, number>>({});
    const [togglingLikeId, setTogglingLikeId] = useState<number | null>(null);


    const checkIsBookmarked = async () => {
        try {
            const token = await getAccessTokenSilently();
            const headers = {
                Authorization: `Bearer ${token}`,
            };
            const response = await fetch(`${popularVoteApiUrl}/policies/${policyId}/is-bookmarked`, { headers });
            if (response.ok) {
                const alreadyBookmarked = await response.json();
                setIsBookmarked(alreadyBookmarked);
            }
        } catch (err) {
            console.error('Failed to check if policy is bookmarked:', err);
        }
    };

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

    const fetchLikedOpinions = async () => {
        try {
            const token = await getAccessTokenSilently();
            const headers = {
                Authorization: `Bearer ${token}`,
            };
            const response = await fetch(`${popularVoteApiUrl}/citizens/self/liked-opinions`, { headers });
            if (response.ok) {
                const likedIds: number[] = await response.json();
                setLikedOpinionIds(new Set(likedIds));
            }
        } catch (err) {
            console.error('Failed to fetch liked opinions:', err);
        }
    };

    const fetchLikeCounts = async (opinionIds: number[]) => {
        if (opinionIds.length === 0) return;
        try {
            const token = await getAccessTokenSilently();
            const headers = {
                Authorization: `Bearer ${token}`,
            };
            const queryParams = opinionIds.map(id => `opinionIds=${id}`).join('&');
            const response = await fetch(`${popularVoteApiUrl}/opinions/likes/count?${queryParams}`, { headers });
            if (response.ok) {
                const counts: OpinionLikeCount[] = await response.json();
                const countsMap: Record<number, number> = {};
                counts.forEach(c => {
                    countsMap[c.opinionId] = c.likeCount;
                });
                setLikeCounts(prev => ({ ...prev, ...countsMap }));
            }
        } catch (err) {
            console.error('Failed to fetch like counts:', err);
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
            checkIsBookmarked();
            fetchLikedOpinions();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [policyId]);

    useEffect(() => {
        if (policy?.opinions && policy.opinions.length > 0) {
            const opinionIds = policy.opinions.map(o => o.id);
            fetchLikeCounts(opinionIds);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [policy]);

    const handleBookmark = async () => {
        if (bookmarking) return;
        setBookmarking(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/policies/${policyId}/bookmark`, {
                method: isBookmarked ? 'DELETE' : 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setIsBookmarked(!isBookmarked);
            } else {
                console.error(`Failed to ${isBookmarked ? 'unbookmark' : 'bookmark'} policy`);
            }
        } catch (err) {
            console.error(`Error ${isBookmarked ? 'unbookmarking' : 'bookmarking'} policy:`, err);
        } finally {
            setBookmarking(false);
        }
    };

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

    const handleToggleLike = async (opinionId: number) => {
        if (togglingLikeId !== null) return;
        setTogglingLikeId(opinionId);
        
        const isLiked = likedOpinionIds.has(opinionId);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/opinions/${opinionId}/like`, {
                method: isLiked ? 'DELETE' : 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setLikedOpinionIds(prev => {
                    const newSet = new Set(prev);
                    if (isLiked) {
                        newSet.delete(opinionId);
                    } else {
                        newSet.add(opinionId);
                    }
                    return newSet;
                });
                setLikeCounts(prev => {
                    const currentCount = prev[opinionId] || 0;
                    return {
                        ...prev,
                        [opinionId]: isLiked ? Math.max(0, currentCount - 1) : currentCount + 1
                    };
                });
            } else {
                console.error(`Failed to ${isLiked ? 'unlike' : 'like'} opinion`);
            }
        } catch (err) {
            console.error(`Error toggling like for opinion ${opinionId}:`, err);
        } finally {
            setTogglingLikeId(null);
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
            
            <Paper elevation={3} sx={{ p: 4, mb: 4, position: 'relative' }}>
                <Box sx={{ position: 'absolute', top: 16, right: 16 }}>
                    <Tooltip title={dayjs().isAfter(dayjs(policy.closeDate)) ? "Voting Closed" : "Voting Open"}>
                        <IconButton color="primary" sx={{ ml: 1 }}>
                            {dayjs().isAfter(dayjs(policy.closeDate)) ? (
                                <TimerOffOutlinedIcon fontSize="medium" />
                            ) : (
                                <TimerOutlinedIcon fontSize="medium" />
                            )}
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={isBookmarked ? "Remove Bookmark" : "Bookmark this policy"}>
                        <span>
                            <IconButton 
                                onClick={handleBookmark} 
                                color="primary" 
                                disabled={bookmarking}
                                size="large"
                            >
                                {isBookmarked ? <BookmarkIcon fontSize="inherit" /> : <BookmarkBorderIcon fontSize="inherit" />}
                            </IconButton>
                        </span>
                    </Tooltip>
                </Box>
                <Typography variant="h4" gutterBottom>Policy Details</Typography>
                
                <Box sx={{ mb: 1, display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                    <Typography variant="subtitle1">
                        <strong>Publisher:</strong>{' '}
                        <Box
                            component="span"
                            sx={{ 
                                cursor: 'pointer', 
                                color: 'primary.main',
                                '&:hover': { textDecoration: 'underline' }
                            }}
                            onClick={() => onCitizenClick(policy.publisherCitizenId)}
                        >
                            {policy.publisherName}
                        </Box>
                    </Typography>
                    {(() => {
                        const party = policy.publisherPoliticalAffiliationId ? politicalParties.get(policy.publisherPoliticalAffiliationId) : null;
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
                <Typography variant="subtitle1">
                    <strong>Close date:</strong>{' '}
                    <Box
                        component="span"
                    >
                        {dayjs(policy.closeDate).format('MMMM D, YYYY HH:mm:ss ')}
                    </Box>
                </Typography>

                {policy.coAuthorCitizens && policy.coAuthorCitizens.length > 0 && (
                    <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
                        <Typography variant="subtitle1"><strong>Co-Authors:</strong></Typography>
                        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                            {policy.coAuthorCitizens.map(author => (
                                <Chip 
                                    key={author.id}
                                    label={getFullName(author)}
                                    variant="outlined"
                                    size="small"
                                    onClick={() => onCitizenClick(author.id)}
                                    sx={{ cursor: 'pointer' }}
                                />
                            ))}
                        </Stack>
                    </Box>
                )}

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
                        disabled={voting || hasVoted || dayjs().isAfter(dayjs(policy.closeDate))}
                        onClick={() => handleVote(1)}
                        sx={{ flexGrow: 1 }}
                    >
                        Approve
                    </Button>
                    <Button 
                        variant="contained" 
                        color="error"
                        startIcon={<ThumbDownIcon />}
                        disabled={voting || hasVoted || dayjs().isAfter(dayjs(policy.closeDate))}
                        onClick={() => handleVote(2)}
                        sx={{ flexGrow: 1 }}
                    >
                        Disapprove
                    </Button>
                    <Button 
                        variant="contained" 
                        color="inherit"
                        startIcon={<RemoveCircleIcon />}
                        disabled={voting || hasVoted || dayjs().isAfter(dayjs(policy.closeDate))}
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
                        <Paper key={opinion.id} elevation={1} sx={{ p: 3, position: 'relative' }}>
                            <Box sx={{ position: 'absolute', top: 8, right: 8, display: 'flex', alignItems: 'center' }}>
                                <Typography variant="caption" color="text.secondary" sx={{ mr: 0.5 }}>
                                    {likeCounts[opinion.id] || 0}
                                </Typography>
                                <Tooltip title={likedOpinionIds.has(opinion.id) ? "Unlike" : "Like"}>
                                    <IconButton 
                                        onClick={() => handleToggleLike(opinion.id)} 
                                        color="secondary"
                                        disabled={togglingLikeId === opinion.id}
                                        size="small"
                                    >
                                        {likedOpinionIds.has(opinion.id) ? <FavoriteIcon fontSize="small" /> : <FavoriteBorderIcon fontSize="small" />}
                                    </IconButton>
                                </Tooltip>
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Typography 
                                        variant="subtitle2" 
                                        fontWeight="bold"
                                        sx={{ 
                                            cursor: 'pointer', 
                                            color: 'primary.main',
                                            '&:hover': { textDecoration: 'underline' }
                                        }}
                                        onClick={() => onCitizenClick(opinion.authorId)}
                                    >
                                        {opinion.authorName}
                                    </Typography>
                                    {(() => {
                                        const party = opinion.authorPoliticalAffiliationId ? politicalParties.get(opinion.authorPoliticalAffiliationId) : null;
                                        if (!party) return null;

                                        return (
                                            <Chip
                                                label={party.displayName || "Unknown Party"}
                                                size="small"
                                                variant="outlined"
                                                sx={{
                                                    borderColor: party.hexColor || 'grey.500',
                                                    color: party.hexColor || 'grey.500'
                                                }}
                                            />
                                        );
                                    })()}
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

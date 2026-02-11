import React, { useState, useEffect, useRef } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  Button, 
  TextField, 
  Box, 
  Alert, 
  Paper,
  CircularProgress,
  Divider,
  Stack
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { Citizen, Policy } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface CreateOpinionProps {
    initialPolicyId: number | null;
    onBack: () => void;
}

const CreateOpinion: React.FC<CreateOpinionProps> = ({ initialPolicyId, onBack }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [selectedPolicyId, setSelectedPolicyId] = useState<number | string>(initialPolicyId || '');
    const [policy, setPolicy] = useState<Policy | null>(null);
    const [description, setDescription] = useState<string>('');
    const [citizen, setCitizen] = useState<Citizen | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<boolean>(false);
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    useEffect(() => {
        if (textareaRef.current) {
            textareaRef.current.focus();
        }
    }, []);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                const token = await getAccessTokenSilently();
                const headers = {
                    Authorization: `Bearer ${token}`,
                };

                const requests = [];
                if (initialPolicyId) {
                    requests.push(fetch(`${popularVoteApiUrl}/policies/${initialPolicyId}`, { headers }));
                }
                requests.push(fetch(`${popularVoteApiUrl}/citizens/self`, { headers }));

                const responses = await Promise.all(requests);
                
                let policyData: Policy | null = null;
                let citizenData: Citizen | null = null;

                if (initialPolicyId) {
                    const policyRes = responses[0];
                    const citizenRes = responses[1];
                    if (!policyRes || !policyRes.ok) throw new Error('Failed to fetch policy');
                    if (!citizenRes || !citizenRes.ok) throw new Error('Failed to fetch citizen profile');
                    policyData = await policyRes.json();
                    citizenData = await citizenRes.json();
                } else {
                    const citizenRes = responses[0];
                    if (!citizenRes || !citizenRes.ok) throw new Error('Failed to fetch citizen profile');
                    citizenData = await citizenRes.json();
                }

                if (policyData && initialPolicyId !== null) {
                    setPolicy(policyData);
                    setSelectedPolicyId(initialPolicyId);
                }
                setCitizen(citizenData);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [initialPolicyId, getAccessTokenSilently]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedPolicyId || !description.trim() || !citizen) {
            setError('Please fill in all fields and ensure you have a citizen profile.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(false);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/opinions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    policyId: typeof selectedPolicyId === 'string' ? parseInt(selectedPolicyId) : selectedPolicyId,
                    description,
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to create opinion');
            }

            setSuccess(true);
            setDescription('');
            
            // Automatically go back to Policy Details after a short delay to show success message
            setTimeout(() => {
                onBack();
            }, 1500);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box>
            <Button 
                startIcon={<ArrowBackIcon />} 
                onClick={onBack} 
                sx={{ mb: 3 }}
            >
                Back to Policy Details
            </Button>
            
            <Paper elevation={3} sx={{ p: 4, maxWidth: 600, mx: 'auto' }}>
                <Typography variant="h5" gutterBottom>
                    Create Opinion
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
                {success && (
                    <Alert severity="success" sx={{ mb: 3 }}>
                        Opinion created successfully! Redirecting to Policy Details...
                    </Alert>
                )}

                <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3 }}>
                    <Stack spacing={3}>
                        <Box>
                            <Typography variant="subtitle2" color="text.secondary">Policy</Typography>
                            <Typography variant="body1">
                                {policy ? policy.description : <CircularProgress size={16} />}
                            </Typography>
                        </Box>

                        <Box>
                            <Typography variant="subtitle2" color="text.secondary">Author</Typography>
                            <Typography variant="body1">
                                {citizen ? `${citizen.givenName} ${citizen.surname}` : <CircularProgress size={16} />}
                            </Typography>
                        </Box>

                        <Divider />

                        <TextField
                            fullWidth
                            label="Your Opinion"
                            variant="outlined"
                            multiline
                            rows={4}
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            placeholder="Enter your opinion"
                            inputRef={textareaRef}
                            required
                        />

                        <Button 
                            type="submit" 
                            variant="contained" 
                            fullWidth
                            disabled={loading || !policy}
                            size="large"
                        >
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Opinion'}
                        </Button>
                    </Stack>
                </Box>
            </Paper>
        </Box>
    );
}

export default CreateOpinion;

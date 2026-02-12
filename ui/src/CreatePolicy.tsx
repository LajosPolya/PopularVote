import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  Button, 
  TextField, 
  Box, 
  Alert, 
  Paper,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  OutlinedInput,
  Chip
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { Citizen, getFullName } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface CreatePolicyProps {
    onBack: () => void;
    onCreateSuccess: () => void;
}

const CreatePolicy: React.FC<CreatePolicyProps> = ({ onBack, onCreateSuccess }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [description, setDescription] = useState<string>('');
    const [coAuthorCitizenIds, setCoAuthorCitizenIds] = useState<number[]>([]);
    const [politicians, setPoliticians] = useState<Citizen[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [fetchingPoliticians, setFetchingPoliticians] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchPoliticians = async () => {
            setFetchingPoliticians(true);
            try {
                const token = await getAccessTokenSilently();
                const response = await fetch(`${popularVoteApiUrl}/citizens/politicians`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (response.ok) {
                    const data = await response.json();
                    setPoliticians(data);
                }
            } catch (err) {
                console.error('Failed to fetch politicians:', err);
            } finally {
                setFetchingPoliticians(false);
            }
        };
        fetchPoliticians();
    }, [getAccessTokenSilently]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!description.trim()) return;

        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/policies`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({ 
                    description,
                    coAuthorCitizenIds
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to create policy');
            }

            setDescription('');
            if (onCreateSuccess) {
                onCreateSuccess();
            }
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
                Back to Policies
            </Button>
            
            <Paper elevation={3} sx={{ p: 4, maxWidth: 600, mx: 'auto' }}>
                <Typography variant="h5" gutterBottom>
                    Create New Policy
                </Typography>
                
                <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3 }}>
                    <TextField
                        fullWidth
                        label="Policy Description"
                        variant="outlined"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="Enter policy description"
                        multiline
                        rows={4}
                        sx={{ mb: 3 }}
                        required
                    />

                    <FormControl fullWidth sx={{ mb: 3 }}>
                        <InputLabel id="co-authors-label">Co-Authors (Politicians)</InputLabel>
                        <Select
                            labelId="co-authors-label"
                            id="co-authors-select"
                            multiple
                            value={coAuthorCitizenIds}
                            onChange={(e) => setCoAuthorCitizenIds(e.target.value as number[])}
                            input={<OutlinedInput label="Co-Authors (Politicians)" />}
                            renderValue={(selected) => (
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                                    {selected.map((value) => {
                                        const politician = politicians.find(p => p.id === value);
                                        return (
                                            <Chip 
                                                key={value} 
                                                label={politician ? getFullName(politician) : value} 
                                            />
                                        );
                                    })}
                                </Box>
                            )}
                            disabled={fetchingPoliticians}
                        >
                            {fetchingPoliticians ? (
                                <MenuItem disabled>
                                    <CircularProgress size={20} sx={{ mr: 1 }} />
                                    Loading politicians...
                                </MenuItem>
                            ) : (
                                politicians.map((politician) => (
                                    <MenuItem key={politician.id} value={politician.id}>
                                        {getFullName(politician)}
                                    </MenuItem>
                                ))
                            )}
                        </Select>
                    </FormControl>
                    
                    {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
                    
                    <Button 
                        type="submit" 
                        variant="contained" 
                        fullWidth
                        disabled={loading || !description.trim()}
                        size="large"
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : 'Create Policy'}
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
}

export default CreatePolicy;

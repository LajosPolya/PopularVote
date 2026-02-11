import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  Button, 
  TextField, 
  Box, 
  Alert, 
  Paper,
  CircularProgress
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface CreatePolicyProps {
    onBack: () => void;
    onCreateSuccess: () => void;
}

const CreatePolicy: React.FC<CreatePolicyProps> = ({ onBack, onCreateSuccess }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [description, setDescription] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

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
                body: JSON.stringify({ description }),
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

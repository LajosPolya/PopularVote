import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import {
  Typography,
  TextField,
  Button,
  Paper,
  Box,
  Alert,
  CircularProgress
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface CreatePoliticalPartyProps {
    onBack: () => void;
    onCreateSuccess: () => void;
    levelOfPoliticsId: number;
    provinceAndTerritoryId: number | null;
}

const CreatePoliticalParty: React.FC<CreatePoliticalPartyProps> = ({ onBack, onCreateSuccess, levelOfPoliticsId, provinceAndTerritoryId }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [displayName, setDisplayName] = useState('');
    const [hexColor, setHexColor] = useState('#000000');
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/political-parties`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    displayName,
                    hexColor,
                    description,
                    levelOfPoliticsId,
                    provinceAndTerritoryId,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to create political party');
            }

            onCreateSuccess();
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
                Back to Parties
            </Button>

            <Typography variant="h4" component="h2" sx={{ mb: 3 }}>
                Create New Political Party
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

            <Paper elevation={3} sx={{ p: 4 }}>
                <Box component="form" onSubmit={handleSubmit}>
                    <TextField
                        fullWidth
                        label="Party Name"
                        value={displayName}
                        onChange={(e) => setDisplayName(e.target.value)}
                        required
                        margin="normal"
                    />
                    <TextField
                        fullWidth
                        label="Hex Color"
                        type="color"
                        value={hexColor}
                        onChange={(e) => setHexColor(e.target.value)}
                        required
                        margin="normal"
                        helperText="Choose the party's official color"
                    />
                    <TextField
                        fullWidth
                        label="Description"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        required
                        multiline
                        rows={4}
                        margin="normal"
                    />
                    <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
                        <Button 
                            type="submit" 
                            variant="contained" 
                            disabled={loading}
                        >
                            {loading ? <CircularProgress size={24} /> : 'Create Party'}
                        </Button>
                    </Box>
                </Box>
            </Paper>
        </Box>
    );
};

export default CreatePoliticalParty;

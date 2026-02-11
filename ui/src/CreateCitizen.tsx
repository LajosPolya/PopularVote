import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  Button, 
  TextField, 
  Box, 
  Alert, 
  Paper,
  CircularProgress,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  Stack,
  SelectChangeEvent
} from '@mui/material';
import { affiliations } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface CreateCitizenProps {
    onCreateSuccess: () => void;
}

const CreateCitizen: React.FC<CreateCitizenProps> = ({ onCreateSuccess }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [givenName, setGivenName] = useState<string>('');
    const [surname, setSurname] = useState<string>('');
    const [middleName, setMiddleName] = useState<string>('');
    const [politicalAffiliation, setPoliticalAffiliation] = useState<string>('INDEPENDENT');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);


    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!givenName.trim() || !surname.trim()) {
            setError('Given Name and Surname are required');
            return;
        }

        setLoading(true);
        setError(null);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/self`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    givenName,
                    surname,
                    middleName: middleName || null,
                    politicalAffiliation,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to create citizen');
            }

            // Force refresh the token to include the new role
            await getAccessTokenSilently({
                cacheMode: 'off',
            } as any);

            onCreateSuccess();
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleAffiliationChange = (event: SelectChangeEvent<string>) => {
        setPoliticalAffiliation(event.target.value);
    };

    return (
        <Box sx={{ maxWidth: 600, mx: 'auto', mt: 4 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom align="center">
                    Complete Your Profile
                </Typography>
                <Typography variant="body1" sx={{ mb: 4 }} align="center">
                    It looks like you're new here. Please provide some details to continue.
                </Typography>
                
                <Box component="form" onSubmit={handleSubmit}>
                    <Stack spacing={3}>
                        <TextField
                            fullWidth
                            id="givenName"
                            label="Given Name"
                            value={givenName}
                            onChange={(e) => setGivenName(e.target.value)}
                            required
                        />
                        <TextField
                            fullWidth
                            id="middleName"
                            label="Middle Name (Optional)"
                            value={middleName}
                            onChange={(e) => setMiddleName(e.target.value)}
                        />
                        <TextField
                            fullWidth
                            id="surname"
                            label="Surname"
                            value={surname}
                            onChange={(e) => setSurname(e.target.value)}
                            required
                        />
                        
                        <FormControl fullWidth>
                            <InputLabel id="political-affiliation-label">Political Affiliation</InputLabel>
                            <Select
                                labelId="political-affiliation-label"
                                id="politicalAffiliation"
                                value={politicalAffiliation}
                                label="Political Affiliation"
                                onChange={handleAffiliationChange}
                            >
                                {Object.entries(affiliations).map(([value, label]) => (
                                    <MenuItem key={value} value={value}>
                                        {label}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        
                        {error && <Alert severity="error">{error}</Alert>}
                        
                        <Button 
                            type="submit" 
                            variant="contained" 
                            size="large"
                            disabled={loading}
                            fullWidth
                        >
                            {loading ? <CircularProgress size={24} color="inherit" /> : 'Complete Registration'}
                        </Button>
                    </Stack>
                </Box>
            </Paper>
        </Box>
    );
}

export default CreateCitizen;

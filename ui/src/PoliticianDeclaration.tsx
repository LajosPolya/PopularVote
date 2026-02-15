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
    MenuItem,
    FormControl,
    InputLabel,
    Select,
    Stack,
    SelectChangeEvent
} from '@mui/material';
import { LevelOfPolitics, DeclarePolitician } from './types';
import { affiliations } from './constants';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticianDeclarationProps {
    onSuccess: () => void;
    onCancel: () => void;
}

const PoliticianDeclaration: React.FC<PoliticianDeclarationProps> = ({ onSuccess, onCancel }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [levels, setLevels] = useState<LevelOfPolitics[]>([]);
    const [levelOfPoliticsId, setLevelOfPoliticsId] = useState<number | ''>('');
    const [geographicLocation, setGeographicLocation] = useState<string>('');
    const [politicalAffiliation, setPoliticalAffiliation] = useState<string>('INDEPENDENT');
    const [loading, setLoading] = useState<boolean>(true);
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLevels = async () => {
            try {
                const token = await getAccessTokenSilently();
                const response = await fetch(`${popularVoteApiUrl}/levels-of-politics`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (!response.ok) {
                    throw new Error('Failed to fetch levels of politics');
                }
                const data = await response.json();
                setLevels(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchLevels();
    }, [getAccessTokenSilently]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (levelOfPoliticsId === '') {
            setError('Level of politics is required');
            return;
        }

        setSubmitting(true);
        setError(null);
        try {
            const token = await getAccessTokenSilently();
            const body: DeclarePolitician = {
                levelOfPoliticsId: levelOfPoliticsId as number,
                geographicLocation: geographicLocation || null,
                politicalAffiliation,
            };
            const response = await fetch(`${popularVoteApiUrl}/citizens/self/declare-politician`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(body),
            });

            if (response.status !== 202) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to submit declaration');
            }

            onSuccess();
        } catch (err: any) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleLevelChange = (event: SelectChangeEvent<number | ''>) => {
        setLevelOfPoliticsId(event.target.value as number);
    };

    const handleAffiliationChange = (event: SelectChangeEvent<string>) => {
        setPoliticalAffiliation(event.target.value);
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Box sx={{ maxWidth: 600, mx: 'auto', mt: 4 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom align="center">
                    Politician Verification
                </Typography>
                <Typography variant="body1" sx={{ mb: 4 }} align="center">
                    Please provide your political details for verification.
                </Typography>
                
                <Box component="form" onSubmit={handleSubmit}>
                    <Stack spacing={3}>
                        <FormControl fullWidth required>
                            <InputLabel id="level-label">Level of Politics</InputLabel>
                            <Select
                                labelId="level-label"
                                id="levelOfPoliticsId"
                                value={levelOfPoliticsId}
                                label="Level of Politics"
                                onChange={handleLevelChange}
                            >
                                {levels.map((level) => (
                                    <MenuItem key={level.id} value={level.id}>
                                        {level.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <TextField
                            fullWidth
                            id="geographicLocation"
                            label="Geographic Location (e.g. Toronto, Ontario, Canada)"
                            value={geographicLocation}
                            onChange={(e) => setGeographicLocation(e.target.value)}
                            placeholder="Specify your jurisdiction"
                        />

                        <FormControl fullWidth required>
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
                        
                        <Stack direction="row" spacing={2}>
                            <Button 
                                variant="outlined" 
                                fullWidth
                                onClick={onCancel}
                                disabled={submitting}
                            >
                                Cancel
                            </Button>
                            <Button 
                                type="submit" 
                                variant="contained" 
                                color="success"
                                fullWidth
                                disabled={submitting}
                            >
                                {submitting ? <CircularProgress size={24} color="inherit" /> : 'Submit for Verification'}
                            </Button>
                        </Stack>
                    </Stack>
                </Box>
            </Paper>
        </Box>
    );
}

export default PoliticianDeclaration;

import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import {
  Typography,
  Button,
  Box,
  Alert,
  Paper,
  CircularProgress,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent
} from '@mui/material';
import { Citizen, GeoData, ProvinceAndTerritory, Municipality, PostalCode } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface IdVerificationProps {
    onVerificationSuccess: (updatedCitizen: Citizen) => void;
}

const IdVerification: React.FC<IdVerificationProps> = ({ onVerificationSuccess }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [geoData, setGeoData] = useState<GeoData | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const [selectedProvinceId, setSelectedProvinceId] = useState<number | ''>('');
    const [selectedMunicipalityId, setSelectedMunicipalityId] = useState<number | ''>('');
    const [selectedPostalCodeId, setSelectedPostalCodeId] = useState<number | ''>('');
    const [verifying, setVerifying] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);

    useEffect(() => {
        const fetchGeoData = async () => {
            try {
                const token = await getAccessTokenSilently();
                const response = await fetch(`${popularVoteApiUrl}/geo-data`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch geographical data');
                }

                const data: GeoData = await response.json();
                setGeoData(data);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchGeoData();
    }, [getAccessTokenSilently]);

    const handleProvinceChange = (event: SelectChangeEvent<number | ''>) => {
        setSelectedProvinceId(event.target.value as number);
        setSelectedMunicipalityId('');
        setSelectedPostalCodeId('');
    };

    const handleMunicipalityChange = (event: SelectChangeEvent<number | ''>) => {
        setSelectedMunicipalityId(event.target.value as number);
        setSelectedPostalCodeId('');
    };

    const handlePostalCodeChange = (event: SelectChangeEvent<number | ''>) => {
        setSelectedPostalCodeId(event.target.value as number);
    };

    const handleSubmit = async () => {
        if (selectedPostalCodeId === '') return;

        setVerifying(true);
        setError(null);
        try {
            const token = await getAccessTokenSilently();
            const response = await fetch(`${popularVoteApiUrl}/citizens/self/verify-identity`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    postalCodeId: selectedPostalCodeId,
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to verify identity');
            }

            const updatedCitizen: Citizen = await response.json();
            onVerificationSuccess(updatedCitizen);
            setSuccess(true);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setVerifying(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <CircularProgress />
            </Box>
        );
    }

    const provinces = geoData?.provincesAndTerritories || [];
    const municipalities = selectedProvinceId !== '' 
        ? provinces.find(p => p.id === selectedProvinceId)?.municipalities || []
        : [];
    const postalCodes = selectedMunicipalityId !== ''
        ? municipalities.find(m => m.id === selectedMunicipalityId)?.postalCodes || []
        : [];

    return (
        <Box sx={{ maxWidth: 600, mx: 'auto', mt: 4 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom align="center">
                    ID Verification
                </Typography>
                <Typography variant="body1" sx={{ mb: 4 }} align="center">
                    Please select your location to verify your identity.
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 3 }}>Identity verified and postal code updated successfully!</Alert>}

                <Stack spacing={3}>
                    <FormControl fullWidth>
                        <InputLabel id="province-label">Province/Territory</InputLabel>
                        <Select
                            labelId="province-label"
                            id="province-select"
                            value={selectedProvinceId}
                            label="Province/Territory"
                            onChange={handleProvinceChange}
                        >
                            {provinces.map((province) => (
                                <MenuItem key={province.id} value={province.id}>
                                    {province.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl fullWidth disabled={selectedProvinceId === ''}>
                        <InputLabel id="municipality-label">Municipality</InputLabel>
                        <Select
                            labelId="municipality-label"
                            id="municipality-select"
                            value={selectedMunicipalityId}
                            label="Municipality"
                            onChange={handleMunicipalityChange}
                        >
                            {municipalities.map((municipality) => (
                                <MenuItem key={municipality.id} value={municipality.id}>
                                    {municipality.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <FormControl fullWidth disabled={selectedMunicipalityId === ''}>
                        <InputLabel id="postal-code-label">Postal Code</InputLabel>
                        <Select
                            labelId="postal-code-label"
                            id="postal-code-select"
                            value={selectedPostalCodeId}
                            label="Postal Code"
                            onChange={handlePostalCodeChange}
                        >
                            {postalCodes.map((pc) => (
                                <MenuItem key={pc.id} value={pc.id}>
                                    {pc.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>

                    <Button
                        variant="contained"
                        size="large"
                        onClick={handleSubmit}
                        disabled={selectedPostalCodeId === '' || verifying || success}
                        fullWidth
                    >
                        {verifying ? <CircularProgress size={24} color="inherit" /> : 'Verify Identity'}
                    </Button>
                </Stack>
            </Paper>
        </Box>
    );
};

export default IdVerification;

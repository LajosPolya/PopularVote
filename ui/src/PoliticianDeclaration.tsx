import React, { useState, useEffect, useMemo } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import {
    Typography,
    Button,
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
import {LevelOfPolitics, DeclarePolitician, PoliticalParty, GeoData, FederalElectoralDistrict} from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticianDeclarationProps {
    onSuccess: () => void;
    onCancel: () => void;
}

const PoliticianDeclaration: React.FC<PoliticianDeclarationProps> = ({ onSuccess, onCancel }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [levels, setLevels] = useState<LevelOfPolitics[]>([]);
    const [levelOfPoliticsId, setLevelOfPoliticsId] = useState<number | ''>('');
    const [geoData, setGeoData] = useState<GeoData | null>(null);
    const [provinceId, setProvinceId] = useState<number | ''>('');
    const [federalElectoralDistrictId, setFederalElectoralDistrictId] = useState<number | ''>('');
    const [parties, setParties] = useState<Map<number,PoliticalParty[]>>(new Map());
    const [politicalAffiliation, setPoliticalAffiliation] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(true);
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    const federalElectoralDistrictsByProvince = useMemo(() => {
        if (!geoData) return new Map<number, { provinceName: string, districts: FederalElectoralDistrict[] }>();

        const provinceMap = new Map<number, { provinceName: string, districts: FederalElectoralDistrict[] }>();

        geoData.provincesAndTerritories.forEach(province => {
            if (province.federalElectoralDistricts && province.federalElectoralDistricts.length > 0) {
                provinceMap.set(province.id, {
                    provinceName: province.name,
                    districts: [...province.federalElectoralDistricts].sort((a, b) => a.name.localeCompare(b.name))
                });
            }
        });

        return provinceMap;
    }, [geoData]);

    useEffect(() => {
        const fetchParties = async () => {
            setLoading(true);
            try {
                const token = await getAccessTokenSilently();
                const response = await fetch(`${popularVoteApiUrl}/political-parties`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });
                if (!response.ok) {
                    throw new Error('Failed to fetch political parties');
                }
                const data: PoliticalParty[] = await response.json();
                const partiesByLevelOfPolitics: Map<number, PoliticalParty[]> = new Map();
                data.forEach((party) =>
                    partiesByLevelOfPolitics.set(party.levelOfPoliticsId, [...(partiesByLevelOfPolitics.get(party.levelOfPoliticsId) || []), party]));
                setParties(partiesByLevelOfPolitics);
                setError(null);
            } catch (err: any) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

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
            }
        };

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
            }
        };

        const fetchAll = async () => {
            setLoading(true);
            await Promise.all([fetchLevels(), fetchParties(), fetchGeoData()]);
            setLoading(false);
        };

        fetchAll();
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
                federalElectoralDistrictId: Number(federalElectoralDistrictId),
                politicalAffiliationId: Number(politicalAffiliation),
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

    const handleProvinceChange = (event: SelectChangeEvent<number | ''>) => {
        setProvinceId(event.target.value as number);
        setFederalElectoralDistrictId('');
    };

    const handleDistrictChange = (event: SelectChangeEvent<number | ''>) => {
        setFederalElectoralDistrictId(event.target.value as number);
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

                        <FormControl fullWidth required>
                            <InputLabel id="province-label">Province</InputLabel>
                            <Select
                                labelId="province-label"
                                id="provinceId"
                                value={provinceId}
                                label="Province"
                                onChange={handleProvinceChange}
                            >
                                {Array.from(federalElectoralDistrictsByProvince.entries()).map(([id, { provinceName }]) => (
                                    <MenuItem key={id} value={id}>
                                        {provinceName}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <FormControl fullWidth required disabled={provinceId === ''}>
                            <InputLabel id="district-label">Federal Electoral District</InputLabel>
                            <Select
                                labelId="district-label"
                                id="federalElectoralDistrictId"
                                value={federalElectoralDistrictId}
                                label="Federal Electoral District"
                                onChange={handleDistrictChange}
                            >
                                {provinceId !== '' && federalElectoralDistrictsByProvince.get(provinceId as number)?.districts.map((district) => (
                                    <MenuItem key={district.id} value={district.id}>
                                        {district.name} ({district.code})
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <FormControl fullWidth required>
                            <InputLabel id="political-affiliation-label">Political Affiliation</InputLabel>
                            <Select
                                labelId="political-affiliation-label"
                                id="politicalAffiliation"
                                value={politicalAffiliation}
                                label="Political Affiliation"
                                onChange={handleAffiliationChange}
                                disabled={!parties.has(levelOfPoliticsId as number)}
                            >
                                {parties.get(levelOfPoliticsId as number)?.map((party) => (
                                    <MenuItem key={party.id} value={party.id}>
                                        {party.displayName}
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

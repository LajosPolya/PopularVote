import React, { useState, useEffect } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { 
  Typography, 
  List, 
  ListItem, 
  ListItemText, 
  Paper, 
  Box, 
  CircularProgress, 
  Alert,
  Divider,
  ListItemButton,
  Chip,
  TextField,
  InputAdornment,
  Pagination,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  SelectChangeEvent
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { Citizen, getFullName, PoliticalParty, Page } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticianSearchProps {
    onPoliticianClick: (id: number) => void;
    levelOfPoliticsId: number | null;
    politicalParties: Map<number, PoliticalParty>
}

const PoliticianSearch: React.FC<PoliticianSearchProps> = ({ onPoliticianClick, levelOfPoliticsId, politicalParties }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [politicians, setPoliticians] = useState<Citizen[]>([]);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [pageSize, setPageSize] = useState<number>(10);

    const fetchPoliticians = async (pageNumber: number, size: number = pageSize) => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const queryParams = new URLSearchParams();
            queryParams.append('page', pageNumber.toString());
            queryParams.append('size', size.toString());
            if (levelOfPoliticsId) {
                queryParams.append('levelOfPolitics', levelOfPoliticsId.toString());
            }
            const response = await fetch(`${popularVoteApiUrl}/citizens/politicians?${queryParams.toString()}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch politicians');
            }
            const data: Page<Citizen> = await response.json();
            setPoliticians(data.content);
            setTotalPages(data.totalPages);
            setError(null);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        setPage(0);
        fetchPoliticians(0);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [levelOfPoliticsId]);

    const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
        const newPage = value - 1;
        setPage(newPage);
        fetchPoliticians(newPage, pageSize);
    };

    const handlePageSizeChange = (event: SelectChangeEvent<number>) => {
        const newSize = event.target.value as number;
        setPageSize(newSize);
        setPage(0);
        fetchPoliticians(0, newSize);
    };

    const filteredPoliticians = politicians.filter(p => {
        const fullName = `${p.givenName} ${p.middleName || ''} ${p.surname}`.toLowerCase();
        return fullName.includes(searchTerm.toLowerCase());
    });

    return (
        <Box>
            <Typography variant="h4" component="h2" sx={{ mb: 3 }}>
                Search Politicians
            </Typography>

            <TextField
                fullWidth
                variant="outlined"
                placeholder="Search by name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                sx={{ mb: 3 }}
                InputProps={{
                    startAdornment: (
                        <InputAdornment position="start">
                            <SearchIcon />
                        </InputAdornment>
                    ),
                }}
            />

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {loading && politicians.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                    <Typography sx={{ ml: 2 }}>Loading politicians...</Typography>
                </Box>
            ) : (
                <Paper elevation={2}>
                    <List sx={{ p: 0 }}>
                        {filteredPoliticians.map((politician, index) => (
                            <React.Fragment key={politician.id}>
                                {index > 0 && <Divider />}
                                <ListItem disablePadding>
                                    <ListItemButton onClick={() => onPoliticianClick(politician.id)}>
                                        <ListItemText 
                                            primary={getFullName(politician)}
                                            secondary={
                                                <Box component="span" sx={{ display: 'flex', alignItems: 'center', mt: 0.5, gap: 1 }}>
                                                    {(() => {
                                                        const party = politician.politicalAffiliationId ? politicalParties.get(politician.politicalAffiliationId) : null;
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
                                            }
                                            secondaryTypographyProps={{ component: 'div' }}
                                        />
                                    </ListItemButton>
                                </ListItem>
                            </React.Fragment>
                        ))}
                    </List>
                </Paper>
            )}
            
            {!loading && filteredPoliticians.length === 0 && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    {politicians.length === 0 ? 'No politicians found.' : 'No politicians match your search.'}
                </Typography>
            )}

            {(totalPages > 0 || politicians.length > 0) && (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 3, gap: 2 }}>
                    <Pagination 
                        count={totalPages} 
                        page={page + 1} 
                        onChange={handlePageChange} 
                        color="primary" 
                    />
                    <FormControl size="small" sx={{ minWidth: 120 }}>
                        <InputLabel id="page-size-label">Page Size</InputLabel>
                        <Select
                            labelId="page-size-label"
                            value={pageSize}
                            label="Page Size"
                            onChange={handlePageSizeChange}
                        >
                            <MenuItem value={5}>5</MenuItem>
                            <MenuItem value={10}>10</MenuItem>
                            <MenuItem value={25}>25</MenuItem>
                            <MenuItem value={50}>50</MenuItem>
                        </Select>
                    </FormControl>
                </Box>
            )}
        </Box>
    );
}

export default PoliticianSearch;

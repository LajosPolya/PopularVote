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
  ListItemIcon,
  ListItemButton,
  Button,
  Pagination,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  SelectChangeEvent
} from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import AddIcon from '@mui/icons-material/Add';
import { PoliticalParty, Page } from './types';

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticalPartiesProps {
    onPartyClick?: (id: number) => void;
    canCreateParty?: boolean;
    onCreateParty?: () => void;
    levelOfPoliticsId: number | null;
    provinceAndTerritoryId: number | null;
}

const PoliticalParties: React.FC<PoliticalPartiesProps> = ({ onPartyClick, canCreateParty, onCreateParty, levelOfPoliticsId, provinceAndTerritoryId }) => {
    const { getAccessTokenSilently } = useAuth0();
    const [parties, setParties] = useState<PoliticalParty[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [pageSize, setPageSize] = useState<number>(10);

    const fetchParties = async (pageNumber: number, size: number = pageSize) => {
        setLoading(true);
        try {
            const token = await getAccessTokenSilently();
            const params = new URLSearchParams();
            params.append('page', pageNumber.toString());
            params.append('size', size.toString());
            if (levelOfPoliticsId) {
                params.append('levelOfPolitics', levelOfPoliticsId.toString());
            }
            if (provinceAndTerritoryId) {
                params.append('provinceAndTerritoryId', provinceAndTerritoryId.toString());
            }
            const queryParams = params.toString() ? `?${params.toString()}` : '';
            const response = await fetch(`${popularVoteApiUrl}/political-parties${queryParams}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                throw new Error('Failed to fetch political parties');
            }
            const data: Page<PoliticalParty> = await response.json();
            setParties(data.content);
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
        fetchParties(0);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [levelOfPoliticsId, provinceAndTerritoryId]);

    const handlePageChange = (event: React.ChangeEvent<unknown>, value: number) => {
        const newPage = value - 1;
        setPage(newPage);
        fetchParties(newPage, pageSize);
    };

    const handlePageSizeChange = (event: SelectChangeEvent<number>) => {
        const newSize = event.target.value as number;
        setPageSize(newSize);
        setPage(0);
        fetchParties(0, newSize);
    };

    return (
        <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h4" component="h2">
                    Political Parties
                </Typography>
                {canCreateParty && (
                    <Button 
                        variant="contained" 
                        startIcon={<AddIcon />} 
                        onClick={onCreateParty}
                    >
                        Create Party
                    </Button>
                )}
            </Box>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            {loading && parties.length === 0 ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                    <CircularProgress />
                    <Typography sx={{ ml: 2 }}>Loading political parties...</Typography>
                </Box>
            ) : (
                <Paper elevation={2}>
                    <List sx={{ p: 0 }}>
                        {parties.map((party, index) => (
                            <React.Fragment key={party.id}>
                                {index > 0 && <Divider />}
                                <ListItem disablePadding>
                                    <ListItemButton onClick={() => onPartyClick && onPartyClick(party.id)}>
                                        <ListItemIcon>
                                            <CircleIcon sx={{ color: party.hexColor }} />
                                        </ListItemIcon>
                                        <ListItemText 
                                            primary={party.displayName}
                                            secondary={party.description}
                                        />
                                    </ListItemButton>
                                </ListItem>
                            </React.Fragment>
                        ))}
                    </List>
                </Paper>
            )}

            {(totalPages > 0 || parties.length > 0) && (
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
            
            {parties.length === 0 && !loading && (
                <Typography variant="body1" sx={{ textAlign: 'center', mt: 4, color: 'text.secondary' }}>
                    No political parties found.
                </Typography>
            )}
        </Box>
    );
}

export default PoliticalParties;

import React, { useState, useEffect } from "react";
import { useAuth0 } from "@auth0/auth0-react";
import {
  Typography,
  Button,
  List,
  ListItem,
  ListItemText,
  Paper,
  Box,
  CircularProgress,
  Chip,
  Alert,
  Divider,
  ListItemButton,
  IconButton,
  Tooltip,
  Pagination,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  SelectChangeEvent,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import BookmarkIcon from "@mui/icons-material/Bookmark";
import BookmarkBorderIcon from "@mui/icons-material/BookmarkBorder";
import TimerOutlinedIcon from "@mui/icons-material/TimerOutlined";
import TimerOffOutlinedIcon from "@mui/icons-material/TimerOffOutlined";
import FiberNewOutlinedIcon from "@mui/icons-material/FiberNewOutlined";
import dayjs, { Dayjs } from "dayjs";
import { Policy, PoliticalParty, Page } from "./types";

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliciesProps {
  onPolicyClick: (id: number) => void;
  onCitizenClick: (id: number) => void;
  onPartyClick: (id: number) => void;
  onCreatePolicy: () => void;
  levelOfPoliticsId: number | null;
  provinceAndTerritoryId: number | null;
  politicalParties: Map<number, PoliticalParty>;
}

const Policies: React.FC<PoliciesProps> = ({
  onPolicyClick,
  onCitizenClick,
  onPartyClick,
  onCreatePolicy,
  levelOfPoliticsId,
  provinceAndTerritoryId,
  politicalParties,
}) => {
  const { getAccessTokenSilently } = useAuth0();
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [canCreatePolicy, setCanCreatePolicy] = useState<boolean>(false);
  const [bookmarkingId, setBookmarkingId] = useState<number | null>(null);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [status, setStatus] = useState<string>("all");
  const [publisherPoliticalPartyId, setPublisherPoliticalPartyId] = useState<
    string | number
  >("all");

  const checkPermissions = async () => {
    try {
      const token = await getAccessTokenSilently();
      const payload = JSON.parse(atob(token.split(".")[1] || ""));
      const permissions = payload.scope?.split(" ") || [];
      setCanCreatePolicy(permissions.includes("write:policies"));
    } catch (err) {
      console.error("Error checking permissions:", err);
      setCanCreatePolicy(false);
    }
  };

  const fetchPolicies = async (pageNumber: number, size: number = pageSize) => {
    setLoading(true);
    try {
      const token = await getAccessTokenSilently();
      const queryParams = new URLSearchParams();
      queryParams.append("page", pageNumber.toString());
      queryParams.append("size", size.toString());
      if (levelOfPoliticsId) {
        queryParams.append("levelOfPolitics", levelOfPoliticsId.toString());
      }
      if (provinceAndTerritoryId) {
        queryParams.append(
          "provinceAndTerritoryId",
          provinceAndTerritoryId.toString(),
        );
      }
      if (status !== "all") {
        queryParams.append("status", status);
      }
      if (publisherPoliticalPartyId !== "all") {
        queryParams.append(
          "publisher-political-party",
          publisherPoliticalPartyId.toString(),
        );
      }
      const response = await fetch(
        `${popularVoteApiUrl}/policies?${queryParams.toString()}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );
      if (!response.ok) {
        throw new Error("Failed to fetch policies");
      }
      const data: Page<Policy> = await response.json();
      setPolicies(data.content);
      setTotalPages(data.totalPages);
      setError(null);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleBookmark = async (e: React.MouseEvent, policy: Policy) => {
    e.stopPropagation();
    if (bookmarkingId) return;

    setBookmarkingId(policy.id);
    try {
      const token = await getAccessTokenSilently();
      const response = await fetch(
        `${popularVoteApiUrl}/policies/${policy.id}/bookmark`,
        {
          method: policy.isBookmarked ? "DELETE" : "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      );

      if (response.ok) {
        setPolicies((prevPolicies) =>
          prevPolicies.map((p) =>
            p.id === policy.id ? { ...p, isBookmarked: !p.isBookmarked } : p,
          ),
        );
      } else {
        console.error(
          `Failed to ${policy.isBookmarked ? "unbookmark" : "bookmark"} policy`,
        );
      }
    } catch (err) {
      console.error(`Error toggling bookmark:`, err);
    } finally {
      setBookmarkingId(null);
    }
  };

  useEffect(() => {
    checkPermissions();
    setPage(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [levelOfPoliticsId, provinceAndTerritoryId, status]);

  useEffect(() => {
    if (levelOfPoliticsId) {
      const currentParty = politicalParties.get(
        Number(publisherPoliticalPartyId),
      );
      if (currentParty && currentParty.levelOfPoliticsId !== levelOfPoliticsId) {
        setPublisherPoliticalPartyId("all");
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [levelOfPoliticsId, politicalParties]);

  useEffect(() => {
    fetchPolicies(page, pageSize);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    levelOfPoliticsId,
    provinceAndTerritoryId,
    status,
    publisherPoliticalPartyId,
    page,
    pageSize,
  ]);

  const handlePageChange = (
    event: React.ChangeEvent<unknown>,
    value: number,
  ) => {
    const newPage = value - 1;
    setPage(newPage);
  };

  const handlePageSizeChange = (event: SelectChangeEvent<number>) => {
    const newSize = event.target.value as number;
    setPageSize(newSize);
    setPage(0);
  };

  const handleStatusChange = (event: SelectChangeEvent<string>) => {
    setStatus(event.target.value as string);
    setPage(0);
  };

  const handlePublisherPoliticalPartyChange = (
    event: SelectChangeEvent<string | number>,
  ) => {
    setPublisherPoliticalPartyId(event.target.value);
    setPage(0);
  };

  return (
    <Box>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 3,
        }}
      >
        <Typography variant="h4" component="h2">
          Policies
        </Typography>
        <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel id="status-filter-label">Status</InputLabel>
            <Select
              labelId="status-filter-label"
              id="status-filter"
              value={status}
              label="Status"
              onChange={handleStatusChange}
            >
              <MenuItem value="all">All</MenuItem>
              <MenuItem value="open">Open</MenuItem>
              <MenuItem value="closed">Closed</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 160 }}>
            <InputLabel id="party-filter-label">Publisher Party</InputLabel>
            <Select
              labelId="party-filter-label"
              id="party-filter"
              value={publisherPoliticalPartyId}
              label="Publisher Party"
              onChange={handlePublisherPoliticalPartyChange}
            >
              <MenuItem value="all">All Parties</MenuItem>
              {Array.from(politicalParties.values())
                .filter(
                  (party) =>
                    !levelOfPoliticsId ||
                    party.levelOfPoliticsId === levelOfPoliticsId,
                )
                .sort((a, b) => a.displayName.localeCompare(b.displayName))
                .map((party) => (
                  <MenuItem key={party.id} value={party.id}>
                    {party.displayName}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
          {canCreatePolicy && (
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={onCreatePolicy}
            >
              Create Policy
            </Button>
          )}
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading && policies.length === 0 ? (
        <Box sx={{ display: "flex", justifyContent: "center", my: 4 }}>
          <CircularProgress />
          <Typography sx={{ ml: 2 }}>Loading policies...</Typography>
        </Box>
      ) : (
        <Paper elevation={2}>
          <List sx={{ p: 0 }}>
            {policies.map((policy, index) => (
              <React.Fragment key={policy.id}>
                {index > 0 && <Divider />}
                <ListItem disablePadding>
                  <ListItemButton
                    onClick={() => onPolicyClick && onPolicyClick(policy.id)}
                  >
                    <ListItemText
                      primary={
                        <Box
                          sx={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                          }}
                        >
                          <Typography variant="body1" fontWeight="medium">
                            {policy.description}
                          </Typography>
                          <Box sx={{ display: "flex", alignItems: "center" }}>
                            {dayjs().diff(dayjs(policy.creationDate), "day") <
                            1 ? (
                              <FiberNewOutlinedIcon
                                color="primary"
                                fontSize="medium"
                                sx={{ ml: 1 }}
                              />
                            ) : null}
                            <Tooltip
                              title={
                                dayjs().isAfter(dayjs(policy.closeDate))
                                  ? "Voting Closed"
                                  : "Voting Open"
                              }
                            >
                              <Box sx={{ display: "inline-flex", ml: 1 }}>
                                {dayjs().isAfter(dayjs(policy.closeDate)) ? (
                                  <TimerOffOutlinedIcon
                                    color="primary"
                                    fontSize="small"
                                  />
                                ) : (
                                  <TimerOutlinedIcon
                                    color="primary"
                                    fontSize="small"
                                  />
                                )}
                              </Box>
                            </Tooltip>
                            <Tooltip
                              title={
                                policy.isBookmarked
                                  ? "Remove Bookmark"
                                  : "Bookmark this policy"
                              }
                            >
                              <IconButton
                                onClick={(e) => handleToggleBookmark(e, policy)}
                                color="primary"
                                size="small"
                                disabled={bookmarkingId === policy.id}
                                sx={{ ml: 1 }}
                              >
                                {policy.isBookmarked ? (
                                  <BookmarkIcon fontSize="small" />
                                ) : (
                                  <BookmarkBorderIcon fontSize="small" />
                                )}
                              </IconButton>
                            </Tooltip>
                          </Box>
                        </Box>
                      }
                      secondary={
                        <Box
                          sx={{
                            gap: "5px",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "left",
                          }}
                        >
                          Published by:{" "}
                          <Box
                            component="span"
                            sx={{
                              cursor: "pointer",
                              color: "primary.main",
                              "&:hover": { textDecoration: "underline" },
                            }}
                            onClick={(e) => {
                              e.stopPropagation();
                              onCitizenClick(policy.publisherCitizenId);
                            }}
                          >
                            {policy.publisherName}
                          </Box>
                          {(() => {
                            const party = policy.publisherPoliticalPartyId
                              ? politicalParties.get(
                                  policy.publisherPoliticalPartyId,
                                )
                              : null;
                            if (!party) return null;

                            return (
                              <>
                                of{" "}
                                <Chip
                                  label={party.displayName || "Unknown Party"}
                                  size="small"
                                  sx={{
                                    bgcolor: party.hexColor || "grey.500",
                                    color: "white",
                                    cursor: "pointer",
                                  }}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    onPartyClick(party.id);
                                  }}
                                />
                              </>
                            );
                          })()}
                        </Box>
                      }
                      secondaryTypographyProps={{ component: "div" }}
                    />
                  </ListItemButton>
                </ListItem>
              </React.Fragment>
            ))}
          </List>
        </Paper>
      )}

      {(totalPages > 0 || policies.length > 0) && (
        <Box
          sx={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            mt: 3,
            gap: 2,
          }}
        >
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

      {policies.length === 0 && !loading && (
        <Typography
          variant="body1"
          sx={{ textAlign: "center", mt: 4, color: "text.secondary" }}
        >
          No policies found.
        </Typography>
      )}
    </Box>
  );
};

export default Policies;

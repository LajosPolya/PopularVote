import { useAuth0 } from "@auth0/auth0-react";
import {
  Autocomplete,
  Box,
  CircularProgress,
  TextField,
  Typography,
} from "@mui/material";
import { debounce } from "lodash";
import React, { useEffect, useMemo, useState } from "react";
import { Citizen, getFullName, Page, PoliticalParty } from "./types";

const popularVoteApiUrl = process.env.REACT_APP_POPULAR_VOTE_API_URL;

interface PoliticianDropdownProps {
  value: Citizen | null;
  onChange: (politician: Citizen | null) => void;
  levelOfPoliticsId?: number;
  provinceAndTerritoryId?: number;
  politicalParties: Map<number, PoliticalParty>;
  error?: boolean;
  helperText?: string;
  label?: string;
}

const PoliticianDropdown: React.FC<PoliticianDropdownProps> = ({
  value,
  onChange,
  levelOfPoliticsId,
  provinceAndTerritoryId,
  politicalParties,
  error,
  helperText,
  label = "Select Politician",
}) => {
  const { getAccessTokenSilently } = useAuth0();
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState<readonly Citizen[]>([]);
  const [inputValue, setInputValue] = useState("");
  const [loading, setLoading] = useState(false);

  const fetchPoliticians = useMemo(
    () =>
      debounce(
        async (search: string, callback: (results: Citizen[]) => void) => {
          setLoading(true);
          try {
            const token = await getAccessTokenSilently();
            const queryParams = new URLSearchParams();
            queryParams.append("page", "0");
            queryParams.append("size", "20");
            if (levelOfPoliticsId) {
              queryParams.append(
                "levelOfPolitics",
                levelOfPoliticsId.toString(),
              );
            }
            if (provinceAndTerritoryId) {
              queryParams.append(
                "provinceAndTerritoryId",
                provinceAndTerritoryId.toString(),
              );
            }
            // The API doesn't seem to have a search by name parameter for getPoliticians,
            // but let's assume it might or we'll filter client-side if needed.
            // For now, let's just fetch and let Autocomplete handle the filtering.

            const response = await fetch(
              `${popularVoteApiUrl}/citizens/politicians?${queryParams.toString()}`,
              {
                headers: {
                  Authorization: `Bearer ${token}`,
                },
              },
            );
            if (!response.ok) {
              throw new Error("Failed to fetch politicians");
            }
            const data: Page<Citizen> = await response.json();
            callback(data.content);
          } catch (err) {
            console.error(err);
            callback([]);
          } finally {
            setLoading(false);
          }
        },
        400,
      ),
    [getAccessTokenSilently, levelOfPoliticsId, provinceAndTerritoryId],
  );

  useEffect(() => {
    let active = true;

    if (!open) {
      return undefined;
    }

    fetchPoliticians(inputValue, (results: Citizen[]) => {
      if (active) {
        setOptions(results);
      }
    });

    return () => {
      active = false;
    };
  }, [open, inputValue, fetchPoliticians]);

  return (
    <Autocomplete
      id="politician-select"
      open={open}
      onOpen={() => setOpen(true)}
      onClose={() => setOpen(false)}
      isOptionEqualToValue={(option, val) => option.id === val.id}
      getOptionLabel={(option) => getFullName(option)}
      options={options}
      loading={loading}
      value={value}
      onChange={(_, newValue) => onChange(newValue)}
      onInputChange={(_, newInputValue) => setInputValue(newInputValue)}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          error={error}
          helperText={helperText}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <React.Fragment>
                {loading ? (
                  <CircularProgress color="inherit" size={20} />
                ) : null}
                {params.InputProps.endAdornment}
              </React.Fragment>
            ),
          }}
        />
      )}
      renderOption={(props, option) => {
        const { key, ...rest } = props as any;
        const party = option.politicalAffiliationId
          ? politicalParties.get(option.politicalAffiliationId)
          : null;

        return (
          <li key={key} {...rest}>
            <Box>
              <Typography variant="body1">{getFullName(option)}</Typography>
              {party && (
                <Typography variant="caption" color="text.secondary">
                  {party.displayName}
                </Typography>
              )}
            </Box>
          </li>
        );
      }}
    />
  );
};

export default PoliticianDropdown;

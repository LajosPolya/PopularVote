import { useAuth0 } from "@auth0/auth0-react";
import HowToVoteIcon from "@mui/icons-material/HowToVote";
import LoginIcon from "@mui/icons-material/Login";
import {
  Box,
  Button,
  Container,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import React from "react";

const LandingPage: React.FC = () => {
  const { loginWithRedirect } = useAuth0();

  return (
    <Container maxWidth="md">
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          textAlign: "center",
        }}
      >
        <HowToVoteIcon sx={{ fontSize: 60, color: "primary.main", mb: 2 }} />
        <Typography variant="h2" component="h1" gutterBottom fontWeight="bold">
          Popular Vote
        </Typography>
        <Typography variant="h5" color="text.secondary" paragraph>
          Empowering citizens to participate directly in the democratic process.
          Vote on policies, engage with politicians, and make your voice heard.
        </Typography>

        <Paper
          elevation={3}
          sx={{
            p: 4,
            mt: 4,
            borderRadius: 2,
            backgroundColor: "background.paper",
          }}
        >
          <Stack spacing={3} alignItems="center">
            <Typography variant="h6">Ready to make an impact?</Typography>
            <Button
              variant="contained"
              size="large"
              startIcon={<LoginIcon />}
              onClick={() => loginWithRedirect()}
              sx={{ px: 4, py: 1.5, fontSize: "1.1rem" }}
            >
              Log In / Sign Up
            </Button>
            <Typography variant="body2" color="text.secondary">
              Use your account to start voting and participating in discussions.
            </Typography>
          </Stack>
        </Paper>

        <Box
          sx={{
            mt: 8,
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "1fr 1fr 1fr" },
            gap: 4,
          }}
        >
          <Box>
            <Typography variant="h6" fontWeight="bold">
              Direct Voting
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Vote directly on policies proposed at federal, provincial, and
              municipal levels.
            </Typography>
          </Box>
          <Box>
            <Typography variant="h6" fontWeight="bold">
              Engage
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Connect with verified politicians and see where they stand on key
              issues.
            </Typography>
          </Box>
          <Box>
            <Typography variant="h6" fontWeight="bold">
              Transparent
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Real-time data on citizen sentiment and policy support across
              Canada.
            </Typography>
          </Box>
        </Box>
      </Box>
    </Container>
  );
};

export default LandingPage;

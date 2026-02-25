import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

const domain = process.env.REACT_APP_AUTH0_DOMAIN || '';
const clientId = process.env.REACT_APP_AUTH0_CLIENT_ID || '';
const audience = process.env.REACT_APP_AUTH0_AUDIENCE || '';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#e0e4e8',
      paper: '#ffffff',
    },
    action: {
      hover: 'rgba(0, 0, 0, 0)', // Make default hover transparent
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          '&:hover': {
            opacity: 0.8,
          },
        },
        contained: {
          '&:hover': {
            backgroundColor: '#1976d2', // Keep primary color
          },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          '&:hover': {
            opacity: 0.8,
          },
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          '&:hover': {
            opacity: 0.8,
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          '&:hover': {
            opacity: 0.8,
          },
        },
      },
    },
  },
});

const rootElement = document.getElementById('root');
if (rootElement) {
  const root = ReactDOM.createRoot(rootElement);
  root.render(
    <React.StrictMode>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Auth0Provider
          domain={domain}
          clientId={clientId}
          authorizationParams={{
            redirect_uri: window.location.origin,
            audience: audience,
            scope: "openid profile email read:policies write:policies read:citizens write:citizens read:polls read:opinions write:opinions read:votes write:votes read:self write:self write:declare-politician read:verify-politician write:verify-politician write:political-parties read:political-parties read:geo",
          }}
        >
          <App />
        </Auth0Provider>
      </ThemeProvider>
    </React.StrictMode>
  );
}

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

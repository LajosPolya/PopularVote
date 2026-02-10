import { render, screen, act } from '@testing-library/react';
import App from './App';
import { useAuth0 } from '@auth0/auth0-react';

jest.mock('@auth0/auth0-react');

const mockUser = {
  name: 'Test User',
  email: 'test@example.com',
  sub: 'auth0|123',
};

// Mock fetch
global.fetch = jest.fn();

test('renders header', async () => {
  useAuth0.mockReturnValue({
    isLoading: false,
    isAuthenticated: true,
    user: mockUser,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn().mockResolvedValue('fake-token'),
  });

  fetch.mockResolvedValue({
    status: 204,
    ok: true,
  });

  // Mock atob
  global.atob = jest.fn().mockReturnValue(JSON.stringify({ scope: '' }));

  await act(async () => {
    render(<App />);
  });

  const headerElement = screen.getByText(/Popular Vote System/i);
  expect(headerElement).toBeInTheDocument();
  expect(screen.getByText(/Welcome, Test User!/i)).toBeInTheDocument();
});

test('renders loading state', async () => {
  useAuth0.mockReturnValue({
    isLoading: true,
    isAuthenticated: false,
    user: null,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn(),
  });

  await act(async () => {
    render(<App />);
  });
  
  expect(screen.getByText(/Loading.../i)).toBeInTheDocument();
});

test('renders redirecting to login when not authenticated', async () => {
  const loginWithRedirect = jest.fn();
  useAuth0.mockReturnValue({
    isLoading: false,
    isAuthenticated: false,
    user: null,
    loginWithRedirect,
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn(),
  });

  await act(async () => {
    render(<App />);
  });

  expect(loginWithRedirect).toHaveBeenCalled();
});

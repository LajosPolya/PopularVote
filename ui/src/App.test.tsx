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
(global as any).fetch = jest.fn();

test('renders header', async () => {
  (useAuth0 as jest.Mock).mockReturnValue({
    isLoading: false,
    isAuthenticated: true,
    user: mockUser,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn().mockResolvedValue('fake-token'),
  });

  (global.fetch as jest.Mock).mockImplementation((url) => {
    if (url.includes('/policies') || url.includes('/political-parties')) {
      return Promise.resolve({
        status: 200,
        ok: true,
        json: () => Promise.resolve({ content: [], totalPages: 0 }),
      });
    }
    return Promise.resolve({
      status: 200,
      ok: true,
      json: () => Promise.resolve([]),
    });
  });

  // Mock atob
  (global as any).atob = jest.fn().mockReturnValue(JSON.stringify({ scope: '' }));

  await act(async () => {
    render(<App />);
  });

  const headerElement = screen.getByText(/Popular Vote System/i);
  expect(headerElement).toBeInTheDocument();
  expect(screen.getByText(/Welcome, Test User!/i)).toBeInTheDocument();
});

test('renders Parties button when permission is present', async () => {
  (useAuth0 as jest.Mock).mockReturnValue({
    isLoading: false,
    isAuthenticated: true,
    user: mockUser,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn().mockResolvedValue('fake-token'),
  });

  (global.fetch as jest.Mock).mockImplementation((url) => {
    if (url.includes('/policies') || url.includes('/political-parties')) {
      return Promise.resolve({
        status: 200,
        ok: true,
        json: () => Promise.resolve({ content: [], totalPages: 0 }),
      });
    }
    return Promise.resolve({
      status: 200,
      ok: true,
      json: () => Promise.resolve([]),
    });
  });

  // Mock atob with read:political-parties permission
  (global as any).atob = jest.fn().mockReturnValue(JSON.stringify({ scope: 'read:political-parties' }));

  await act(async () => {
    render(<App />);
  });

  expect(screen.getByText(/Parties/i)).toBeInTheDocument();
});

test('does not render Parties button when permission is missing', async () => {
  (useAuth0 as jest.Mock).mockReturnValue({
    isLoading: false,
    isAuthenticated: true,
    user: mockUser,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn().mockResolvedValue('fake-token'),
  });

  (global.fetch as jest.Mock).mockImplementation((url) => {
    if (url.includes('/policies') || url.includes('/political-parties')) {
      return Promise.resolve({
        status: 200,
        ok: true,
        json: () => Promise.resolve({ content: [], totalPages: 0 }),
      });
    }
    return Promise.resolve({
      status: 200,
      ok: true,
      json: () => Promise.resolve([]),
    });
  });

  // Mock atob without read:political-parties permission
  (global as any).atob = jest.fn().mockReturnValue(JSON.stringify({ scope: 'read:policies' }));

  await act(async () => {
    render(<App />);
  });

  expect(screen.queryByText(/Parties/i)).not.toBeInTheDocument();
});

test('renders Create Party button when write:political-parties permission is present', async () => {
  (useAuth0 as jest.Mock).mockReturnValue({
    isLoading: false,
    isAuthenticated: true,
    user: mockUser,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn().mockResolvedValue('fake-token'),
  });

  (global.fetch as jest.Mock).mockImplementation((url) => {
    if (url.includes('/policies') || url.includes('/political-parties')) {
      return Promise.resolve({
        status: 200,
        ok: true,
        json: () => Promise.resolve({ content: [], totalPages: 0 }),
      });
    }
    return Promise.resolve({
      status: 200,
      ok: true,
      json: () => Promise.resolve([]),
    });
  });

  // Mock atob with read:political-parties and write:political-parties permissions
  (global as any).atob = jest.fn().mockReturnValue(JSON.stringify({ scope: 'read:political-parties write:political-parties' }));

  await act(async () => {
    render(<App />);
  });

  // Navigate to Parties view
  const partiesButton = screen.getByText(/Parties/i);
  await act(async () => {
    partiesButton.click();
  });

  expect(screen.getByText(/Create Party/i)).toBeInTheDocument();
});

test('does not render Create Party button when write:political-parties permission is missing', async () => {
  (useAuth0 as jest.Mock).mockReturnValue({
    isLoading: false,
    isAuthenticated: true,
    user: mockUser,
    loginWithRedirect: jest.fn(),
    logout: jest.fn(),
    getAccessTokenSilently: jest.fn().mockResolvedValue('fake-token'),
  });

  (global.fetch as jest.Mock).mockImplementation((url) => {
    if (url.includes('/policies') || url.includes('/political-parties')) {
      return Promise.resolve({
        status: 200,
        ok: true,
        json: () => Promise.resolve({ content: [], totalPages: 0 }),
      });
    }
    return Promise.resolve({
      status: 200,
      ok: true,
      json: () => Promise.resolve([]),
    });
  });

  // Mock atob with only read:political-parties permission
  (global as any).atob = jest.fn().mockReturnValue(JSON.stringify({ scope: 'read:political-parties' }));

  await act(async () => {
    render(<App />);
  });

  // Navigate to Parties view
  const partiesButton = screen.getByText(/Parties/i);
  await act(async () => {
    partiesButton.click();
  });

  expect(screen.queryByText(/Create Party/i)).not.toBeInTheDocument();
});

test('renders loading state', async () => {
  (useAuth0 as jest.Mock).mockReturnValue({
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
  (useAuth0 as jest.Mock).mockReturnValue({
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

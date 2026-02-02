import { render, screen } from '@testing-library/react';
import App from './App';

test('renders header', () => {
  render(<App />);
  const headerElement = screen.getByText(/Popular Vote System/i);
  expect(headerElement).toBeInTheDocument();
});

test('renders sign in page by default', () => {
  render(<App />);
  const signInHeader = screen.getByRole('heading', { name: /Sign In/i });
  expect(signInHeader).toBeInTheDocument();
  
  expect(screen.getByLabelText(/First Name:/i)).toBeInTheDocument();
  expect(screen.getByLabelText(/Last Name:/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Sign In/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Sign Up/i })).toBeInTheDocument();
});

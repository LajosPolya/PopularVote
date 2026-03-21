import { useAuth0 } from "@auth0/auth0-react";
import { fireEvent, render, screen } from "@testing-library/react";
import LandingPage from "./LandingPage";

jest.mock("@auth0/auth0-react");

describe("LandingPage Component", () => {
  const mockLoginWithRedirect = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      loginWithRedirect: mockLoginWithRedirect,
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders landing page content", () => {
    render(<LandingPage />);

    expect(screen.getByText("Popular Vote")).toBeInTheDocument();
    expect(
      screen.getByText(/Empowering citizens to participate directly/i),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Log In \/ Sign Up/i }),
    ).toBeInTheDocument();
  });

  test("calls loginWithRedirect when login button is clicked", () => {
    render(<LandingPage />);

    const loginButton = screen.getByRole("button", {
      name: /Log In \/ Sign Up/i,
    });
    fireEvent.click(loginButton);

    expect(mockLoginWithRedirect).toHaveBeenCalled();
  });
});

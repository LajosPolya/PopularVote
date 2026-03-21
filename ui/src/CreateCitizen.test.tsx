import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import CreateCitizen from "./CreateCitizen";

jest.mock("@auth0/auth0-react");

describe("CreateCitizen Component", () => {
  const mockOnCreateSuccess = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
      user: { name: "John Doe" },
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders form with initial values from user name", () => {
    render(<CreateCitizen onCreateSuccess={mockOnCreateSuccess} />);

    expect(screen.getByLabelText(/Given Name/i)).toHaveValue("John");
    expect(screen.getByLabelText(/Surname/i)).toHaveValue("Doe");
  });

  test("submits form and calls onCreateSuccess on success", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({}),
    });

    render(<CreateCitizen onCreateSuccess={mockOnCreateSuccess} />);

    fireEvent.change(screen.getByLabelText(/Given Name/i), {
      target: { value: "Jane" },
    });
    fireEvent.change(screen.getByLabelText(/Surname/i), {
      target: { value: "Smith" },
    });

    await act(async () => {
      fireEvent.click(
        screen.getByRole("button", { name: /Complete Registration/i }),
      );
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/citizens/self"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({
          givenName: "Jane",
          surname: "Smith",
          middleName: null,
        }),
      }),
    );
    expect(mockOnCreateSuccess).toHaveBeenCalled();
  });

  test("displays error message on submission failure", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      json: () => Promise.resolve({ message: "Registration failed" }),
    });

    render(<CreateCitizen onCreateSuccess={mockOnCreateSuccess} />);

    await act(async () => {
      fireEvent.click(
        screen.getByRole("button", { name: /Complete Registration/i }),
      );
    });

    expect(await screen.findByText("Registration failed")).toBeInTheDocument();
    expect(mockOnCreateSuccess).not.toHaveBeenCalled();
  });
});

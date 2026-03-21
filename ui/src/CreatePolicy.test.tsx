import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import CreatePolicy from "./CreatePolicy";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("CreatePolicy Component", () => {
  const mockOnBack = jest.fn();
  const mockOnCreateSuccess = jest.fn();
  const mockPoliticalParties = new Map<number, PoliticalParty>();
  const mockSelf = {
    id: 1,
    givenName: "John",
    surname: "Doe",
    role: "POLITICIAN",
  };

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ content: [] }),
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders the create policy form", async () => {
    await act(async () => {
      render(
        <CreatePolicy
          onBack={mockOnBack}
          onCreateSuccess={mockOnCreateSuccess}
          self={mockSelf as any}
          politicalParties={mockPoliticalParties}
          levelOfPoliticsId={1}
        />,
      );
    });

    expect(screen.getByText("Create New Policy")).toBeInTheDocument();
    expect(screen.getByLabelText(/Title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
  });

  test("submits the form successfully", async () => {
    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ content: [] }), // for fetchPoliticians
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({}), // for policy creation
      });

    await act(async () => {
      render(
        <CreatePolicy
          onBack={mockOnBack}
          onCreateSuccess={mockOnCreateSuccess}
          self={mockSelf as any}
          politicalParties={mockPoliticalParties}
          levelOfPoliticsId={1}
        />,
      );
    });

    fireEvent.change(screen.getByLabelText(/Title/i), {
      target: { value: "New Policy Title" },
    });
    fireEvent.change(screen.getByLabelText(/Description/i), {
      target: { value: "New Policy Description" },
    });

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: /Create Policy/i }));
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/policies"),
      expect.objectContaining({
        method: "POST",
        body: expect.stringContaining('"title":"New Policy Title"'),
      }),
    );
    expect(mockOnCreateSuccess).toHaveBeenCalled();
  });
});

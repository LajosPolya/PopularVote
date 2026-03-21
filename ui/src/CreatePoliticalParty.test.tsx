import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import CreatePoliticalParty from "./CreatePoliticalParty";

jest.mock("@auth0/auth0-react");

describe("CreatePoliticalParty Component", () => {
  const mockOnBack = jest.fn();
  const mockOnCreateSuccess = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders the create political party form", () => {
    render(
      <CreatePoliticalParty
        onBack={mockOnBack}
        onCreateSuccess={mockOnCreateSuccess}
        levelOfPoliticsId={1}
        provinceAndTerritoryId={null}
      />,
    );

    expect(screen.getByText("Create New Political Party")).toBeInTheDocument();
    expect(screen.getByLabelText(/Party Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Hex Color/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
  });

  test("submits the form successfully", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({}),
    });

    render(
      <CreatePoliticalParty
        onBack={mockOnBack}
        onCreateSuccess={mockOnCreateSuccess}
        levelOfPoliticsId={1}
        provinceAndTerritoryId={null}
      />,
    );

    fireEvent.change(screen.getByLabelText(/Party Name/i), {
      target: { value: "New Party" },
    });
    fireEvent.change(screen.getByLabelText(/Description/i), {
      target: { value: "Party Description" },
    });

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: /Create Party/i }));
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/political-parties"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({
          displayName: "New Party",
          hexColor: "#000000",
          description: "Party Description",
          levelOfPoliticsId: 1,
          provinceAndTerritoryId: null,
        }),
      }),
    );
    expect(mockOnCreateSuccess).toHaveBeenCalled();
  });
});

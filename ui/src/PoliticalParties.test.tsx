import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import PoliticalParties from "./PoliticalParties";

jest.mock("@auth0/auth0-react");

describe("PoliticalParties Component", () => {
  const mockOnPartyClick = jest.fn();
  const mockOnCreateParty = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders political parties list", async () => {
    const mockPartiesPage = {
      content: [
        {
          id: 1,
          displayName: "Liberal Party",
          hexColor: "#FF0000",
          description: "Liberal Party of Canada",
          levelOfPoliticsId: 1,
        },
      ],
      totalPages: 1,
    };

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPartiesPage),
    });

    await act(async () => {
      render(
        <PoliticalParties
          onPartyClick={mockOnPartyClick}
          canCreateParty={true}
          onCreateParty={mockOnCreateParty}
          levelOfPoliticsId={1}
          provinceAndTerritoryId={null}
        />,
      );
    });

    expect(screen.getByText("Political Parties")).toBeInTheDocument();
    expect(await screen.findByText("Liberal Party")).toBeInTheDocument();
    expect(screen.getByText("Liberal Party of Canada")).toBeInTheDocument();
  });

  test("calls onPartyClick when a party is clicked", async () => {
    const mockPartiesPage = {
      content: [
        {
          id: 1,
          displayName: "Liberal Party",
          hexColor: "#FF0000",
          description: "Liberal Party of Canada",
          levelOfPoliticsId: 1,
        },
      ],
      totalPages: 1,
    };

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPartiesPage),
    });

    await act(async () => {
      render(
        <PoliticalParties
          onPartyClick={mockOnPartyClick}
          canCreateParty={true}
          onCreateParty={mockOnCreateParty}
          levelOfPoliticsId={1}
          provinceAndTerritoryId={null}
        />,
      );
    });

    fireEvent.click(await screen.findByText("Liberal Party"));
    expect(mockOnPartyClick).toHaveBeenCalledWith(1);
  });

  test("calls onCreateParty when create button is clicked", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve({ content: [], totalPages: 0 }),
    });

    await act(async () => {
      render(
        <PoliticalParties
          onPartyClick={mockOnPartyClick}
          canCreateParty={true}
          onCreateParty={mockOnCreateParty}
          levelOfPoliticsId={1}
          provinceAndTerritoryId={null}
        />,
      );
    });

    fireEvent.click(screen.getByRole("button", { name: /Create Party/i }));
    expect(mockOnCreateParty).toHaveBeenCalled();
  });
});

import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import PoliticianSearch from "./PoliticianSearch";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("PoliticianSearch Component", () => {
  const mockOnPoliticianClick = jest.fn();
  const mockPoliticalParties = new Map<number, PoliticalParty>([
    [
      1,
      {
        id: 1,
        displayName: "Party A",
        hexColor: "#FF0000",
        description: "Desc",
        levelOfPoliticsId: 1,
        provinceAndTerritoryId: null,
      },
    ],
  ]);

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders politicians list", async () => {
    const mockPoliticiansPage = {
      content: [
        {
          id: 10,
          givenName: "John",
          surname: "Doe",
          role: "POLITICIAN",
          politicalAffiliationId: 1,
        },
      ],
      totalPages: 1,
    };

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPoliticiansPage),
    });

    await act(async () => {
      render(
        <PoliticianSearch
          onPoliticianClick={mockOnPoliticianClick}
          levelOfPoliticsId={1}
          provinceAndTerritoryId={null}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    expect(screen.getByText(/Politicians/i)).toBeInTheDocument();
    expect(await screen.findByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("Party A")).toBeInTheDocument();
  });

  test("calls onPoliticianClick when a politician is clicked", async () => {
    const mockPoliticiansPage = {
      content: [
        {
          id: 10,
          givenName: "John",
          surname: "Doe",
          role: "POLITICIAN",
          politicalAffiliationId: 1,
        },
      ],
      totalPages: 1,
    };

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPoliticiansPage),
    });

    await act(async () => {
      render(
        <PoliticianSearch
          onPoliticianClick={mockOnPoliticianClick}
          levelOfPoliticsId={1}
          provinceAndTerritoryId={null}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    fireEvent.click(await screen.findByText("John Doe"));
    expect(mockOnPoliticianClick).toHaveBeenCalledWith(10);
  });
});

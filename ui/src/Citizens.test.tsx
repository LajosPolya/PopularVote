import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import Citizens from "./Citizens";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("Citizens Component", () => {
  const mockOnCitizenClick = jest.fn();
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

  test("renders list of citizens", async () => {
    const mockCitizens = [
      {
        id: 1,
        givenName: "John",
        surname: "Doe",
        middleName: null,
        role: "CITIZEN",
        politicalAffiliationId: 1,
      },
      {
        id: 2,
        givenName: "Jane",
        surname: "Smith",
        middleName: "Marie",
        role: "POLITICIAN",
        politicalAffiliationId: null,
      },
    ];

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockCitizens),
    });

    await act(async () => {
      render(
        <Citizens
          onCitizenClick={mockOnCitizenClick}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    expect(screen.getByText("Citizens")).toBeInTheDocument();
    expect(await screen.findByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("Jane Smith")).toBeInTheDocument();
    expect(screen.getByText("Party A")).toBeInTheDocument();
  });

  test("calls onCitizenClick when a citizen is clicked", async () => {
    const mockCitizens = [
      {
        id: 1,
        givenName: "John",
        surname: "Doe",
        middleName: null,
        role: "CITIZEN",
        politicalAffiliationId: null,
      },
    ];

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockCitizens),
    });

    await act(async () => {
      render(
        <Citizens
          onCitizenClick={mockOnCitizenClick}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    fireEvent.click(await screen.findByText("John Doe"));
    expect(mockOnCitizenClick).toHaveBeenCalledWith(1);
  });
});

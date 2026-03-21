import { useAuth0 } from "@auth0/auth0-react";
import { act, render, screen } from "@testing-library/react";
import Profile from "./Profile";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("Profile Component", () => {
  const mockOnBack = jest.fn();
  const mockOnDeclarePolitician = jest.fn();
  const mockOnPolicyClick = jest.fn();
  const mockOnPartyClick = jest.fn();
  const mockPoliticalParties = new Map<number, PoliticalParty>();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders citizen profile", async () => {
    const mockCitizen = {
      id: 1,
      givenName: "John",
      surname: "Doe",
      role: "CITIZEN",
      politicalAffiliationId: null,
    };

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockCitizen),
    });

    await act(async () => {
      render(
        <Profile
          citizenId={1}
          onBack={mockOnBack}
          onDeclarePolitician={mockOnDeclarePolitician}
          onPolicyClick={mockOnPolicyClick}
          onPartyClick={mockOnPartyClick}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    expect(await screen.findByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("Citizen")).toBeInTheDocument();
  });

  test("renders politician profile with policies", async () => {
    const mockPolitician = {
      id: 2,
      givenName: "Jane",
      surname: "Smith",
      role: "POLITICIAN",
      politicalAffiliationId: null,
    };
    const mockPolicies = [
      { id: 100, description: "Policy A", publisherName: "Jane Smith" },
    ];

    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockPolitician),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockPolicies),
      });

    await act(async () => {
      render(
        <Profile
          citizenId={2}
          onBack={mockOnBack}
          onDeclarePolitician={mockOnDeclarePolitician}
          onPolicyClick={mockOnPolicyClick}
          onPartyClick={mockOnPartyClick}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    expect(await screen.findByText("Jane Smith")).toBeInTheDocument();
    expect(screen.getByText("Politician")).toBeInTheDocument();
    expect(await screen.findByText("Policy A")).toBeInTheDocument();
  });
});

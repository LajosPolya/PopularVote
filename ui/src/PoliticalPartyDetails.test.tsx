import { useAuth0 } from "@auth0/auth0-react";
import { act, render, screen } from "@testing-library/react";
import PoliticalPartyDetails from "./PoliticalPartyDetails";

jest.mock("@auth0/auth0-react");

describe("PoliticalPartyDetails Component", () => {
  const mockOnBack = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders party details, members and policies", async () => {
    const mockParty = {
      id: 1,
      displayName: "Liberal Party",
      hexColor: "#FF0000",
      description: "Liberal Party of Canada",
      levelOfPoliticsId: 1,
    };
    const mockMembers = [
      { id: 10, givenName: "John", surname: "Doe", role: "POLITICIAN" },
    ];
    const mockPolicies = [
      {
        id: 100,
        description: "Climate Change Policy",
        publisherName: "John Doe",
      },
    ];

    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockParty),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockMembers),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockPolicies),
      });

    await act(async () => {
      render(<PoliticalPartyDetails partyId={1} onBack={mockOnBack} />);
    });

    expect(await screen.findByText("Liberal Party")).toBeInTheDocument();
    expect(screen.getByText("Liberal Party of Canada")).toBeInTheDocument();
    expect(await screen.findByText("John Doe")).toBeInTheDocument();
    expect(
      await screen.findByText("Climate Change Policy"),
    ).toBeInTheDocument();
  });
});

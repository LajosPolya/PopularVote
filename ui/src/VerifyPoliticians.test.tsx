import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import VerifyPoliticians from "./VerifyPoliticians";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("VerifyPoliticians Component", () => {
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
    window.alert = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders pending verifications and handles verification", async () => {
    const mockVerifications = [
      {
        id: 10,
        givenName: "John",
        surname: "Doe",
        role: "POLITICIAN",
        politicalAffiliationId: 1,
      },
    ];

    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockVerifications),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({}),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve([]),
      });

    await act(async () => {
      render(
        <VerifyPoliticians
          onCitizenClick={mockOnCitizenClick}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    expect(screen.getByText("Verify Politicians")).toBeInTheDocument();
    expect(await screen.findByText("John Doe")).toBeInTheDocument();

    const verifyButton = screen.getByRole("button", { name: /Verify/i });

    await act(async () => {
      fireEvent.click(verifyButton);
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/citizens/10/verify-politician"),
      expect.objectContaining({ method: "PUT" }),
    );
    expect(window.alert).toHaveBeenCalledWith(
      "Politician verified successfully!",
    );
  });
});

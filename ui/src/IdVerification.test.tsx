import { useAuth0 } from "@auth0/auth0-react";
import { act, render, screen } from "@testing-library/react";
import IdVerification from "./IdVerification";

jest.mock("@auth0/auth0-react");

describe("IdVerification Component", () => {
  const mockOnVerificationSuccess = jest.fn();
  const mockGeoData = {
    provincesAndTerritories: [
      {
        id: 1,
        name: "Ontario",
        municipalities: [
          {
            id: 10,
            name: "Toronto",
            provinceAndTerritoryId: 1,
            postalCodes: [{ id: 100, name: "M5V 2L7", municipalityId: 10 }],
          },
        ],
      },
    ],
  };

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockGeoData),
    });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders identity verification correctly", async () => {
    await act(async () => {
      render(
        <IdVerification onVerificationSuccess={mockOnVerificationSuccess} />,
      );
    });

    // Verify key UI elements are rendered
    expect(
      await screen.findByRole("heading", { name: /ID Verification/i }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/Province\/Territory/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Municipality/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Postal Code/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Verify Identity/i }),
    ).toBeInTheDocument();
  });
});

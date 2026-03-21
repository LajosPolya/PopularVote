import { useAuth0 } from "@auth0/auth0-react";
import { act, render, screen } from "@testing-library/react";
import PoliticianDeclaration from "./PoliticianDeclaration";

jest.mock("@auth0/auth0-react");

describe("PoliticianDeclaration Component", () => {
  const mockOnSuccess = jest.fn();
  const mockOnCancel = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders declaration form correctly", async () => {
    const mockLevels = [{ id: 1, name: "Federal" }];
    const mockParties = [
      { id: 10, displayName: "Party A", levelOfPoliticsId: 1 },
    ];
    const mockGeoData = {
      provincesAndTerritories: [
        {
          id: 100,
          name: "Ontario",
          electoralDistricts: [{ id: 1000, name: "District A" }],
        },
      ],
    };

    (global.fetch as jest.Mock).mockImplementation((url) => {
      if (url.includes("/political-parties")) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockParties),
        });
      }
      if (url.includes("/level-of-politics")) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockLevels),
        });
      }
      if (url.includes("/geo-data")) {
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockGeoData),
        });
      }
      return Promise.resolve({ ok: false });
    });

    await act(async () => {
      render(
        <PoliticianDeclaration
          onSuccess={mockOnSuccess}
          onCancel={mockOnCancel}
        />,
      );
    });

    // Verify key UI elements are rendered
    expect(
      await screen.findByRole("heading", { name: /Politician Verification/i }),
    ).toBeInTheDocument();
    expect(screen.getByLabelText(/Level of Politics/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Political Affiliation/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Province/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Electoral District/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Submit for Verification/i }),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Cancel/i })).toBeInTheDocument();
  });
});

import { useAuth0 } from "@auth0/auth0-react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import PoliticianDropdown from "./PoliticianDropdown";
import { Citizen, PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("PoliticianDropdown Component", () => {
  const mockOnChange = jest.fn();
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

  const mockPolitician: Citizen = {
    id: 10,
    givenName: "John",
    surname: "Doe",
    middleName: null,
    role: "POLITICIAN",
    politicalAffiliationId: 1,
    postalCodeId: null,
    postalCode: null,
  };

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders and allows searching", async () => {
    const mockPoliticiansPage = {
      content: [mockPolitician],
      totalPages: 1,
    };

    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockPoliticiansPage),
    });

    render(
      <PoliticianDropdown
        value={null}
        onChange={mockOnChange}
        politicalParties={mockPoliticalParties}
        label="Select a Politician"
      />,
    );

    const input = screen.getByLabelText("Select a Politician");
    fireEvent.focus(input);
    fireEvent.change(input, { target: { value: "John" } });

    await waitFor(() => {
      expect(screen.getByText("John Doe")).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText("John Doe"));

    expect(mockOnChange).toHaveBeenCalledWith(mockPolitician);
  });

  test("displays party name in options", async () => {
    const mockPoliticiansPage = {
      content: [mockPolitician],
      totalPages: 1,
    };

    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockPoliticiansPage),
    });

    render(
      <PoliticianDropdown
        value={null}
        onChange={mockOnChange}
        politicalParties={mockPoliticalParties}
      />,
    );

    const input = screen.getByLabelText("Select Politician");
    fireEvent.focus(input);
    fireEvent.keyDown(input, { key: "ArrowDown" });

    await waitFor(() => {
      expect(screen.getByText("Party A")).toBeInTheDocument();
    });
  });
});

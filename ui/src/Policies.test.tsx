import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen, within } from "@testing-library/react";
import Policies from "./Policies";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

const mockPoliticalParties = new Map<number, PoliticalParty>([
  [
    1,
    {
      id: 1,
      displayName: "Liberal Party",
      hexColor: "#FF0000",
      description: "Liberal Party of Canada",
      levelOfPoliticsId: 1,
      provinceAndTerritoryId: null,
    },
  ],
]);

describe("Policies Component", () => {
  const mockOnPolicyClick = jest.fn();
  const mockOnCitizenClick = jest.fn();
  const mockOnPartyClick = jest.fn();
  const mockOnCreatePolicy = jest.fn();

  beforeEach(() => {
    (useAuth0 as jest.Mock).mockReturnValue({
      getAccessTokenSilently: jest.fn().mockResolvedValue("fake-token"),
    });

    (global as any).fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          content: [],
          totalPages: 0,
        }),
    });

    // Mock atob
    (global as any).atob = jest
      .fn()
      .mockReturnValue(JSON.stringify({ scope: "" }));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test("renders Approval Status dropdown and calls API on change", async () => {
    await act(async () => {
      render(
        <Policies
          onPolicyClick={mockOnPolicyClick}
          onCitizenClick={mockOnCitizenClick}
          onPartyClick={mockOnPartyClick}
          onCreatePolicy={mockOnCreatePolicy}
          levelOfPoliticsId={null}
          provinceAndTerritoryId={null}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    // Check if the label is present
    expect(screen.getByLabelText(/Approval Status/i)).toBeInTheDocument();

    // The initial call should not have approvalStatus parameter because it is "all"
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/policies?page=0&size=10"),
      expect.any(Object),
    );
    expect(global.fetch).not.toHaveBeenCalledWith(
      expect.stringContaining("approvalStatus="),
      expect.any(Object),
    );

    // Find the select element
    const selectLabel = screen.getByLabelText(/Approval Status/i);
    const selectControl =
      selectLabel.parentElement?.querySelector(".MuiSelect-select");

    // Click to open the select
    await act(async () => {
      fireEvent.mouseDown(selectControl!);
    });

    // Find the "Approved" option in the listbox and click it
    const listbox = screen.getByRole("listbox");
    const approvedOption = within(listbox).getByText(/Approved/i);

    await act(async () => {
      fireEvent.click(approvedOption);
    });

    // Check if fetch was called with approvalStatus=APPROVED
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("approvalStatus=APPROVED"),
      expect.any(Object),
    );

    // Now select "Denied"
    await act(async () => {
      fireEvent.mouseDown(selectControl!);
    });

    const deniedOption = within(screen.getByRole("listbox")).getByText(
      /Denied/i,
    );
    await act(async () => {
      fireEvent.click(deniedOption);
    });

    // Check if fetch was called with approvalStatus=DENIED
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("approvalStatus=DENIED"),
      expect.any(Object),
    );

    // Now select "All" again
    await act(async () => {
      fireEvent.mouseDown(selectControl!);
    });

    const allOption = within(screen.getByRole("listbox")).getByText(/All/i);
    await act(async () => {
      fireEvent.click(allOption);
    });

    // Should NOT contain approvalStatus parameter when set back to "all"
    const lastCall = (global.fetch as jest.Mock).mock.calls.pop();
    expect(lastCall[0]).not.toContain("approvalStatus=");
  });

  test("renders Voting Status dropdown and calls API on change", async () => {
    await act(async () => {
      render(
        <Policies
          onPolicyClick={mockOnPolicyClick}
          onCitizenClick={mockOnCitizenClick}
          onPartyClick={mockOnPartyClick}
          onCreatePolicy={mockOnCreatePolicy}
          levelOfPoliticsId={null}
          provinceAndTerritoryId={null}
          politicalParties={mockPoliticalParties}
        />,
      );
    });

    // Check if the label is present
    expect(screen.getByLabelText(/Voting Status/i)).toBeInTheDocument();

    // Find the select element
    const selectLabel = screen.getByLabelText(/Voting Status/i);
    const selectControl =
      selectLabel.parentElement?.querySelector(".MuiSelect-select");

    // Click to open the select
    await act(async () => {
      fireEvent.mouseDown(selectControl!);
    });

    // Find the "Open" option in the listbox and click it
    const listbox = screen.getByRole("listbox");
    const openOption = within(listbox).getByText(/Open/i);

    await act(async () => {
      fireEvent.click(openOption);
    });

    // Check if fetch was called with votingStatus=OPEN
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("votingStatus=OPEN"),
      expect.any(Object),
    );

    // Now select "Closed"
    await act(async () => {
      fireEvent.mouseDown(selectControl!);
    });

    const closedOption = within(screen.getByRole("listbox")).getByText(
      /Closed/i,
    );
    await act(async () => {
      fireEvent.click(closedOption);
    });

    // Check if fetch was called with votingStatus=CLOSED
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("votingStatus=CLOSED"),
      expect.any(Object),
    );
  });
});

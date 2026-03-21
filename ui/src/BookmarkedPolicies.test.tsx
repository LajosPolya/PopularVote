import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import BookmarkedPolicies from "./BookmarkedPolicies";

jest.mock("@auth0/auth0-react");

describe("BookmarkedPolicies Component", () => {
  const mockOnPolicyClick = jest.fn();
  const mockOnCitizenClick = jest.fn();
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

  test("renders loading state and then bookmarked policies", async () => {
    const mockPolicies = [
      {
        id: 1,
        description: "Policy 1",
        publisherCitizenId: 10,
        publisherName: "John Doe",
        isBookmarked: true,
        closeDate: "2026-12-31T23:59:59",
      },
    ];

    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockPolicies),
    });

    await act(async () => {
      render(
        <BookmarkedPolicies
          onPolicyClick={mockOnPolicyClick}
          onCitizenClick={mockOnCitizenClick}
          onBack={mockOnBack}
        />,
      );
    });

    expect(screen.getByText("Bookmarked Policies")).toBeInTheDocument();
    expect(await screen.findByText("Policy 1")).toBeInTheDocument();
    expect(screen.getByText(/Published by/i)).toBeInTheDocument();
    expect(screen.getByText("John Doe")).toBeInTheDocument();
  });

  test("toggles bookmark status", async () => {
    const mockPolicies = [
      {
        id: 1,
        description: "Policy 1",
        publisherCitizenId: 10,
        publisherName: "John Doe",
        isBookmarked: true,
        closeDate: "2026-12-31T23:59:59",
      },
    ];

    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockPolicies),
      })
      .mockResolvedValueOnce({
        ok: true,
      });

    await act(async () => {
      render(
        <BookmarkedPolicies
          onPolicyClick={mockOnPolicyClick}
          onCitizenClick={mockOnCitizenClick}
          onBack={mockOnBack}
        />,
      );
    });

    const unbookmarkButton = await screen.findByRole("button", {
      name: /Remove Bookmark/i,
    });

    await act(async () => {
      fireEvent.click(unbookmarkButton);
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/policies/1/bookmark"),
      expect.objectContaining({ method: "DELETE" }),
    );
  });

  test("calls onBack when back button is clicked", async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve([]),
    });

    await act(async () => {
      render(
        <BookmarkedPolicies
          onPolicyClick={mockOnPolicyClick}
          onCitizenClick={mockOnCitizenClick}
          onBack={mockOnBack}
        />,
      );
    });

    fireEvent.click(screen.getByText(/Back to Policies/i));
    expect(mockOnBack).toHaveBeenCalled();
  });
});

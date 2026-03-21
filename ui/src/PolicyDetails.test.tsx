import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import PolicyDetails from "./PolicyDetails";
import { PoliticalParty } from "./types";

jest.mock("@auth0/auth0-react");

describe("PolicyDetails Component", () => {
  const mockOnBack = jest.fn();
  const mockOnCitizenClick = jest.fn();
  const mockOnPartyClick = jest.fn();
  const mockOnCreateOpinion = jest.fn();
  const mockOnVerifyIdentity = jest.fn();
  const mockPoliticalParties = new Map<number, PoliticalParty>();

  const mockPolicy = {
    id: 1,
    title: "Policy 1 Title",
    description: "Policy 1 Description",
    publisherName: "John Doe",
    publisherCitizenId: 10,
    publisherPoliticalPartyId: null,
    publisherPoliticalAffiliationId: null,
    levelOfPoliticsId: 1,
    closeDate: "2026-12-31T23:59:59",
    creationDate: "2026-03-20T10:00:00",
    approvalStatus: "APPROVED",
    opinions: [],
    coAuthorCitizens: [],
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

  test("renders policy details", async () => {
    (global.fetch as jest.Mock).mockImplementation((url) => {
      if (url.includes("/is-bookmarked"))
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(false),
        });
      if (url.includes("/has-voted"))
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(false),
        });
      if (url.includes("/policies/999"))
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockPolicy),
        });
      if (url.includes("/liked-opinion-ids"))
        return Promise.resolve({ ok: true, json: () => Promise.resolve([]) });
      return Promise.resolve({ ok: false });
    });

    await act(async () => {
      render(
        <PolicyDetails
          policyId={999}
          onBack={mockOnBack}
          onCitizenClick={mockOnCitizenClick}
          onPartyClick={mockOnPartyClick}
          onCreateOpinion={mockOnCreateOpinion}
          politicalParties={mockPoliticalParties}
          canWriteVotes={true}
          onVerifyIdentity={mockOnVerifyIdentity}
        />,
      );
    });

    expect(await screen.findByText(/Policy 1 Title/i)).toBeInTheDocument();
    expect(screen.getByText(/Policy 1 Description/i)).toBeInTheDocument();
    expect(screen.getByText(/John Doe/i)).toBeInTheDocument();
  });

  test("handles voting", async () => {
    (global.fetch as jest.Mock).mockImplementation((url) => {
      if (url.includes("/is-bookmarked"))
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(false),
        });
      if (url.includes("/has-voted"))
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(false),
        });
      if (url.includes("/policies/999"))
        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockPolicy),
        });
      if (url.includes("/liked-opinion-ids"))
        return Promise.resolve({ ok: true, json: () => Promise.resolve([]) });
      if (url.includes("/votes") && !url.includes("has-voted"))
        return Promise.resolve({ ok: true });
      return Promise.resolve({ ok: false });
    });

    await act(async () => {
      render(
        <PolicyDetails
          policyId={999}
          onBack={mockOnBack}
          onCitizenClick={mockOnCitizenClick}
          onPartyClick={mockOnPartyClick}
          onCreateOpinion={mockOnCreateOpinion}
          politicalParties={mockPoliticalParties}
          canWriteVotes={true}
          onVerifyIdentity={mockOnVerifyIdentity}
        />,
      );
    });

    const approveButton = await screen.findByRole("button", {
      name: /^Approve$/,
    });

    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true }); // vote response

    await act(async () => {
      fireEvent.click(approveButton);
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/votes"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ policyId: 999, selectionId: 1 }),
      }),
    );
  });
});

import { useAuth0 } from "@auth0/auth0-react";
import { act, fireEvent, render, screen } from "@testing-library/react";
import CreateOpinion from "./CreateOpinion";

jest.mock("@auth0/auth0-react");

describe("CreateOpinion Component", () => {
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

  test("renders policy and citizen info when initialPolicyId is provided", async () => {
    const mockPolicy = {
      id: 1,
      description: "Policy 1 Description",
      publisherName: "John Doe",
    };
    const mockCitizen = {
      id: 10,
      givenName: "Jane",
      surname: "Smith",
    };

    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockPolicy),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockCitizen),
      });

    await act(async () => {
      render(<CreateOpinion initialPolicyId={1} onBack={mockOnBack} />);
    });

    expect(
      screen.getByRole("heading", { name: "Create Opinion" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Policy 1 Description")).toBeInTheDocument();
    expect(screen.getByText("Jane Smith")).toBeInTheDocument();
  });

  test("submits opinion successfully", async () => {
    const mockPolicy = {
      id: 1,
      description: "Policy 1",
      publisherName: "John Doe",
    };
    const mockCitizen = { id: 10, givenName: "Jane", surname: "Smith" };

    (global.fetch as jest.Mock)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockPolicy),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockCitizen),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({}),
      });

    await act(async () => {
      render(<CreateOpinion initialPolicyId={1} onBack={mockOnBack} />);
    });

    fireEvent.change(screen.getByLabelText(/Your Opinion/i), {
      target: { value: "I agree with this policy." },
    });

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: /Create Opinion/i }));
    });

    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/opinions"),
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({
          policyId: 1,
          description: "I agree with this policy.",
        }),
      }),
    );
    expect(
      await screen.findByText(/Opinion created successfully!/i),
    ).toBeInTheDocument();
  });
});

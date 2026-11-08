import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError, api, errorMessage, saveSession } from "../api";

function mockFetch(status: number, body: unknown) {
  const fn = vi.fn().mockResolvedValue({
    status,
    ok: status >= 200 && status < 300,
    json: () => Promise.resolve(body),
  });
  vi.stubGlobal("fetch", fn);
  return fn;
}

describe("api client", () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => vi.unstubAllGlobals());

  it("sends the JWT in the Authorization header", async () => {
    saveSession({ token: "abc.def.ghi", username: "admin", name: "Admin", role: "ADMIN" });
    const fetchMock = mockFetch(200, []);
    await api.frames();
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("/api/frames");
    expect(init.headers.Authorization).toBe("Bearer abc.def.ghi");
  });

  it("sends no Authorization header without a session", async () => {
    const fetchMock = mockFetch(200, {});
    await api.login("x", "y").catch(() => undefined);
    expect(fetchMock.mock.calls[0][1].headers.Authorization).toBeUndefined();
  });

  it("raises an ApiError carrying the API message", async () => {
    mockFetch(409, { error: "CONFLICT", message: "SKU already exists" });
    await expect(api.createFrame({})).rejects.toMatchObject({ status: 409, message: "SKU already exists" });
    await expect(api.createFrame({})).rejects.toBeInstanceOf(ApiError);
  });

  it("clears the session and goes to /login on 401", async () => {
    saveSession({ token: "t", username: "u", name: "U", role: "SELLER" });
    mockFetch(401, { message: "expired" });
    const assign = vi.fn();
    vi.stubGlobal("location", { assign });
    await expect(api.patients()).rejects.toMatchObject({ status: 401 });
    expect(assign).toHaveBeenCalledWith("/login");
    expect(localStorage.getItem("visioncare.session")).toBeNull();
  });
});

describe("errorMessage", () => {
  it("explains a 403 in plain words", () => {
    expect(errorMessage(403, {})).toMatch(/permiso/);
  });
  it("joins the validation details of a 400", () => {
    expect(errorMessage(400, { details: [{ field: "sku", message: "must not be blank" }] })).toBe(
      "sku: must not be blank",
    );
  });
});

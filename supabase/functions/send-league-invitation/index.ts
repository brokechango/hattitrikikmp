import { createClient } from "jsr:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-region",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

type InvitationRequest = {
  playerId?: unknown;
  email?: unknown;
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function readDefaultApiKey(legacyVariable: string, currentVariable: string): string | null {
  const legacyKey = Deno.env.get(legacyVariable)?.trim();
  if (legacyKey) return legacyKey;

  const encodedKeys = Deno.env.get(currentVariable);
  if (!encodedKeys) return null;

  try {
    const keys = JSON.parse(encodedKeys) as Record<string, unknown>;
    const defaultKey = keys.default;
    return typeof defaultKey === "string" && defaultKey.trim().length > 0 ? defaultKey.trim() : null;
  } catch {
    console.error(`Could not read ${currentVariable}.`);
    return null;
  }
}

function response(status: number, body: Record<string, string>) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json; charset=utf-8",
    },
  });
}

Deno.serve(async (request) => {
  if (request.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  if (request.method !== "POST") {
    return response(405, { code: "method_not_allowed", message: "Method not allowed" });
  }

  const authorization = request.headers.get("Authorization")?.trim();
  if (!authorization) {
    console.warn("Invitation request rejected: authorization header missing.");
    return response(401, { code: "unauthorized", message: "Missing authorization" });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const supabaseAnonKey = readDefaultApiKey("SUPABASE_ANON_KEY", "SUPABASE_PUBLISHABLE_KEYS");
  const serviceRoleKey = readDefaultApiKey("SUPABASE_SERVICE_ROLE_KEY", "SUPABASE_SECRET_KEYS");
  if (!supabaseUrl || !supabaseAnonKey || !serviceRoleKey) {
    console.error("Supabase Edge Function environment is incomplete.");
    return response(500, { code: "configuration_error", message: "Server configuration is incomplete" });
  }

  const callerClient = createClient(supabaseUrl, supabaseAnonKey, {
    global: { headers: { Authorization: authorization } },
  });
  const { data: userData, error: userError } = await callerClient.auth.getUser();
  if (userError || !userData.user) {
    console.warn("Invitation request rejected: session is invalid.");
    return response(401, { code: "unauthorized", message: "Invalid session" });
  }

  const adminClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: { autoRefreshToken: false, persistSession: false },
  });
  const { data: callerProfile, error: callerProfileError } = await adminClient
    .from("profiles")
    .select("role, is_active")
    .eq("id", userData.user.id)
    .maybeSingle();
  if (callerProfileError) {
    console.error("Could not verify invitation sender", callerProfileError);
    return response(500, { code: "authorization_check_failed", message: "Could not verify permissions" });
  }
  if (callerProfile?.role !== "admin" || !callerProfile.is_active) {
    return response(403, { code: "forbidden", message: "Administrator permission required" });
  }

  let payload: InvitationRequest;
  try {
    payload = await request.json() as InvitationRequest;
  } catch {
    return response(400, { code: "invalid_request", message: "Expected a JSON request body" });
  }
  const email = typeof payload.email === "string" ? payload.email.trim().toLowerCase() : "";
  const playerId = typeof payload.playerId === "string" ? payload.playerId.trim() : "";
  if (!emailPattern.test(email)) {
    return response(400, { code: "invalid_email", message: "A valid email is required" });
  }
  if (!uuidPattern.test(playerId)) {
    return response(400, { code: "invalid_player", message: "An existing player is required" });
  }

  const inviteToken = crypto.randomUUID();
  const { error: inviteRecordError } = await callerClient.rpc("create_player_invite", {
    p_player_id: playerId,
    p_email: email,
    p_token: inviteToken,
  });
  if (inviteRecordError) {
    const details = inviteRecordError.message.toLowerCase();
    const conflict = details.includes("already") || details.includes("pending");
    console.error("Could not reserve player invitation", inviteRecordError);
    return response(
      conflict ? 409 : 400,
      {
        code: conflict ? "player_already_linked" : "player_invitation_failed",
        message: conflict ? "Player already linked or invited" : "Could not prepare invitation",
      },
    );
  }

  // The project's Auth Site URL is the invitation target. It must be the permanent
  // Hattitriki web origin configured in Supabase Auth URL Configuration.
  const { data: invitationData, error: invitationError } = await adminClient.auth.admin
    .inviteUserByEmail(email, { data: { invite_token: inviteToken } });
  if (invitationError || !invitationData.user) {
    await callerClient.rpc("cancel_player_invite", { p_token: inviteToken });
    const details = invitationError?.message.toLowerCase() ?? "";
    const duplicate = details.includes("already") || details.includes("registered");
    console.error("Could not create league invitation", invitationError);
    return response(
      duplicate ? 409 : 400,
      {
        code: duplicate ? "email_already_registered" : "invitation_failed",
        message: duplicate ? "User already registered" : "Could not create invitation",
      },
    );
  }

  // The Auth trigger consumes the token and links player_id before this point.
  // Only activate the expected profile: never repair a missing/mismatched link
  // with the service key.
  const { data: linkedProfile, error: linkedProfileError } = await adminClient
    .from("profiles")
    .select("player_id")
    .eq("id", invitationData.user.id)
    .maybeSingle();
  if (linkedProfileError || linkedProfile?.player_id !== playerId) {
    console.error("Invitation sent but player link was not created", linkedProfileError);
    return response(500, { code: "profile_link_failed", message: "Could not link player profile" });
  }
  const { error: profileActivationError } = await adminClient
    .from("profiles")
    .update({ role: "member", is_active: true })
    .eq("id", invitationData.user.id)
    .eq("player_id", playerId);
  if (profileActivationError) {
    console.error("Invitation sent but profile activation failed", profileActivationError);
    return response(500, { code: "profile_activation_failed", message: "Could not activate member" });
  }

  return response(200, { email, message: "Invitation sent" });
});

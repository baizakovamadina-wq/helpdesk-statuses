let accessToken = null;
let expiresAt = null;

const $ = (id) => document.getElementById(id);

// Open and close portal panels

function openPanel(id) {
    document.querySelectorAll(".panel").forEach(panel => {
        panel.hidden = true;
    });

    const panel = $(id);
    panel.hidden = false;

    panel.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

document.querySelectorAll("[data-panel]").forEach(button => {
    button.addEventListener("click", () => {
        openPanel(button.dataset.panel);
    });
});

document.querySelectorAll("[data-close]").forEach(button => {
    button.addEventListener("click", () => {
        $(button.dataset.close).hidden = true;
    });
});

// Display API responses without exposing JWT

function showResponse(status, data, success) {

    $("responseStatus").textContent = status;

    $("responseStatus").className =
        success ? "success" : "error";

    $("responseBody").textContent =
        JSON.stringify(data, null, 2);
}

// Send requests to Spring Boot

async function apiRequest(path, method = "GET", body = null) {

    const headers = {
        "X-Correlation-Id": crypto.randomUUID()
    };

    if (accessToken) {
        headers["Authorization"] = "Bearer " + accessToken;
    }

    if (body !== null) {
        headers["Content-Type"] = "application/json";
    }

    const options = {
        method: method,
        headers: headers
    };

    if (body !== null) {
        options.body = JSON.stringify(body);
    }

    try {

        const response = await fetch(path, options);

        const text = await response.text();

        let data;

        try {
            data = JSON.parse(text);
        } catch {
            data = { message: text };
        }

        // Never display the access token

        const safeData = { ...data };

        if (safeData.accessToken) {
            delete safeData.accessToken;
            safeData.message = "JWT received successfully";
        }

        showResponse(
            "HTTP " + response.status + " — " + method + " " + path,
            safeData,
            response.ok
        );

        return {
            ok: response.ok,
            status: response.status,
            data: data
        };

    } catch (error) {

        showResponse(
            "Connection error",
            {
                message: "Cannot connect to the backend."
            },
            false
        );

        return {
            ok: false,
            status: 0,
            data: {}
        };
    }
}

// Login

async function login() {

    const username = $("username").value.trim();
    const password = $("password").value;

    clearSession();

    const result = await apiRequest(
        "/api/v1/auth/login",
        "POST",
        {
            username: username,
            password: password
        }
    );

    $("password").value = "";

    if (!result.ok) {
        return;
    }

    accessToken = result.data.accessToken;

    expiresAt = Date.now() +
        result.data.expiresIn * 1000;

    const identity = await checkIdentity();

    if (identity.ok) {
        $("loginPanel").hidden = true;
        updateExpiry();
    }
}

// Check current user

async function checkIdentity() {

    const result = await apiRequest("/api/me");

    if (result.ok) {

        $("currentUser").textContent =
            result.data.username;

        $("currentRole").textContent =
            result.data.authorities.join(", ");

        $("connectionStatus").textContent =
            "Authenticated";

    } else if (result.status === 401) {

        clearSession();
    }

    return result;
}

// Clear authentication

function clearSession() {

    accessToken = null;
    expiresAt = null;

    $("currentUser").textContent = "Guest";
    $("currentRole").textContent = "None";
    $("connectionStatus").textContent = "Not authenticated";
    $("expiry").textContent = "--";
}

// Logout

function logout() {

    clearSession();

    $("username").value = "";
    $("password").value = "";

    $("displayKey").textContent = "--";
    $("displayTitle").textContent = "--";
    $("displayStatus").textContent = "--";

    $("loginPanel").hidden = true;

    showResponse(
        "Logged out",
        {
            message: "Local JWT removed successfully."
        },
        true
    );
}

// Token expiration countdown

function updateExpiry() {

    if (!accessToken || !expiresAt) {
        $("expiry").textContent = "--";
        return;
    }

    const remaining = Math.max(
        0,
        Math.ceil((expiresAt - Date.now()) / 1000)
    );

    if (remaining === 0) {

        clearSession();

        showResponse(
            "Session expired",
            {
                message: "Please sign in again."
            },
            false
        );

        return;
    }

    const minutes = Math.floor(remaining / 60);

    const seconds = String(
        remaining % 60
    ).padStart(2, "0");

    $("expiry").textContent =
        minutes + ":" + seconds;
}

setInterval(updateExpiry, 1000);

// Display ticket information

function displayTicket(ticket) {

    $("ticketId").value = ticket.id;

    $("displayKey").textContent =
        ticket.businessKey;

    $("displayTitle").textContent =
        ticket.title;

    $("displayStatus").textContent =
        ticket.status;

    if (ticket.status === "IN_PROGRESS") {

        $("displayStatus").style.background = "#e3fcef";
        $("displayStatus").style.color = "#006644";

    } else {

        $("displayStatus").style.background = "#deebff";
        $("displayStatus").style.color = "#0747a6";
    }
}

// Create a new ticket

async function createTicket() {

    const businessKey =
        $("businessKey").value.trim();

    const title =
        $("ticketTitle").value.trim();

    const result = await apiRequest(
        "/api/v1/tickets",
        "POST",
        {
            businessKey: businessKey,
            title: title
        }
    );

    if (result.ok) {

        displayTicket(result.data);

        $("businessKey").value = "";
        $("ticketTitle").value = "";

        openPanel("managePanel");
    }
}

// Find an existing ticket

async function findTicket() {

    const id = $("ticketId").value.trim();

    if (!id) {

        showResponse(
            "Ticket ID required",
            {
                message: "Please enter a ticket UUID."
            },
            false
        );

        return;
    }

    const result = await apiRequest(
        "/api/v1/tickets/" + encodeURIComponent(id)
    );

    if (result.ok) {
        displayTicket(result.data);
    }
}

// Start ticket progress

async function startTicket() {

    const id = $("ticketId").value.trim();

    if (!id) {

        showResponse(
            "Ticket ID required",
            {
                message: "Please enter a ticket UUID."
            },
            false
        );

        return;
    }

    const result = await apiRequest(
        "/api/v1/tickets/" +
        encodeURIComponent(id) +
        "/start",
        "POST"
    );

    if (result.ok) {
        displayTicket(result.data);
    }
}

// Search from the main portal

async function searchTicket() {

    const id = $("searchInput").value.trim();

    $("ticketId").value = id;

    openPanel("managePanel");

    await findTicket();
}

// Connect buttons

$("loginBtn").addEventListener("click", login);

$("logoutBtn").addEventListener("click", logout);

$("meBtn").addEventListener("click", checkIdentity);

$("createBtn").addEventListener("click", createTicket);

$("findBtn").addEventListener("click", findTicket);

$("startBtn").addEventListener("click", startTicket);

$("searchBtn").addEventListener("click", searchTicket);

// Enter key support

$("searchInput").addEventListener("keydown", event => {

    if (event.key === "Enter") {
        searchTicket();
    }
});

$("password").addEventListener("keydown", event => {

    if (event.key === "Enter") {
        login();
    }
});

// Initial state

clearSession();
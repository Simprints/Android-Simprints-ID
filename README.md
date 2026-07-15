# Simprints ID — Release Dashboard

This branch (`gh-pages`) hosts the **Release Status Dashboard** for the Simprints ID Android app.  
It is served as a GitHub Pages site at:

> **`https://releases.simprints.com`** (custom domain, see `CNAME`; also reachable at `https://simprints.github.io/Android-Simprints-ID/`)

---

## Purpose

The dashboard gives the team and stakeholders a single, always-up-to-date view of:

- Which **SID version** is live in each deployment environment (Production, Production Ready, Delivery Testing, Internal Testing / Development).
- Direct **APK download** links and **Release Notes** links for each environment.
- A full **All Releases** table listing every GitHub Release tag, title, and APK asset.
- Dedicated **per-project pages** (not linked from the main dashboard) showing which SID version a given project targets, with its own APK and release notes links — accessed via a private `?project=<slug>` URL.

---

## Files

| File | Description |
|------|-------------|
| `index.html` | The dashboard UI — a single self-contained HTML/CSS/JS page. No build step; it is served directly by GitHub Pages. |
| `status.json` | Machine-written state file that records the current version (and optional APK/release URLs) for each environment. Updated by CI. |
| `projects.csv` | Manually maintained registry of projects, their custom URL slugs, and the SID version they are pinned to. |
| `app-icon.png` | Simprints ID app icon, used as the dashboard header logo and browser favicon. |
| `CNAME` | GitHub Pages custom domain config, points the dashboard at `releases.simprints.com`. |

---

## How `status.json` is updated

`status.json` is updated automatically by CI using the reusable workflow
[`.github/workflows/reusable-update-dashboard.yml`](.github/workflows/reusable-update-dashboard.yml).

---

## How `projects.csv` is maintained

`projects.csv` is a **manually edited** CSV file with the following columns:

```
name,slug,sid_version
GG2,2r6g,v2026.2.0
eCHIS,j9u7,v2025.2.1
RAMP,cg6y,v2026.2.1
```

- **`name`** — Display name shown on the project's page.
- **`slug`** — Manually chosen, URL-safe identifier used in the `?project=<slug>` link (e.g. `https://releases.simprints.com/?project=2r6g`). Fully independent of `name`, so the URL never has to reveal or match the project name.
- **`sid_version`** — The SID release tag that this project is currently targeting (must match a GitHub Release tag exactly, e.g. `v2026.2.0`).

The main dashboard does **not** list or link to project pages — they are intentionally only reachable via their direct `?project=<slug>` URL, so the project list isn't publicly visible.

To add a new project or update a version pin, edit this file and push to `gh-pages`.

---

## How `index.html` works

The page is entirely client-side. On load it:

1. **Fetches `status.json`** (cache-busted) to populate the four environment cards at the top.
2. **Fetches all GitHub Releases** from the GitHub API (`/repos/Simprints/Android-Simprints-ID/releases`) — paginated — to build the *All Releases* table and to resolve APK/release URLs for any environment that doesn't have them pre-filled in `status.json`.
3. **Fetches `projects.csv`** (cache-busted) to build a hidden per-project page for each row. These pages are not linked from the main dashboard; each is reachable only via its own `?project=<slug>` URL and displays that project's targeted SID version with links to the corresponding GitHub Release and APK asset.

A `?project=<slug>` query parameter selects and displays that project's page instead of the main SID Releases view (useful for deep-linking to a specific project without exposing the full project list).

---

## Updating the dashboard manually

### Promote a version to Delivery Testing or Production

1. Go to **Actions → Promote Release Stage** in the GitHub repository.
2. Click **Run workflow**.
3. Select the target stage (`delivery_testing` or `production`) and enter the version number (e.g. `2026.2.0`).
4. The workflow resolves the APK URL from the GitHub Release and updates `status.json` automatically.

### Update a project's SID version pin

Edit `projects.csv` on the `gh-pages` branch directly (via the GitHub UI or a local checkout) and push.

---

## Environments explained

| Environment | Colour | Meaning |
|-------------|--------|---------|
| **Production** | 🟢 Green | Live version shipped to end users via Google Play. |
| **Production Ready** | 🟣 Purple | Technically signed off, but held pending non-technical approvals (e.g. user training, project milestones). |
| **Delivery Testing** | 🟡 Amber | Deployed to a limited tester group for final validation before promotion to Production. |

---
name: tanzu-patch-apply
description: Patch upgrade workflow for Spring applications. Applies all available patch-level dependency upgrades in one pass, optionally opening a pull request, and can explain how to exclude a dependency from patching. Use when the user asks to apply patch upgrades, update dependencies to their latest patch version, open a PR for patch upgrades, exclude/ignore a dependency from patching, or run cf repo patch-apply.
license: LICENSE.txt
version: 0.0.1-SNAPSHOT
---

## MANDATORY PREREQUISITE (execute automatically, do not skip)

Before doing anything else, execute the `/tanzu-shared` skill. Do not ask the user. Do not skip this step.

# Tanzu Patch Apply Workflow

Help the user apply patch-level dependency upgrades to their Spring application in a single pass.

Follow this workflow step by step. Stop and wait for confirmation before applying any changes.

---

## Step 1: Check Prerequisites

Run these commands to verify the environment is set up correctly:

```bash
cf help -a | grep repo
```

If the CF CLI plugin is not installed (no "repo" commands in output), stop and tell the user to install it.

Then run `cf repo -h` to discover the available subcommands. Only use commands and flags shown in this output throughout
the workflow.

## Step 2: Detect project layout

Determine whether the project root contains a single application or multiple applications (monorepo).

1. Check whether any of these files exist **at the project root**: `pom.xml`, `build.gradle` or `build.gradle.kts`.
2. **If a project indicator exists at the root**, this is a single-application project. Set
   `<selected-folder>` to empty and proceed to Step 3.
3. **If no project indicator exists at the root**, scan up to 2 levels deep for subdirectories
   containing any of these indicators: `pom.xml`, `build.gradle`, `build.gradle.kts` or `Makefile`.
4. If multiple candidate directories are found, **use the AskQuestion tool** to present them and
   ask the user which application to work with. Set `<selected-folder>` to the user's choice
   (relative path from the project root, e.g. `applications/app1`).
5. If exactly one candidate directory is found, use it as `<selected-folder>` without asking.
6. If no candidate directories are found, warn the user and proceed without a folder.

Remember `<selected-folder>` for all subsequent commands in this session.

## Step 3: Apply patch upgrades

1. Ask the user whether they also want to open a Pull Request with the upgrades. Do not assume —
   only add `--push` when the user explicitly asks for a PR.
2. If the user wants a PR, determine the git host of the project's remote:
    - If it is `github.com` or `gitlab.com`, `--scm-host` is not needed.
    - Otherwise (a self-hosted GitHub/GitLab instance), ask the user for the hostname (e.g.
      `gitlab.mycompany.com`) and pass it via `--scm-host`.
3. Change into the project directory (or `<selected-folder>` when set) before running the command:
    - Without a PR:
      ```bash
      cf repo patch-apply
      ```
    - With a PR on a public host:
      ```bash
      cf repo patch-apply --push
      ```
    - With a PR on a self-hosted host:
      ```bash
      cf repo patch-apply --push --scm-host <host>
      ```
4. Show the full command output to the user.

Tell the user what the output means:

- If the output ends with `All dependencies are up to date. No new patches available.` — all dependencies are already at
  the latest patch version; no changes were made.
- If the output ends with `Patch apply complete: N dependency(-ies) upgraded in M file(s).` — patch upgrades were
  applied; summarise the upgraded dependencies grouped by build file and scope.
- If `--push` was used, also report whether the pull request was created successfully.
- If the output contains `Deprecated configuration file detected: .spring-app-advisor.yml` — the project still uses
  the legacy config filename. Tell the user to rename it to `.advisor.yml` (support for the old name will be removed
  in a future release); the upgrade still proceeds normally.
- If the output contains `Failed to parse ...` — the `.advisor.yml` (or `.spring-app-advisor.yml`) is malformed.
  Surface the YAML error verbatim, note that no changes were made, and suggest fixing the syntax and re-running.
- If the run is skipped because `enabled: false` is set in the config — explain that patch upgrades are disabled for
  this project via `.advisor.yml`; the user must set `enabled: true` (or remove the flag) to apply upgrades.

## Excluding dependencies from patch upgrades (.advisor.yml)

If the user asks how to exclude (ignore) a dependency from being patched, explain and help edit an
`.advisor.yml` file at the project root. The schema is a superset of the Dependabot configuration
format:

```yaml
enabled: true          # set to false to skip patch upgrades entirely for this project
version: 1
updates:
    -   package-ecosystem: "maven"      # "maven" or "gradle" (matches the project's build tool)
        ignore:
            -   dependency-name: "org.springframework:spring-web"    # exact group:artifact
            -   dependency-name: "org.springframework.boot"          # group only == every artifact in the group
            -   dependency-name: "com.acme:*"                         # explicit group wildcard
            -   dependency-name: "org.other:legacy-util"
                versions: [ "2.x", "3.x" ]                              # restrict the ignore to these ranges only
```

Notes:

- `package-ecosystem` values follow Dependabot naming (`maven`, `gradle`) and are matched
  case-insensitively; add one `updates` entry per build tool the project uses.
- Ignore rules only apply to the `package-ecosystem` entry they're listed under — a `maven` rule has
  no effect on a `gradle` build and vice versa.
- `dependency-name` without a `:` matches the whole group (every artifact in it); with a `:` it
  matches the exact `group:artifact` (or a wildcard artifact like `com.acme:*`).
- `versions` is optional; when present, the dependency is only ignored for those node-semver ranges.
  Omit it to ignore the dependency for every version.
- The deprecated filename `.spring-app-advisor.yml` is also read, but prefer `.advisor.yml` for new
  projects.

After helping the user add or edit an ignore rule, tell them to re-run `cf repo patch-apply` to pick
up the change. If the re-run reports a deprecation warning, a parse failure, or a disabled project,
handle it as described under "Tell the user what the output means" above.

## Summary

After the command completes, give the user a summary of:

- How many dependencies were upgraded (if any)
- Which build files were modified
- Whether a pull request was opened (if `--push` was used)
- Next steps (e.g. run tests, review changes)

---

## Command Usage Guidelines

### When to Ask for Confirmation

**Always ask before running:**

- `cf repo patch-apply` — it modifies source files in place
- Adding `--push` — it creates a pull request; confirm this is wanted before including the flag
- Adding `--scm-host` — only ask for/pass this alongside `--push` when the remote is a self-hosted GitHub/GitLab
  instance

**No need to ask before:**

- `cf repo -h` — read-only, just show available subcommands

### Showing command output (mandatory)

After running `cf repo patch-apply`, you MUST include the full command output (or a clear, faithful
summary) in your response. Do not only say "command succeeded" or "command failed" — show the
relevant output so the user can see which dependencies were upgraded and in which files.

### Parameter Values

Run `cf repo patch-apply` from within the project directory. The command defaults the path to the
current directory; do not pass `--path` or `-p`.

DO NOT pass optional parameters (`--build-tool`, `--build-tool-jvm-args`, `--build-tool-options`,
`--debug`) unless the user explicitly requests them.

DO NOT reference, suggest, or pass `--token`, `--branch-prefix`, or `--current-branch`. These are
hidden, environment-variable-driven flags meant for CI use only — they are not shown in the
`cf repo patch-apply -h` output and should never be surfaced to the user.

### When a command fails

If `cf repo patch-apply` fails:

1. Show the full error output to the user
2. Tell the user the command failed and suggest they retry later or check their environment
3. If `--push` was used and the failure mentions a missing or invalid token, tell the user to set
   the `GIT_TOKEN_FOR_PRS` environment variable to a token with permission to create pull requests,
   then retry
4. Do NOT suggest running other commands to work around the failure
5. Do NOT invent commands or flags that are not in the `cf repo -h` output
6. Do NOT suggest installing a different plugin version
7. Just report the failure and stop

---

Start now by checking the prerequisites (Step 1).

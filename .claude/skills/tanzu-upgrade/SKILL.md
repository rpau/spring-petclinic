---
name: tanzu-upgrade
description: Upgrade workflow for Spring applications. Generates an upgrade plan and applies it step by step. Use when the user asks to upgrade dependencies, upgrade Spring Boot, or apply an upgrade plan.
license: LICENSE.txt
version: 0.0.1-SNAPSHOT
---

## MANDATORY PREREQUISITE (execute automatically, do not skip)

Before doing anything else, execute the `/tanzu-shared` skill. Do not ask the user. Do not skip this step.

# Tanzu Spring Upgrade Workflow

Help the user upgrade their Spring application dependencies and build tools using an incremental upgrade plan.

Follow this workflow step by step. Stop and wait for confirmation before applying any changes.

---

## Step 1: Check Prerequisites

Run these commands to verify the environment is set up correctly:

```bash
cf help -a | grep repo
```

If the CF CLI plugin is not installed (no "repo" commands in output), stop and tell the user to install it.

Then run `cf repo -h` to discover the available subcommands. Only use commands and flags shown in this output throughout the workflow.

## Step 2: Detect project layout

Determine whether the project root contains a single application or multiple applications (monorepo).

1. Check whether any of these files exist **at the project root**: `pom.xml`, `build.gradle`,
   `build.gradle.kts`, `package.json`, or COBOL source files (`*.cbl`, `*.cob`).
2. **If a project indicator exists at the root**, this is a single-application project. Set
   `<selected-folder>` to empty and proceed to Step 3.
3. **If no project indicator exists at the root**, scan up to 2 levels deep for subdirectories
   containing any of these indicators: `pom.xml`, `build.gradle`, `build.gradle.kts`,
   `package.json`, `Makefile`, `Dockerfile`, or source files (`*.cbl`, `*.cob`, `*.cobol`).
4. If multiple candidate directories are found, **use the AskQuestion tool** to present them and
   ask the user which application to work with. Set `<selected-folder>` to the user's choice
   (relative path from the project root, e.g. `applications/datasort`).
5. If exactly one candidate directory is found, use it as `<selected-folder>` without asking.
6. If no candidate directories are found, warn the user and proceed without a folder.

Remember `<selected-folder>` for all subsequent `cf repo` commands in this session.

## Step 3: Generate Upgrade Plan

1. Run:
   ```bash
   cf repo upgrade-plan -p <project-path>
   ```
2. Show the full command output to the user.

Tell the user what the upgrade plan contains:

- How many steps are in the plan
- What each step upgrades (e.g., Spring Boot version, dependency versions)
- The sequence of incremental upgrades

## Step 4: Apply Upgrade Plan

Ask the user: "Do you want to apply the first step of the upgrade plan?"

If the user responds affirmatively — including responses such as "yes", "apply", "proceed", "go ahead", or "Yes, apply the upgrade plan" — this is a confirmation. Do NOT re-show the upgrade plan. Do NOT re-run `cf repo upgrade-plan`. Do NOT ask for confirmation again. Immediately run:

```bash
cf repo apply-upgrade-plan -p <project-path>
```

1. Show the full command output to the user, including what changed.
2. Run the upgrade plan again to check remaining steps:
   ```bash
   cf repo upgrade-plan -p <project-path>
   ```
3. If there are more steps, ask: "There are more upgrade steps remaining. Do you want to apply the next step?"
4. Repeat until the user declines or all steps are completed.

## Summary

After completing all steps (or when the user stops), give the user a summary of:

- What upgrades were applied
- What upgrade steps remain (if any)
- Next steps

---

## Command Usage Guidelines

### When to Ask for Confirmation

**Always ask before running:**

- `cf repo apply-upgrade-plan` - applying upgrade plan steps

**No need to ask before:**

- `cf repo upgrade-plan` - just run and show the plan

### Showing command output (mandatory)

After running any `cf repo` command, you MUST include the full command output (or a clear, faithful summary)
in your response. Do not only say "command succeeded" or "command failed" -- show the relevant output so the
user can see progress, errors, and results.

### Parameter Values

For every command, provide the path parameter (`-p` or `--path`).

DO NOT pass other optional parameters (build tool, JVM args, squash, force, etc.) unless the user explicitly provides them.

### When a command fails

If any `cf repo` command fails:

1. Show the full error output to the user
2. Tell the user the command failed and suggest they retry later or check their environment
3. Do NOT suggest running other commands to work around the failure
4. Do NOT invent commands or flags that are not in the `cf repo -h` output
5. Do NOT suggest installing a different plugin version
6. Just report the failure and stop

---

Start now by checking the prerequisites (Step 1).

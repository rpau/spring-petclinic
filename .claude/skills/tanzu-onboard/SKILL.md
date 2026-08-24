---
name: tanzu-onboard
description: Complete workflow for onboarding, modernizing, and upgrading Spring applications on Tanzu Platform. Use when the user asks to onboard, modernize, upgrade, get advice, or apply recommendations for their Spring application on Tanzu Platform or Cloud Foundry.
license: LICENSE.txt
version: 0.0.1-SNAPSHOT
---

## MANDATORY FIRST ACTION — execute before anything else

**STOP. You MUST invoke the `tanzu-shared` skill right now using the Skill tool, before reading any further
instructions.**
Do not check prerequisites, do not detect the project layout, do not run any commands. Invoke `tanzu-shared` first, then
continue with the steps below.

# Tanzu Spring Modernization Workflow

Help the user modernize, upgrade, and onboard their Spring application to Tanzu Platform.

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

1. Check whether any of these files exist **at the project root**: `pom.xml`, `build.gradle`, `build.gradle.kts`,
   `package.json`, or COBOL source files (`*.cbl`, `*.cob`).
2. **If a project indicator exists at the root**, this is a single-application project. Set `<selected-folder>` to empty
   and proceed to Step 3.
3. **If no project indicator exists at the root**, scan up to 2 levels deep for subdirectories containing any of these
   indicators: `pom.xml`, `build.gradle`, `build.gradle.kts`, `package.json`, `Makefile`, `Dockerfile`, or source files
   (`*.cbl`, `*.cob`, `*.cobol`).
4. If multiple candidate directories are found, ask the user which application to work with, listing them as relative
   paths from the project root (use the AskQuestion tool if you have one, otherwise ask in plain text). Set
   `<selected-folder>` to the user's choice **exactly as they wrote it** (e.g. `apps/myapp`). **Do NOT append or strip
   path segments** -- do not "correct" the answer to the directory holding the source files.
5. If exactly one candidate directory is found, use it as `<selected-folder>` without asking.
6. If no candidate directories are found, warn the user and proceed without a folder.

Remember `<selected-folder>` for all subsequent `cf repo` commands in this session.

**Choosing the folder is NOT the end of the task.** The moment `<selected-folder>` is known -- including when the user
supplies it in a later message -- your very next action MUST be the `cf repo advice`
command in Step 3.

- **Do NOT** run any command to check, resolve or confirm the folder first (no `ls`, no `cd`, no `realpath`).
- **Do NOT** reply with the folder path, the absolute path, or a confirmation that you will use it.
- A reply that does not contain `cf repo advice` output is a failed response.

## Step 3: Get Advice recommendations

1. Run (immediately, without any preceding command):
   ```bash
   cf repo advice -p <project-path> --folder=<selected-folder>
   ```
   Omit `--folder` if `<selected-folder>` is empty (single-application project).
2. Show the results, then read them with the **Challenge categories** table below.

Tell the user what the assessment found:

- What migrations or improvements are recommended?
- Does it need Spring upgrades?

## Challenge categories

`cf repo onboard-app` and `cf repo advice` group what they find under category headers, printed as
`🤖 [ i / N ] <Category> — <title>`. Identify a category by its leading word: the `[ i / N ]` counter numbers only the
categories that are not empty, so a run without blockers starts at `[ 1 / N ] Antipatterns`. Never key off the numbers.

| Category | What you must do with its rows |
|----------|--------------------------------|
| `Blockers` | Stop. Explain each item and why it blocks onboarding, and offer nothing else. |
| `Antipatterns` | `--non-interactive` already applied the auto-fixable ones. Report what was applied and what is still open. Do not offer to apply them. |
| `Rearchitecture` | List the items and recommend the `/tanzu-modernize-app` skill. Never `cf repo apply-advice`. |
| `Tech debt` | No command applies these. Offer them in Step 5. |
| `Enhancements` | Offer them in Step 5. |

Each row ends with a fix type, and the fix type decides what is possible regardless of the category:

- `[auto-fix]` -- `cf repo apply-advice -n <name>` can apply it.
- `[manual-fix]` -- report it, never offer to apply it. `apply-advice` cannot address it.
- `[ai-assisted]` -- recommend `/tanzu-modernize-app`, never `apply-advice`.

`cf repo advice` also prints a **Legend** explaining these; `cf repo onboard-app` usually does not, so rely on the fix
type in the row itself. Never suggest onboarding while any blocker is listed.

## Step 4: Onboard application

Only if there are no blockers from previous steps and if there is no `manifest.yml` in the project (or in
`<selected-folder>` when set), suggest onboarding the application as the first action.

1. Ask the user: "Would you like to run `cf repo onboard-app`?
2. If the user replies yes, run exactly this command:
   ```bash
   cf repo onboard-app --path=<project-path> --folder=<selected-folder> --non-interactive
   ```
   Omit `--folder` if `<selected-folder>` is empty (single-application project). Then show the user the full command
   output and act on it as described in Step 5.
3. If the user replies no, skip onboard-app and proceed to next steep.

## Step 5: Act on the categories (mandatory)

Apply the **Challenge categories** table to the output of Step 4 -- or to Step 3's `cf repo advice` output if the user
declined onboarding. In order:

1. A `Blockers` section was printed: stop here. Do not offer anything from the sections below it.
2. A `Rearchitecture` section was printed: list those items and recommend `/tanzu-modernize-app` for them.
3. Offer the `[auto-fix]` rows under `Tech debt`, then those under `Enhancements`, **one item per reply**: name the
   item, say what it does, and ask whether to apply it. Wait for the answer before you move on to the next item. When
   the user accepts:
   ```bash
   cf repo apply-advice -p <project-path> -n <exact-name>
   ```
   Show the output, then offer the next item.
4. Neither section has an `[auto-fix]` row: say so and go to the Summary.

Offer one item at a time rather than the whole list at once. Each `apply-advice` run rewrites the project and can fail
on its own, so a single batched "yes" leaves both of you unsure which change the user actually approved.

## Step 6: Upgrade plan (optional)

If the user wants to upgrade dependencies or build tools:

1. Run:
   ```bash
   cf repo upgrade-plan -p <project-path>
   ```
2. Show the plan (sequence of steps).
3. Ask: "Do you want to apply the first step of the upgrade plan?"
4. If yes, run:
   ```bash
   cf repo apply-upgrade-plan -p <project-path>
   ```
5. Show what changed.

## Summary

Write the Summary **only** when Step 5 is finished: every `[auto-fix]` item under `Tech debt` and `Enhancements` was
offered and answered, or you confirmed there are none. Never write the Summary in the same reply that shows the
`cf repo onboard-app` output while items are still unoffered. When blockers were printed, report them instead -- there
is no applied work to summarize.

Then give the user a summary of:

- What was analyzed
- What changes were applied
- What recommendations remain (manual fixes, etc.)
- Whether onboard-app was completed (if applicable)
- Next steps

---

## Command Usage Guidelines

### When to Ask for Confirmation

**Always ask before running:**

- `cf repo apply-advice` - applying migrations
- `cf repo onboard-app` - executing (it applies changes and generates manifest)
- `cf repo apply-upgrade-plan` - applying upgrade plan steps

**No need to ask before:**

- `cf repo advice` - just run and show results
- `cf repo upgrade-plan` - just run and show the plan

**Order:** Run `cf repo advice` first (Step 3). Only offer `cf repo onboard-app` afterward (Step 4), and only if there
are no blockers and no `manifest.yml` exists.

### Showing command output (mandatory)

After running any `cf repo` command, you MUST include the full command output (or a clear, faithful summary) in your
response. Do not only say "command succeeded" or "command failed" -- show the relevant output so the user can see
progress, errors, and results (e.g. assessment steps, manifest path, onboarding finished).

### Parameter Values

For every command, provide the path parameter (`-p` or `--path`).

The `--folder` flag is supported by `cf repo advice` and `cf repo onboard-app`. If Step 2 identified a
`<selected-folder>` (monorepo), pass `--folder=<selected-folder>` to both. Do NOT pass `--folder` to
`cf repo apply-advice`, `cf repo upgrade-plan`, or `cf repo apply-upgrade-plan` -- they do not support it. To execute
these commands, change directories before running the command temporarily.

DO NOT pass other optional parameters (build tool, JVM args, etc.) unless the user explicitly provides them.

For `cf repo apply-advice`, use the exact advice name from `cf repo advice` output with `-n`.

For `cf repo onboard-app`, always use `--non-interactive`.

### When a command fails

If any `cf repo` command fails:

1. Show the full error output to the user
2. Tell the user the command failed and suggest they retry later or check their environment
3. Do NOT suggest running other commands to work around the failure
4. Do NOT invent commands (e.g. `cf repo assess`) or flags that are not in the `cf repo -h` output
5. Do NOT suggest installing a different plugin version
6. Just report the failure and stop

---

Start now by checking the prerequisites (Step 1).

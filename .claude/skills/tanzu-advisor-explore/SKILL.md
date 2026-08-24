---
name: tanzu-advisor-explore
description: Explore and inspect OpenRewrite recipe definitions (and Spring project mappings) using the Tanzu advisor. Use when the user asks to see, inspect, or audit an OpenRewrite recipe definition by name, or to discover Spring project mappings.
license: LICENSE.txt
version: 0.0.1-SNAPSHOT
---

## MANDATORY PREREQUISITE (execute automatically, do not skip)

Before doing anything else, execute the `/tanzu-shared` skill. Do not ask the user. Do not skip this step.

# Tanzu Advisor Explore Workflow

Help the user inspect OpenRewrite recipe definitions and discover Spring project mappings with the
Tanzu advisor. This skill is read-only — it never modifies project files.

Follow the relevant section below based on what the user asks for.

---

## Step 1: Check Prerequisites

Verify the CF CLI plugin is available:

```bash
cf help -a | grep repo
```

If no "repo" commands are listed, stop and tell the user to install the advisor CF repo plugin.
Then run `cf repo -h` to discover the available subcommands. Only use commands and flags shown in
this output.

---

## Inspect a recipe definition

When the user wants to see the definition of an OpenRewrite recipe, ask for the fully-qualified
recipe name if they have not provided it (for example `org.openrewrite.java.migrate.UpgradeToJava21`),
then run:

```bash
cf repo show-recipe <recipe-name>
```

Interpret the output and present it to the user:

- **Declarative (YAML) recipe** — the command prints the raw YAML definition. Present the YAML
  content so the user can audit, reuse, or troubleshoot the recipe.
- **Java recipe** — the command prints a message explaining the recipe is implemented in Java and its
  definition cannot be shown. Relay that message; do not attempt to fabricate a definition.
- **Recipe not found** — the command reports that no recipe matches the name. Tell the user the
  recipe was not found and offer to help them find the correct name.

---

## Discover Spring project mappings

When the user wants to download the Spring project mapping for a known project slug, ask for the slug
of the project, then run:

```bash
cf repo search-mapping --slug <slug>
```

This performs a direct, non-interactive lookup and download — it never prompts, so it is safe to run
from this skill. Interpret the output and present it to the user:

- **Mapping found** — the command prints a message containing `Mapping saved to: <path>`. Tell the user
  the mapping was downloaded and where it was saved.
- **No project matches** — the command prints a message containing `No project found with slug "<slug>" ...`.
  Tell the user no match was found and offer to try a different slug.

Do NOT run this command with only `--prefix` and no `--slug` from this skill — without a slug the
underlying command prompts interactively for a selection, which will hang waiting for input that this
skill cannot provide. If the user does not know the exact slug, tell them to run
`cf repo search-mapping --prefix <prefix>` themselves in an interactive terminal to browse and select
one, then come back with the slug. Or complete the interactive session of that command which will allow
them to obtain and download a mapping file directly.

---

## Notes

- All commands in this skill are read-only and safe to run without confirmation.
- Always use the fully-qualified recipe name when calling `show-recipe`.

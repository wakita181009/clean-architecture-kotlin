---
name: spec
description: Start SDD spec definition for a Kotlin CA/FP feature. Guides through creating a complete spec document covering domain model, use cases, infrastructure, and presentation changes.
argument-hint: [feature-name]
---

# /spec — Feature Specification Wizard

Create a SDD spec for a Kotlin Clean Architecture feature.

## What This Command Does

1. Explores the existing codebase to understand conventions
2. Delegates to the `spec-writer` agent to create a complete spec
3. Saves the spec to `.claude/specs/[feature-name].md`

## Instructions

The user has run `/spec $ARGUMENTS`. The feature name (if provided) is: `$ARGUMENTS`

If no feature name was provided, ask the user: "What feature would you like to specify? Please provide a name (kebab-case) and a brief description."

Once you have a feature name and description:

### Step 1: Explore the Project

Before writing the spec, understand the project:

1. Read `settings.gradle.kts` to identify the CA modules (domain, application, etc.)
2. Read the domain module to find: base package, existing entities, value objects, and error types
3. Read 1-2 existing use cases to understand naming patterns
4. Check if `.claude/specs/` directory already has a spec for this feature

### Step 2: Delegate to spec-writer Agent

Use the Task tool to delegate to the `spec-writer` agent with:
- The feature name and user's description
- The base package discovered
- The existing domain patterns found
- Instructions to save to `.claude/specs/[feature-name].md`

### Step 3: Present Result

After the spec is written:
1. Read the created spec file and show a summary to the user
2. Ask: "Does this spec look correct? Would you like to run `/spec-review` to evaluate it?"

## Tips

- If the user only provides a feature name, ask for 2-3 sentences of context about what the feature does
- Encourage the user to think about error cases before happy paths
- Remind the user that the spec drives tests and implementation — completeness matters

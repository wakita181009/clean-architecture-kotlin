---
name: spec-review
description: Review a SDD feature spec for completeness, CA compliance, and testability.
argument-hint: [feature-name]
---

# /spec-review — Spec Review

Evaluate a feature spec for quality and readiness to drive implementation.

## Instructions

The user has run `/spec-review $ARGUMENTS`.

### Step 1: Find the Spec

If `$ARGUMENTS` is provided, look for `.claude/specs/$ARGUMENTS.md`.
If not provided, list all files in `.claude/specs/` and ask which one to review.

If no spec files exist, inform the user: "No specs found. Run `/spec [feature-name]` to create one."

### Step 2: Delegate to spec-reviewer Agent

Use the Task tool to delegate to the `spec-reviewer` agent with:
- The full contents of the spec file
- The project's existing code patterns (domain module contents)

### Step 3: Present Review

Show the full review to the user. If there are blocking issues, ask:
"Would you like me to update the spec to fix these issues?"

If the spec is approved, suggest:
"The spec looks good. Run `/test-design $ARGUMENTS` to design the test cases."

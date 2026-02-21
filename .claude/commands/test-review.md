---
name: test-review
description: Review a test design for coverage, correctness, and TDD compliance for Kotlin CA/FP projects.
argument-hint: [feature-name]
---

# /test-review — Test Design Review

Evaluate a test design for coverage and correctness.

## Instructions

The user has run `/test-review $ARGUMENTS`.

### Step 1: Find the Test Design

Look for `.claude/specs/tests/$ARGUMENTS-tests.md`.
If not found, ask the user to run `/test-design $ARGUMENTS` first.

### Step 2: Delegate to test-reviewer Agent

Use the Task tool to delegate to the `test-reviewer` agent with:
- The test design content from `.claude/specs/tests/$ARGUMENTS-tests.md`
- The corresponding spec from `.claude/specs/$ARGUMENTS.md`

### Step 3: Present Review

Show the review result. If there are missing tests, ask:
"Would you like me to update the test design to add the missing cases?"

If approved, suggest:
"Test design looks good. Run `/impl $ARGUMENTS` to start implementation."

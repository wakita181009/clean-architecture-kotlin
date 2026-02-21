---
name: test-design
description: Design test cases from a SDD spec for Kotlin CA/FP projects. Creates a structured test plan with Kotest+MockK skeletons for domain, application, and presentation layers.
argument-hint: [feature-name]
---

# /test-design — Test Case Design

Design comprehensive test cases from a feature spec before implementation begins.

## Instructions

The user has run `/test-design $ARGUMENTS`.

### Step 1: Verify Prerequisites

Check that `.claude/specs/$ARGUMENTS.md` exists. If not:
- If `$ARGUMENTS` is empty: list spec files in `.claude/specs/` and ask which one
- If spec doesn't exist: "Spec not found. Run `/spec $ARGUMENTS` first."

### Step 2: Explore Existing Tests

Before designing tests, read existing test files to understand:
- Test class structure and import patterns
- Helper/fixture function conventions
- Test file naming and placement

### Step 3: Delegate to test-designer Agent

Use the Task tool to delegate to the `test-designer` agent with:
- The spec content from `.claude/specs/$ARGUMENTS.md`
- The existing test patterns discovered
- Instructions to save output to `.claude/specs/tests/$ARGUMENTS-tests.md`

### Step 4: Present Result

Show a summary of the test design including:
- Number of tests per layer
- Any design decisions made

Ask: "Does this test design look comprehensive? Run `/test-review $ARGUMENTS` to evaluate, or run `/impl $ARGUMENTS` to start implementation."

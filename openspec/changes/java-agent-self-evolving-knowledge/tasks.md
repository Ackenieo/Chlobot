## 1. Memory Candidate Extraction

- [ ] 1.1 Define memory candidate DTOs and lifecycle states: observed, candidate, inferred, confirmed, rule, deprecated
- [ ] 1.2 Add candidate extraction service triggered by conversation events, task completion, tool results, user corrections, and research reflection
- [ ] 1.3 Classify candidates into user profile, user preference, project preference, workflow rule, pitfall, correction, confirmed fact, behavior rule, tool usage rule, and domain knowledge
- [ ] 1.4 Add confidence scoring, source event linking, sensitivity checks, and retention policy
- [ ] 1.5 Add deduplication and merge logic for repeated candidates

## 2. User Profile

- [ ] 2.1 Define profile fact model for role, expertise, language preference, tech stack preference, communication style, decision preference, risk tolerance, tools, and confirmed constraints
- [ ] 2.2 Require user confirmation before long-term user profile facts become active
- [ ] 2.3 Add profile query, edit, delete, and audit APIs
- [ ] 2.4 Integrate confirmed profile facts with Spring AI ProfileAdvisor under explicit context budget

## 3. Pitfalls and Corrections

- [ ] 3.1 Define pitfall memory model with trigger, mistake, correction, why, howToApply, relatedProject, confidence, and source events
- [ ] 3.2 Extract pitfall candidates from user corrections, failed tasks, rejected plans, and verification failures
- [ ] 3.3 Add APIs to confirm, edit, deprecate, and query pitfalls
- [ ] 3.4 Inject relevant pitfalls through PitfallAdvisor when task context matches trigger conditions

## 4. Behavior Rule Distillation

- [ ] 4.1 Define behavior rule model with condition, action, priority, scope, enabled flag, version, and source memory ids
- [ ] 4.2 Distill behavior rule candidates from repeated confirmed preferences, repeated corrections, and high-confidence pitfalls
- [ ] 4.3 Add rule conflict detection and user override policy
- [ ] 4.4 Add rule enable, disable, rollback, and version history APIs
- [ ] 4.5 Inject behavior rules through BehaviorRuleAdvisor with traceable rule application logs

## 5. Persistence and Governance

- [ ] 5.1 Add MySQL tables for memory candidates, profile facts, pitfall memories, behavior rules, rule versions, source events, and application audit
- [ ] 5.2 Add audit logs for candidate extraction, confirmation, rejection, rule creation, rule application, and rollback
- [ ] 5.3 Prevent storage of secrets, credentials, tokens, or disallowed sensitive data
- [ ] 5.4 Add soft-delete and deprecation handling for all self-evolving knowledge entities

## 6. Verification

- [ ] 6.1 Verify candidate extraction from a conversation with curl
- [ ] 6.2 Verify user-confirmed profile fact appears in profile API and Advisor trace
- [ ] 6.3 Verify rejected candidate is not injected into future prompts
- [ ] 6.4 Verify repeated correction can generate a behavior rule candidate
- [ ] 6.5 Verify disabled or rolled-back behavior rule is not applied

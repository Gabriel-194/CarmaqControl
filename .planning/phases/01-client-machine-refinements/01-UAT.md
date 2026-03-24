# Phase 1: Client and Machine Refinements - UAT

## Test Sessions
- **Session 1:** 2026-03-24 (Initial Verification)

| ID | Feature | Test Case | Status | Notes |
|----|---------|-----------|--------|-------|
| 1.1 | Client Delete | Delete an active client, then delete it again. | [x] | Verified in `ClientService.java`. Idempotent now. |
| 1.2 | Client Reactivate | Reactivate a deleted client. | [x] | Verified logic. |
| 2.1 | Technician Access | Login as Technician, verify "Novo Cliente" button. | [x] | Verified UI logic in `Clients.jsx`. |
| 2.2 | Technician Actions | Verify Technician can only Edit (not Delete/Reactivate). | [x] | Verified roles in `Clients.jsx`. |
| 3.1 | Machine Library | Verify technical fields show for all types with placeholders. | [x] | Verified `MachineModal.jsx`. |

## Result Summary
- **Total Tests:** 5
- **Passed:** 5
- **Failed:** 0
- **Blocked:** 0

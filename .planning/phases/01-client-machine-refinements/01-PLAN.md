# Phase 1: Client and Machine Refinements - Plan

## Objective
Fix client management bugs, enable technician client registration, and finalize machine library technical fields.

## Wave 1: Backend Fixes
1. **Modify `ClientService.java`:**
   - Update `deleteClient(Long id)` to use `clientRepository.findById(id)` instead of `findClientByIdAndActive(id)`.
   - Ensure it only sets `active = false` if it's not already.
   - [ ] **Task 1.1:** Update `deleteClient` logic.
   - [ ] **Task 1.2:** Verify `reactivateClient` and other methods for consistency.

## Wave 2: Frontend UI Updates
1. **Modify `Clients.jsx`:**
   - Update `isOwner` check or add `isTecnico` to allow "Novo Cliente" button visibility.
   - [ ] **Task 2.1:** Enable "Novo Cliente" for Technicians.
2. **Review `MachineModal.jsx`:**
   - Ensure labels match the requested Brazilian Portuguese terms.
   - [ ] **Task 2.2:** Update labels and placeholders in `MachineModal.jsx`.

## Verification
- **Automated:** Run `mvn test` (if tests exist) or specifically `ClientServiceTest` (if exists).
- **Manual:**
  - Login as Technician, try to add a client.
  - Login as Owner, delete a client, then try to delete it again (should not error).
  - Check Machine Library for all 7 types and their fields.

# Phase 1: Client and Machine Refinements - Research

## 1. Client Delete/Reactivate Error
- **Current Logic:** `ClientService.java` uses `findClientByIdAndActive(id)` which filters by `active=true`. 
- **Problem:** If a client is already inactive (`active=false`), calling `deleteClient` (to ensure it's inactive) or `reactivateClient` (which uses a generic `findById` but might be inconsistent with other methods) can lead to "Client not found" errors or inconsistent states.
- **Solution:** `deleteClient` should handle already inactive clients gracefully. `reactivateClient` should use a standard `findById`. Consistency across the service is key.

## 2. Client Search & Technician Access
- **Search:** `Clients.jsx` has `searchTerm` filtering by `companyName`, `contactName`, and `cnpj`. The user wants this improved and also added to the OS creation "select".
- **Technician Access:** The backend `ClientController` already has `@PreAuthorize("hasAnyAuthority('PROPRIETARIO', 'FINANCEIRO', 'TECNICO')")` for `createClient`. The frontend `Clients.jsx` only shows "Novo Cliente" button for `PROPRIETARIO`.
- **Solution:** Enable "Novo Cliente" button for `TECNICO` in `Clients.jsx`.

## 3. Machine Library Fields
- **Current State:** `Machine.java` has many fields. `MachineModal.jsx` maps them by type. 
- **Requirement:** Ensure all types (Laser, Dobradeira, etc.) have all requested attributes:
    - **Laser:** size, kind (fechada/aberta), power. (Done)
    - **Dobradeira/Guilhotina:** size, tonnage, command. (Done)
    - **Curvadora:** size, command, force, diameter. (Done)
    - **Metaleira:** size, tonnage. (Done)
    - **Calandra:** size, command, force, diameter, roller count. (Done)
    - **Gravadora:** size, power. (Done)
- **Validation:** Ensure the UI labels and placeholders are clear.

## 4. Dependencies
- Backend: `ClientService`, `ClientController`, `MachineService`, `MachineController`.
- Frontend: `Clients.jsx`, `ClientModal.jsx`, `Machines.jsx`, `MachineModal.jsx`.

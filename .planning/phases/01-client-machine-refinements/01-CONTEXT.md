# Phase 1: Client and Machine Refinements - Context

## Phase Boundary
Deliver a more robust Client management module and a fully functional Machine Library with specific technical fields. Ensure technicians can register clients and fix existing delete/reactivate bugs.

## Implementation Decisions

### Client Module
- **Soft Delete:** Modify `ClientService` to handle already inactive clients in `deleteClient`.
- **Reactivate:** Ensure `reactivateClient` works reliably.
- **Access:** Enable "Add Client" button for `TECNICO` role in `Clients.jsx`.
- **Search:** Enhance search indexing or UI filtering if needed.

### Machine Library
- **UI:** Review `MachineModal.jsx` labels to match Portuguese requirements exactly (e.g., "Quantidade de Rolos" for Calandra).
- **Validation:** Add basic validation for specific fields in `MachineRequestDTO` (though most are optional, they should be well-formatted).

### Discretion
- **UI Styling:** Use standard project styles (vibrant green/white theme).
- **Error Messages:** Use Portuguese (Brazil) for all user-facing errors.

## Canonical References
- `src/main/java/com/example/Models/Client.java`
- `src/main/java/com/example/Service/ClientService.java`
- `carmarq-control-frontend/src/Pages/Clients.jsx`
- `carmarq-control-frontend/src/Components/Machines/MachineModal.jsx`

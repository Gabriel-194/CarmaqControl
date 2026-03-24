---
wave: 1
depends_on: []
files_modified:
  - src/main/java/com/example/Models/ServiceOrderStatus.java
  - src/main/java/com/example/Service/ServiceOrderService.java
  - carmarq-control-frontend/src/utils/statusUtils.js
  - carmarq-control-frontend/src/Pages/Ordens.jsx
  - carmarq-control-frontend/src/Pages/OrdemDetalhes.jsx
autonomous: true
---

# Phase 4: Final Polishing & Verification

## Task 1: Add COM_PROBLEMA Status Backend
<read_first>
- src/main/java/com/example/Models/ServiceOrderStatus.java
</read_first>

<action>
In `ServiceOrderStatus.java`, add `COM_PROBLEMA` to the enum list (e.g., ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA, REQUER_INSPECAO, COM_PROBLEMA).
</action>

<acceptance_criteria>
- `ServiceOrderStatus.java` contains `COM_PROBLEMA`
</acceptance_criteria>

## Task 2: DTO Data Scrubbing for Technician Role
<read_first>
- src/main/java/com/example/Service/ServiceOrderService.java
</read_first>

<action>
In `ServiceOrderService.java`:
1. Find `mapToDTO(ServiceOrder order)` and change its signature to `mapToDTO(ServiceOrder order, boolean isTechnician)`.
2. Update the calls in `getAllServiceOrders`, `getServiceOrderById`, etc., to get the role: `Authentication auth = SecurityContextHolder.getContext().getAuthentication(); boolean isTech = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("TECNICO"));`
3. In `mapToDTO`, if `isTechnician` is true, forcefully set the builder values of `totalValue(null)`, `netProfit(null)`, `discountValue(null)`, `partsValue(null)`, `expensesValue(null)`, `serviceValue(null)`. Leave `technicianPayment` intact.
</action>

<acceptance_criteria>
- `mapToDTO` takes a boolean `isTechnician` argument
- if `isTechnician` is true, `totalValue` and `netProfit` are scrubbed out by passing null
</acceptance_criteria>

## Task 3: COM_PROBLEMA Status in Frontend
<read_first>
- carmarq-control-frontend/src/utils/statusUtils.js
- carmarq-control-frontend/src/Pages/Ordens.jsx
- carmarq-control-frontend/src/Pages/OrdemDetalhes.jsx
</read_first>

<action>
1. In `statusUtils.js`, add `COM_PROBLEMA: { label: 'Com Problema', css: 'status-cancelada' },` to `statusMap`.
2. In `Ordens.jsx`, in the `<select value={statusFilter}>`, add `<option value="COM_PROBLEMA">Com Problema</option>`.
3. In `OrdemDetalhes.jsx`:
   - Under the Actions card (where `REQUER_INSPECAO` is), add:
   ```javascript
   {(osData.status === 'ABERTA' || osData.status === 'EM_ANDAMENTO') && (
       <button
           className="btn-secondary btn-full mb-2"
           style={{ color: '#ef4444', borderColor: '#ef4444' }}
           onClick={() => handleStatusChange('COM_PROBLEMA')}
       >
           <AlertTriangle size={18} /> Relatar Problema
       </button>
   )}
   ```
</action>

<acceptance_criteria>
- `statusUtils.js` contains `COM_PROBLEMA:`
- `Ordens.jsx` select contains `value="COM_PROBLEMA"`
- `OrdemDetalhes.jsx` button exists to call `handleStatusChange('COM_PROBLEMA')`
</acceptance_criteria>

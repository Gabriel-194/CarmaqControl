---
wave: 1
depends_on: []
files_modified:
  - "src/main/java/com/example/Service/ServiceOrderService.java"
  - "carmarq-control-frontend/src/Pages/NovaOS.jsx"
autonomous: true
---
# Plan: Billing Logic Implementation

<objective>
Implement the exact 250/h and 185/h rate splitting for Carmaq and Valentim, respectively, along with the R$ 85,00/h for travel and R$ 2,20 per km rules.
Compute the Technician Payment correctly (10% labor + 100% expenses).
</objective>

<task>
<read_first>
- src/main/java/com/example/Service/ServiceOrderService.java
- src/main/java/com/example/Models/ServiceOrder.java
- src/main/java/com/example/Models/TimeTracking.java
</read_first>
<action>
Modify `ServiceOrderService.java`. Locate `calculateTotal` and create a private method `recalculateValues(ServiceOrder order)`.
1. Fetch all `TimeTracking` for the OS.
2. Sum hours worked. If `order.getManutencaoOrigin() == "VALENTIM"`, value = hours * 185.00. Else if "CARMAQ", value = hours * 250.00. (Note: use duration logic).
3. If `timeTracking.getType() == "DESLOCAMENTO"`, use hours * 85.00.
4. Set `order.setServiceValue(labor_value)` and `order.setExpensesValue(expenses_db_sum + travel_time_value + km_value)`. km_value = km * 2.20.
5. In `calculateTechnicianPayment(ServiceOrder order)`, set formula to: `(order.getServiceValue() * 0.10) + order.getExpensesValue()`.
</action>
<acceptance_criteria>
- `ServiceOrderService.java` contains `public Double calculateTechnicianPayment` that uses `* 0.10` for labor.
- `ServiceOrderService.java` contains `185.00` and `250.00` logic mapped to `order.getManutencaoOrigin()`.
</acceptance_criteria>
</task>

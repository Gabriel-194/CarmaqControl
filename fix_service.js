const fs = require('fs');
let f = fs.readFileSync('src/main/java/com/example/Service/ServiceOrderService.java', 'utf8');

// Fix expenses
f = f.replace('ServiceOrder order = ServiceOrder.builder()', 
    'Double totalReimbursement = (dto.getReimbursementValue() != null ? dto.getReimbursementValue() : 0.0) + ' +
    '(dto.getFoodValue() != null ? dto.getFoodValue() : 0.0) + ' +
    '(dto.getTollValue() != null ? dto.getTollValue() : 0.0) + ' +
    '(dto.getAccommodationValue() != null ? dto.getAccommodationValue() : 0.0); \n        ServiceOrder order = ServiceOrder.builder()');

f = f.replace('.reimbursementValue(dto.getReimbursementValue() != null ? dto.getReimbursementValue() : 0.0)', 
    '.reimbursementValue(totalReimbursement)');

f = f.replace('.partsValue(0.0) // No preview de criação, peças começam em zero', 
    '.partsValue(0.0) \n                .reimbursementValue((dto.getReimbursementValue() != null ? dto.getReimbursementValue() : 0.0) + ' +
    '(dto.getFoodValue() != null ? dto.getFoodValue() : 0.0) + ' +
    '(dto.getTollValue() != null ? dto.getTollValue() : 0.0) + ' +
    '(dto.getAccommodationValue() != null ? dto.getAccommodationValue() : 0.0))');

// Fix status transitions
const oldStatusCheck = 'if (!"CONCLUIDA".equals(newStatus) && !"EM_REVISAO".equals(newStatus) && !"ABERTA".equals(newStatus)) {\r\n                throw new RuntimeException("Técnico pode apenas atualizar para: CONCLUIDA, EM_REVISAO ou ABERTA.");\r\n            }';
const newStatusCheck = 'java.util.List<String> allowedStatuses = java.util.List.of("CONCLUIDA", "EM_REVISAO", "ABERTA", "EM_ANDAMENTO", "COM_PROBLEMA", "REQUER_INSPECAO");\r\n            if (!allowedStatuses.contains(newStatus)) {\r\n                throw new RuntimeException("Técnico pode apenas atualizar para: " + String.join(", ", allowedStatuses));\r\n            }';

// Fallback for different line endings
if (!f.includes(oldStatusCheck)) {
    const oldStatusCheckLF = oldStatusCheck.replace(/\r\n/g, '\n');
    const newStatusCheckLF = newStatusCheck.replace(/\r\n/g, '\n');
    f = f.replace(oldStatusCheckLF, newStatusCheckLF);
} else {
    f = f.replace(oldStatusCheck, newStatusCheck);
}

fs.writeFileSync('src/main/java/com/example/Service/ServiceOrderService.java', f, 'utf8');

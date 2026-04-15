
with open(r'C:\Users\gabri\Downloads\CarmaqControl\src\main\java\com\example\Service\ServiceOrderService.java', 'r', encoding='utf-8') as f:
    content = f.read()

count = 0
for i, char in enumerate(content):
    if char == '{':
        count += 1
    elif char == '}':
        count -= 1
    if count < 0:
        print(f"Negative count at index {i}")
print(f"Final count: {count}")

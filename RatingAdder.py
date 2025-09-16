import os

base_path = r'C:\Users\sunma\Downloads\FINALFINALFRFINAL\ai-gen-solutions'
input_txt = r'C:\Users\sunma\Downloads\final.txt'
output_txt = r'C:\Users\sunma\Downloads\Final_IDs.txt'
missing_log = r'C:\Users\sunma\Downloads\missing_ids.txt'

# Build ID -> rating map from folder structure
id_to_rating = {}
for rating_folder in os.listdir(base_path):
    rating_path = os.path.join(base_path, rating_folder)
    if os.path.isdir(rating_path):
        for problem_id in os.listdir(rating_path):
            problem_path = os.path.join(rating_path, problem_id)
            if os.path.isdir(problem_path):
                id_to_rating[problem_id] = rating_folder

# Read the original file
with open(input_txt, 'r') as f:
    lines = f.readlines()

# Process lines and add ratings
fixed_lines = []
missing_ids = []
for line in lines:
    parts = [p.strip() for p in line.strip().split(',')]
    pid = parts[0]
    if len(parts) < 4:
        rating = id_to_rating.get(pid)
        if rating:
            while len(parts) < 3:
                parts.append('')  # pad with empty fields
            parts.append(rating)
        else:
            missing_ids.append(pid)
    fixed_lines.append(', '.join(parts))

# Write to output
with open(output_txt, 'w') as f:
    for line in fixed_lines:
        f.write(line + '\n')

# Write missing ID log
with open(missing_log, 'w') as f:
    for mid in missing_ids:
        f.write(mid + '\n')

print(f"Updated file written to {output_txt}")
print(f"Missing problem IDs written to {missing_log}")

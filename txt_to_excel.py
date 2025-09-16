import pandas as pd
from statsmodels.stats.contingency_tables import mcnemar
import itertools

# Comments GPT Generated

def convert_txt_to_excel(txt_file, excel_file):
    # Read and parse lines
    with open(txt_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    data = []
    for line in lines:
        parts = line.strip().split(',')
        if len(parts) >= 4:
            problem_id = parts[0].strip()
            prompt_type = parts[1].strip()
            verdict = parts[2].strip()
            rating = parts[3].strip()
        elif len(parts) == 3:
            problem_id = parts[0].strip()
            prompt_type = parts[1].strip()
            verdict = parts[2].strip()
            rating = ""
        else:
            continue  # skip malformed lines

        # Flag whether verdict should count
        is_valid = verdict and verdict not in ["**"]
        if not is_valid:
            correct = "❌ (Ignored)"
        else:
            correct = "✅" if verdict == "A" else "❌"

        data.append([problem_id, prompt_type, verdict, correct, is_valid, rating])

    # Create DataFrame
    df = pd.DataFrame(data, columns=["Problem ID", "Prompt Type", "Verdict", "Correct", "Counts for Accuracy", "Rating"])

    # Compute accuracy (only valid rows)
    valid_df = df[df["Counts for Accuracy"] == True]
    accuracy_df = (
        valid_df.groupby("Prompt Type")["Correct"]
        .apply(lambda x: round((x == "✅").sum() / len(x) * 100, 2))
        .reset_index()
        .rename(columns={"Correct": "Accuracy (%)"})
    )

    # Aggregate to avoid duplicate (Problem ID, Prompt Type) combinations
    aggregated = (
        valid_df.groupby(["Problem ID", "Prompt Type"])
        .agg({"Correct": lambda x: any(x == "✅")})
        .reset_index()
        .rename(columns={"Correct": "Solved"})
    )

    # Pivot to build a solved matrix
    solved_matrix = aggregated.pivot(index="Problem ID", columns="Prompt Type", values="Solved").fillna(False)

    # McNemar Test (Non-exact with correction, one-tailed, only NP vs. other prompts)
    mcnemar_results = []
    prompt_types = solved_matrix.columns.tolist()
    baseline = "NP"  # Define NP as the baseline

    # Only compare NP with each other prompt type
    for prompt in prompt_types:
        if prompt != baseline and prompt in ["CoT", "CoT-ADV", "PC"]:  # Limit to specified prompts
            # Calculate discordant counts
            a_only = ((solved_matrix[baseline]) & (~solved_matrix[prompt])).sum()  # NP only
            b_only = ((solved_matrix[prompt]) & (~solved_matrix[baseline])).sum()  # Prompt only

            # Assign prompt with more unique successes as Prompt A
            if a_only >= b_only:
                prompt_a, prompt_b = baseline, prompt
                a_wins, b_wins = a_only, b_only
            else:
                prompt_a, prompt_b = prompt, baseline
                a_wins, b_wins = b_only, a_only

            # Build contingency table
            table = [[0, b_wins], [a_wins, 0]]

            # Perform non-exact McNemar test without continuity correction
            result = mcnemar(table, exact=False, correction=False)

            # One-tailed p-value (testing if Prompt A is better than Prompt B)
            p_value = result.pvalue / 2 if a_wins >= b_wins else result.pvalue

            mcnemar_results.append([prompt_a, prompt_b, a_wins, b_wins, round(result.statistic, 4), round(p_value, 4)])

    mcnemar_df = pd.DataFrame(mcnemar_results, columns=["Prompt A", "Prompt B", "A solved only", "B solved only", "Test Statistic", "P-Value"])

    # Count combination patterns
    combination_counts = solved_matrix.apply(lambda row: ''.join([k if row[k] else '-' for k in prompt_types]), axis=1)
    combo_summary = combination_counts.value_counts().reset_index()
    combo_summary.columns = ["Solved Combination", "Problem Count"]

    # Save to Excel
    with pd.ExcelWriter(excel_file, engine='openpyxl') as writer:
        df.drop(columns=["Counts for Accuracy"]).to_excel(writer, index=False, sheet_name="Raw Data")
        accuracy_df.to_excel(writer, index=False, sheet_name="Prompt Type Accuracy")
        combo_summary.to_excel(writer, index=False, sheet_name="Solved Combinations")
        mcnemar_df.to_excel(writer, index=False, sheet_name="McNemar Test")

    print(f"✅ Created '{excel_file}' with accuracy, combinations, and one-tailed McNemar tests (non-exact, Prompt A has more unique successes) for NP vs. CoT, CoT-ADV, PC only.")

if __name__ == "__main__":
    txt_path = "Data_With_Ratings.txt"
    excel_path = "data.xlsx"
    convert_txt_to_excel(txt_path, excel_path)
import os
import json
import re
import sys
import pyperclip # For clipboard operations

# Define the base directory where AI results are stored
# This will now be set by user input
OUTPUT_AI_RESULTS_BASE_DIR = ''

def review_and_log_responses():
    """
    Asks for a prompt type and a text name, then iterates through problems
    of that type, copies Java code to clipboard, waits for user input,
    and logs the response. It skips problems already in the log file.
    It processes problems in batches of 5 for copying and then asks for responses.
    """
    print("--- Problem Review and Response Logging Script ---")

    # 0. Ask for the main directory name
    global OUTPUT_AI_RESULTS_BASE_DIR
    while True:
        user_dir_input = input("Enter the path to your main AI results directory (e.g., './_ai-gen-solutions'): ").strip()
        if user_dir_input:
            OUTPUT_AI_RESULTS_BASE_DIR = user_dir_input
            if not os.path.exists(OUTPUT_AI_RESULTS_BASE_DIR):
                print(f"Warning: Directory '{OUTPUT_AI_RESULTS_BASE_DIR}' does not exist. Please ensure the path is correct.")
                continue
            break
        else:
            print("Directory path cannot be empty. Please enter a valid path.")


    # 1. Ask for prompt type
    # Using "CoT-ADV" as per the main script's internal prompt_names
    allowed_prompt_types = ["NP", "CoT", "CoT-ADV", "PC"]
    while True:
        prompt_type = input(f"Enter the prompt type you are working on ({', '.join(allowed_prompt_types)}): ").strip()
        if prompt_type in allowed_prompt_types:
            break
        else:
            print(f"Invalid prompt type. Please choose from: {', '.join(allowed_prompt_types)}")

    # 2. Ask for text name (for the output log file)
    text_file_name = input("Enter the name for the output log file (e.g., 'review_results'): ").strip()
    if not text_file_name:
        text_file_name = "review_log" # Default if user enters nothing
    output_log_file = f"{text_file_name}.txt"

    # --- Load already processed problems from the log file ---
    already_processed = set()
    if os.path.exists(output_log_file):
        try:
            with open(output_log_file, 'r', encoding='utf-8') as f:
                for line in f:
                    parts = line.strip().split(',')
                    if len(parts) >= 2:
                        # Assuming format: problem_id,prompt_type,user_response
                        # We only need problem_id and prompt_type to identify uniqueness
                        p_id = parts[0].strip()
                        p_type = parts[1].strip()
                        already_processed.add((p_id, p_type))
            print(f"Loaded {len(already_processed)} previously processed entries from '{output_log_file}'.")
        except Exception as e:
            print(f"Warning: Could not read existing log file '{output_log_file}'. Starting fresh. Error: {e}")
            already_processed = set() # Reset if there's an issue with the file

    processed_problems_count = 0
    problem_buffer = [] # To store problems for batch processing
    BATCH_SIZE = 5

    print(f"\nStarting review for prompt type: '{prompt_type}'")
    print(f"Responses will be logged to: '{output_log_file}'\n")
    print("Press Ctrl+C at any time to exit.")

    try:
        # Open the log file in append mode. It will be created if it doesn't exist.
        with open(output_log_file, 'a', encoding='utf-8') as log_f:
            # Walk through the directory structure: _ai-gen-solutions/rating/problem_id/prompt_name/
            # Sort directories for consistent processing order
            for rating_dir in sorted(os.listdir(OUTPUT_AI_RESULTS_BASE_DIR)):
                rating_path = os.path.join(OUTPUT_AI_RESULTS_BASE_DIR, rating_dir)
                if not os.path.isdir(rating_path):
                    continue

                for problem_id_dir in sorted(os.listdir(rating_path)):
                    problem_id_path = os.path.join(rating_path, problem_id_dir)
                    if not os.path.isdir(problem_id_path):
                        continue

                    # Check if the specific prompt type directory exists for this problem
                    target_prompt_path = os.path.join(problem_id_path, prompt_type)
                    if os.path.isdir(target_prompt_path):
                        # Construct the path to the Java file
                        java_file_name = f"{prompt_type}_{problem_id_dir}.java"
                        java_file_path = os.path.join(target_prompt_path, java_file_name)

                        # --- Check if this problem+prompt combination is already processed ---
                        if (problem_id_dir, prompt_type) in already_processed:
                            print(f"Skipping {problem_id_dir} ({prompt_type}): Already processed in log.")
                            continue # Skip to the next problem

                        if os.path.exists(java_file_path):
                            processed_problems_count += 1
                            print(f"\n--- Buffering Problem: {problem_id_dir} (Rating: {rating_dir}, Prompt: {prompt_type}) ---")
                            print(f"Reading Java code from: '{java_file_path}'")

                            java_code_content = ""
                            try:
                                with open(java_file_path, 'r', encoding='utf-8') as f:
                                    java_code_content = f.read()

                                if java_code_content.strip(): # Check if content is not empty
                                    pyperclip.copy(java_code_content)
                                    print("Java code copied to clipboard. Press Enter to continue to next problem.")
                                else:
                                    print(f"Failed to copy, manually copy from path: '{java_file_path}' to your clipboard. Press Enter to continue.")
                                input() # Wait for user to press Enter after copying

                                # Store problem details in buffer
                                problem_buffer.append({
                                    'problem_id': problem_id_dir,
                                    'prompt_type': prompt_type,
                                    'java_file_path': java_file_path
                                })

                                # If buffer is full, process the batch
                                if len(problem_buffer) >= BATCH_SIZE:
                                    print("\n--- Batch Buffer Full. Please provide responses for the following problems ---")
                                    for buffered_problem in problem_buffer:
                                        print(f"\n--- Requesting response for {buffered_problem['problem_id']} ({buffered_problem['prompt_type']}) ---")
                                        user_response = input("Enter your response for this problem (e.g., 'A', 'WA', or 'exit' to quit): ").strip()

                                        if user_response.lower() == 'exit':
                                            print("\nExiting review process as requested.")
                                            return # Exit the function and thus the script

                                        log_line = f"{buffered_problem['problem_id']},{buffered_problem['prompt_type']},{user_response}\n"
                                        log_f.write(log_line)
                                        log_f.flush()
                                        print(f"Logged: {log_line.strip()}")
                                        print("=" * 80)

                                    problem_buffer = [] # Clear buffer after processing batch

                            except IOError as e:
                                print(f"Error accessing Java file '{java_file_path}': {e}. Skipping.")
                            except pyperclip.PyperclipException as e:
                                print(f"Warning: Could not copy to clipboard for '{problem_id_dir}'. Please ensure a clipboard utility is installed (e.g., 'xclip' or 'xsel' on Linux). Error: {e}")
                                # Still buffer and ask for response even if clipboard fails
                                problem_buffer.append({
                                    'problem_id': problem_id_dir,
                                    'prompt_type': prompt_type,
                                    'java_file_path': java_file_path
                                })
                                if len(problem_buffer) >= BATCH_SIZE:
                                    print("\n--- Batch Buffer Full. Please provide responses for the following problems ---")
                                    for buffered_problem in problem_buffer:
                                        print(f"\n--- Requesting response for {buffered_problem['problem_id']} ({buffered_problem['prompt_type']}) ---")
                                        user_response = input("Enter your response for this problem (e.g., 'A', 'WA', or 'exit' to quit): ").strip()
                                        if user_response.lower() == 'exit':
                                            print("\nExiting review process as requested.")
                                            return
                                        log_line = f"{buffered_problem['problem_id']},{buffered_problem['prompt_type']},{user_response}\n"
                                        log_f.write(log_line)
                                        log_f.flush()
                                        print(f"Logged: {log_line.strip()}")
                                        print("=" * 80)
                                    problem_buffer = [] # Clear buffer after processing batch
                            except Exception as e:
                                print(f"An unexpected error occurred for problem {problem_id_dir}: {e}. Skipping.")
                        else:
                            print(f"Skipping {problem_id_dir}: Java file not found at '{java_file_path}' for prompt type '{prompt_type}'.")
                    # No else here, as we only care if the specific prompt_type directory exists

            # After iterating through all problems, process any remaining in the buffer
            if problem_buffer:
                print(f"\n--- Processing Remaining {len(problem_buffer)} Problems in Buffer ---")
                for buffered_problem in problem_buffer:
                    print(f"\n--- Requesting response for {buffered_problem['problem_id']} ({buffered_problem['prompt_type']}) ---")
                    user_response = input("Enter your response for this problem (e.g., 'A', 'WA', or 'exit' to quit): ").strip()
                    if user_response.lower() == 'exit':
                        print("\nExiting review process as requested.")
                        return # Exit the function and thus the script
                    log_line = f"{buffered_problem['problem_id']},{buffered_problem['prompt_type']},{user_response}\n"
                    log_f.write(log_line)
                    log_f.flush()
                    print(f"Logged: {log_line.strip()}")
                    print("=" * 80)
                problem_buffer = [] # Clear buffer

    except KeyboardInterrupt:
        print("\nScript interrupted by user.")
    finally:
        print(f"\n--- Review process complete. Processed {processed_problems_count} problems for prompt type '{prompt_type}'. ---")
        print(f"Results saved to '{output_log_file}'")

if __name__ == "__main__":
    review_and_log_responses()

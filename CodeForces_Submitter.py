"""Clipboard-assisted manual review and verdict logging for generated Java files."""

from __future__ import annotations

from pathlib import Path
from typing import Any, Dict, List, TextIO

from cf_prompting.review import (
    ALLOWED_PROMPTS,
    append_review_entry,
    iter_review_files,
    load_processed_entries,
)

BATCH_SIZE = 5


def _ask_existing_directory() -> Path:
    while True:
        candidate = Path(
            input(
                "Enter the path to your main AI results directory (e.g., './_ai-gen-solutions'): "
            ).strip()
        )
        if candidate.is_dir():
            return candidate
        print(f"Directory '{candidate}' does not exist. Please enter a valid path.")


def _ask_prompt_type() -> str:
    while True:
        prompt = input(f"Enter the prompt type ({', '.join(ALLOWED_PROMPTS)}): ").strip()
        if prompt in ALLOWED_PROMPTS:
            return prompt
        print(f"Invalid prompt type. Please choose from: {', '.join(ALLOWED_PROMPTS)}")


def _record_batch(batch: List[Dict[str, str]], prompt: str, log_handle: TextIO) -> bool:
    print(f"\n--- Record verdicts for {len(batch)} buffered problem(s) ---")
    for problem in batch:
        problem_id = problem["problem_id"]
        verdict = input(f"Verdict for {problem_id} ({prompt}); enter 'exit' to stop: ").strip()
        if verdict.lower() == "exit":
            print("Exiting review process as requested.")
            return False
        append_review_entry(log_handle, problem_id, prompt, verdict)
        print(f"Logged: {problem_id},{prompt},{verdict}")
    batch.clear()
    return True


def review_and_log_responses() -> None:
    print("--- Problem Review and Response Logging Script ---")
    results_dir = _ask_existing_directory()
    prompt = _ask_prompt_type()
    requested_name = input("Enter the output log name (default: review_log): ").strip()
    log_path = Path(requested_name or "review_log")
    if log_path.suffix.lower() != ".txt":
        log_path = log_path.with_suffix(".txt")

    try:
        processed = load_processed_entries(log_path)
    except OSError as exc:
        print(f"Error: could not read existing log '{log_path}': {exc}")
        return
    if processed:
        print(f"Loaded {len(processed)} previously processed entries from '{log_path}'.")

    clipboard: Any = None
    try:
        import pyperclip as clipboard
    except ImportError:
        print(
            "Warning: pyperclip is unavailable; Java file paths will be shown for manual copying."
        )

    discovered_count = 0
    batch: List[Dict[str, str]] = []
    try:
        with log_path.open("a", encoding="utf-8", newline="") as log_handle:
            for rating, problem_id, java_path in iter_review_files(results_dir, prompt, processed):
                discovered_count += 1
                print(f"\n--- Problem {problem_id} (Rating: {rating}, Prompt: {prompt}) ---")
                print(f"Java file: '{java_path}'")
                try:
                    java_code = java_path.read_text(encoding="utf-8")
                except (OSError, UnicodeError) as exc:
                    print(f"Warning: could not read '{java_path}': {exc}. Skipping.")
                    continue

                if clipboard is not None and java_code.strip():
                    try:
                        clipboard.copy(java_code)
                        print("Java code copied to the clipboard.")
                    except Exception as exc:
                        print(f"Warning: clipboard copy failed ({exc}); copy from the path above.")
                else:
                    print("Copy the Java code manually from the path above.")

                input("Press Enter after reviewing/submitting this problem...")
                batch.append({"problem_id": problem_id})
                if len(batch) == BATCH_SIZE and not _record_batch(batch, prompt, log_handle):
                    return

            if batch:
                _record_batch(batch, prompt, log_handle)
    except KeyboardInterrupt:
        print("\nScript interrupted by user.")
    except OSError as exc:
        print(f"Error writing review log '{log_path}': {exc}")
        return

    print(f"\nReview complete. Found {discovered_count} unreviewed problem(s).")
    print(f"Results saved to '{log_path}'.")


if __name__ == "__main__":
    review_and_log_responses()

import os
import json
import time
import re
import sys
import shutil
import random
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor, as_completed

# Try to import AzureOpenAI, handle if not available
try:
    from openai import AzureOpenAI
    from openai import APIError, AuthenticationError, RateLimitError, OpenAIError
except ImportError:
    print("Warning: openai library not found. Please install it (`pip install openai`).")
    print("AI prompting functionality will be disabled.")
    AzureOpenAI = None # Set to None to disable AI features

# Try to import selenium, handle if not available
try:
    from selenium import webdriver
    from selenium.webdriver.chrome.service import Service
    from selenium.webdriver.common.by import By
    from selenium.webdriver.common.keys import Keys
    from selenium.webdriver import ActionChains
    from selenium.webdriver.chrome.options import Options
    import pyperclip # Used by selenium script
except ImportError:
    print("Warning: selenium or pyperclip library not found. Please install them (`pip install selenium pyperclip`).")
    print("Problem statement scraping functionality will be disabled.")
    webdriver = None # Set to None to disable scraping features

# Try to import requests, handle if not available
try:
    import requests
except ImportError:
    print("Warning: requests library not found. Please install it (`pip install requests`).")
    print("Codeforces API fetching functionality will be disabled.")
    requests = None # Set to None to disable API fetching

# --- Global Configuration (to be overridden by user input) ---
AZURE_OPENAI_API_KEY = None
AZURE_OPENAI_ENDPOINT = None
AZURE_OPENAI_API_VERSION = "2025-01-01-preview" # Default API version
AZURE_OPENAI_MODEL_NAME = "gpt-4o-mini" # Default model name

# Directories
SCRAPED_PROBLEM_STATEMENTS_DIR = './_scraped_problem_statements' # Temp dir for scraped problem files before organizing
ORGANIZED_PROBLEMS_BY_RATING_DIR = './_organized_problems_by_rating' # Final destination for problems
OUTPUT_AI_RESULTS_BASE_DIR = './_ai-gen-solutions' # Base for AI outputs

STATUS_FILE_PATH = 'solution_status.json' # To keep track of processed prompts per problem

# Multithreading for AI calls
NUM_WORKERS = 1 # Default, will be asked from user

# No MAX_RETRIES for API calls, as per request for infinite retries on specific errors.
BASE_RETRY_DELAY_SECONDS = 2
MAX_RETRY_DELAY_SECONDS = 60

# Prompts will be loaded from a JSON file
CONFIGURED_PROMPTS = {} # Will store "prompts" and "text" from user-provided JSON

# Selenium ChromeDriver path (will be asked from user if selenium is enabled)
CHROMEDRIVER_PATH = None

# --- Helper Functions ---

def load_api_config(config_file_name):
    """
    Loads API endpoint and key from a specified file.
    File format:
    line1: AZURE_OPENAI_ENDPOINT
    line2: AZURE_OPENAI_API_KEY
    """
    global AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY
    try:
        with open(config_file_name, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            if len(lines) >= 2:
                AZURE_OPENAI_ENDPOINT = lines[0].strip()
                AZURE_OPENAI_API_KEY = lines[1].strip()
                print(f"Successfully loaded API configuration from '{config_file_name}'.")
                return True
            else:
                print(f"Error: API config file '{config_file_name}' must contain at least 2 lines (endpoint, key).")
                return False
    except FileNotFoundError:
        print(f"Error: API config file '{config_file_name}' not found.")
        return False
    except Exception as e:
        print(f"Error loading API config file '{config_file_name}': {e}")
        return False

def call_openai_api(client, messages, prompt_name_for_logging, is_chained_secondary=False, log_dir=None):
    """
    Calls the OpenAI API with exponential backoff.
    Records request/response timestamps. Retries indefinitely for certain errors.
    If log_dir is provided, saves individual interaction logs there.
    """
    if not client:
        print("AI client not initialized. Skipping API call.")
        return None, None
    if not AZURE_OPENAI_API_KEY or not AZURE_OPENAI_ENDPOINT:
        print("Error: Azure OpenAI API Key or Endpoint is not set.")
        return None, None

    attempt = 0
    while True: # Infinite retries as requested
        attempt += 1
        print(f"  Calling OpenAI API for prompt '{prompt_name_for_logging}' (Attempt {attempt})...")
        request_timestamp = datetime.now().isoformat()
        try:
            api_params = {
                "messages": messages,
                "model": AZURE_OPENAI_MODEL_NAME
            }

            response = client.chat.completions.create(**api_params)
            response_timestamp = datetime.now().isoformat()

            if response and response.choices:
                ai_content = response.choices[0].message.content
                full_interaction_segment = {
                    "request_timestamp": request_timestamp,
                    "response_timestamp": response_timestamp,
                    "sent_messages": [m.model_dump() if hasattr(m, 'model_dump') else m for m in messages], # Ensure serializable
                    "received_response": response.model_dump_json(indent=2)
                }

                if log_dir:
                    os.makedirs(log_dir, exist_ok=True)
                    # Use microsecond for unique filename
                    log_filename = f"{datetime.now().strftime('%Y%m%d%H%M%S%f')}.json"
                    log_filepath = os.path.join(log_dir, log_filename)
                    try:
                        with open(log_filepath, 'w', encoding='utf-8') as f:
                            json.dump(full_interaction_segment, f, indent=4, ensure_ascii=False)
                        print(f"  Saved individual API call log to '{log_filepath}'")
                    except IOError as e:
                        print(f"  Error saving individual API call log to '{log_filepath}': {e}")

                return ai_content, full_interaction_segment
            else:
                print(
                    f"  API response did not contain expected 'choices'. Full response: {response.model_dump_json(indent=2)}")
                # If response structure is unexpected, treat as a transient error and retry.
                delay = min(MAX_RETRY_DELAY_SECONDS, BASE_RETRY_DELAY_SECONDS * (2 ** (attempt - 1)))
                jitter = random.uniform(0, delay * 0.1)
                sleep_time = delay + jitter
                print(f"  Unexpected API response for '{prompt_name_for_logging}'. Retrying in {sleep_time:.2f} seconds...")
                time.sleep(sleep_time)

        except RateLimitError as e:
            delay = min(MAX_RETRY_DELAY_SECONDS, BASE_RETRY_DELAY_SECONDS * (2 ** (attempt - 1)))
            jitter = random.uniform(0, delay * 0.1)
            sleep_time = delay + jitter
            print(f"  Rate limit hit for '{prompt_name_for_logging}'. Retrying in {sleep_time:.2f} seconds...")
            time.sleep(sleep_time)
        except (APIError, AuthenticationError, OpenAIError) as e:
            # These are typically more persistent errors, but user requested infinite retries.
            delay = min(MAX_RETRY_DELAY_SECONDS, BASE_RETRY_DELAY_SECONDS * (2 ** (attempt - 1)))
            jitter = random.uniform(0, delay * 0.1)
            sleep_time = delay + jitter
            print(f"  Error calling Azure OpenAI API: {e}. Retrying in {sleep_time:.2f} seconds...")
            time.sleep(sleep_time)
        except Exception as e:
            # Catch all other unexpected errors and retry
            delay = min(MAX_RETRY_DELAY_SECONDS, BASE_RETRY_DELAY_SECONDS * (2 ** (attempt - 1)))
            jitter = random.uniform(0, delay * 0.1)
            sleep_time = delay + jitter
            print(f"  An unexpected error occurred during API call: {e}. Retrying in {sleep_time:.2f} seconds...")
            time.sleep(sleep_time)

def extract_java_code(response_text):
    """
    Extracts Java code blocks from a given text.
    """
    java_code_blocks = re.findall(r'```java\s*([\s\S]*?)\s*```', response_text)
    if java_code_blocks:
        return "\n\n".join(java_code_blocks).strip()
    return ""

def run_chained_prompt(client, problem_content, initial_prompt_str, secondary_ai_prompt_template, max_rounds, current_prompt_name_for_logging, api_calls_log_dir_base):
    """
    Executes a chained prompting sequence.
    Records all API interactions with timestamps.
    """
    print(f"    Starting prompt chain (max {max_rounds} rounds) for {current_prompt_name_for_logging}...")
    all_interactions_log = [] # For the aggregated interaction.json

    primary_history = [
        {"role": "user", "content": f"Codeforces Problem:\n\n{problem_content}\n\n---\n\n{initial_prompt_str}"}
    ]

    last_primary_ai_response_content = None

    for i in range(1, max_rounds + 1):
        print(f"      Round {i}/{max_rounds}: Primary AI call for {current_prompt_name_for_logging}...")

        # Pass the specific log directory for this prompt's API calls
        primary_ai_response, primary_interaction = call_openai_api(client, primary_history,
                                                                   f"{current_prompt_name_for_logging}_primary_round_{i}",
                                                                   log_dir=api_calls_log_dir_base)
        if primary_interaction:
            all_interactions_log.append(primary_interaction)

        if primary_ai_response is None:
            print(f"      Primary AI failed in round {i} for {current_prompt_name_for_logging}. Aborting chain.")
            return None, all_interactions_log

        primary_history.append({"role": "assistant", "content": primary_ai_response})
        last_primary_ai_response_content = primary_ai_response

        secondary_ai_messages = primary_history.copy()
        secondary_ai_messages.append({"role": "system", "content": secondary_ai_prompt_template})

        print(f"      Round {i}/{max_rounds}: Secondary AI call for {current_prompt_name_for_logging}...")
        # Per user request: "no max on the second AI's tokens"
        secondary_ai_response, secondary_interaction = call_openai_api(
            client,
            secondary_ai_messages,
            f"{current_prompt_name_for_logging}_secondary_round_{i}",
            is_chained_secondary=True, # Indicate this is the secondary AI, so max_tokens is omitted
            log_dir=api_calls_log_dir_base
        )
        if secondary_interaction:
            all_interactions_log.append(secondary_interaction)

        if secondary_ai_response is None:
            print(f"      Secondary AI failed in round {i} for {current_prompt_name_for_logging}. Aborting chain.")
            return last_primary_ai_response_content, all_interactions_log

        secondary_ai_response_stripped = secondary_ai_response.strip()

        if secondary_ai_response_stripped.lower() == "perfect":
            print(
                f"      Secondary AI returned 'Perfect'. Chain complete after {i} rounds for {current_prompt_name_for_logging}.")
            break

        match = re.match(r'^"(.*)"$', secondary_ai_response_stripped, re.S)
        if match:
            new_prompt_for_primary = match.group(1)
            print(
                f"      Secondary AI suggested new prompt: '{new_prompt_for_primary[:50]}...' for {current_prompt_name_for_logging}.")
            primary_history.append({"role": "user", "content": new_prompt_for_primary})
        else:
            print(
                f"      Secondary AI did not return a valid prompt or 'Perfect'. Response: '{secondary_ai_response_stripped}'. Aborting chain for {current_prompt_name_for_logging}.")
            break
        time.sleep(0.5) # Small delay between rounds

    return last_primary_ai_response_content, all_interactions_log


def process_single_problem_with_ai(problem_info):
    """
    Processes a single problem using the configured AI prompts.
    Handles AI client initialization and file saving.
    """
    if not AzureOpenAI:
        print(f"AI processing disabled for problem {problem_info['problem_id']}: OpenAI client not available.")
        return problem_info['problem_id'], False

    try:
        client = AzureOpenAI(
            api_version=AZURE_OPENAI_API_VERSION,
            azure_endpoint=AZURE_OPENAI_ENDPOINT,
            api_key=AZURE_OPENAI_API_KEY,
        )
    except AuthenticationError as e:
        print(f"Thread Error: Authentication failed for problem {problem_info['problem_id']}. Details: {e}")
        return problem_info['problem_id'], False
    except Exception as e:
        print(
            f"Thread Error: Could not initialize OpenAI client for problem {problem_info['problem_id']}. Details: {e}")
        return problem_info['problem_id'], False

    problem_id = problem_info['problem_id']
    rating = problem_info['rating']
    original_problem_txt_file = problem_info['file_path']

    print(f"\n[Thread] Processing problem: {problem_id} (Rating: {rating})")

    try:
        with open(original_problem_txt_file, 'r', encoding='utf-8') as f:
            problem_content = f.read()
    except IOError as e:
        print(f"[Thread] Error reading problem file '{original_problem_txt_file}': {e}. Skipping problem.")
        return problem_id, False

    problem_processed_successfully = True

    prompts_to_run_for_this_problem = problem_info.get('prompts_to_run', list(CONFIGURED_PROMPTS['prompts']))

    for prompt_name_to_run in prompts_to_run_for_this_problem:
        prompt_index = -1
        try:
            prompt_index = CONFIGURED_PROMPTS['prompts'].index(prompt_name_to_run)
            prompt_config = CONFIGURED_PROMPTS['text'][prompt_index]
            current_prompt_name = prompt_name_to_run
        except ValueError:
            print(f"  [Thread] Warning: Prompt '{prompt_name_to_run}' not found in configured prompts. Skipping.")
            problem_processed_successfully = False
            continue
        except IndexError:
            print(f"  [Thread] Error: Prompt text for '{prompt_name_to_run}' (index {prompt_index}) is missing. Skipping.")
            problem_processed_successfully = False
            continue

        print(f"  [Thread] Processing prompt '{current_prompt_name}' for problem {problem_id}")

        ai_response_content = None
        full_interaction_data_for_problem_prompt = [] # Log for this specific prompt run

        # Define common base output directories for this problem/prompt
        rating_output_dir_for_problem = os.path.join(OUTPUT_AI_RESULTS_BASE_DIR, str(rating))
        os.makedirs(rating_output_dir_for_problem, exist_ok=True)

        problem_base_output_dir = os.path.join(rating_output_dir_for_problem, problem_id)
        os.makedirs(problem_base_output_dir, exist_ok=True)

        problem_output_subdir = os.path.join(problem_base_output_dir, current_prompt_name)
        os.makedirs(problem_output_subdir, exist_ok=True)

        # New directory for individual API call logs
        api_calls_log_dir = os.path.join(problem_output_subdir, "apicalls")
        os.makedirs(api_calls_log_dir, exist_ok=True)


        if isinstance(prompt_config, list):
            # Chained prompt
            if len(prompt_config) >= 2: # Ensure it has at least initial and secondary prompts
                initial_prompt_str = prompt_config[0]
                secondary_ai_prompt_template = prompt_config[1]

                max_rounds = 5 # Default max rounds for chained prompt if not specified
                if len(prompt_config) >= 3 and isinstance(prompt_config[2], int):
                    max_rounds = prompt_config[2]

                ai_response_content, full_interaction_data_for_problem_prompt = run_chained_prompt(
                    client,
                    problem_content,
                    initial_prompt_str,
                    secondary_ai_prompt_template,
                    max_rounds,
                    current_prompt_name,
                    api_calls_log_dir # Pass the log directory
                )
            else:
                print(f"  [Thread] Warning: Chained prompt '{current_prompt_name}' config is invalid. Skipping.")
                problem_processed_successfully = False
                continue
        else:
            # Single prompt
            user_prompt_template = prompt_config
            full_user_message = f"Codeforces Problem:\n\n{problem_content}\n\n---\n\n{user_prompt_template}"
            messages_payload = [
                {"role": "user", "content": full_user_message}
            ]

            ai_response_content, interaction_segment = call_openai_api(client, messages_payload, current_prompt_name, log_dir=api_calls_log_dir) # Pass the log directory
            if interaction_segment:
                full_interaction_data_for_problem_prompt = [interaction_segment]

        # --- File Saving Logic (for aggregated files) ---

        # Save Interaction JSON (aggregated)
        interaction_json_filename = f"{current_prompt_name}-interaction.json"
        interaction_json_path = os.path.join(problem_output_subdir, interaction_json_filename)
        try:
            json_content_to_save = full_interaction_data_for_problem_prompt if full_interaction_data_for_problem_prompt is not None else []
            with open(interaction_json_path, 'w', encoding='utf-8') as f:
                json.dump(json_content_to_save, f, ensure_ascii=False, indent=4)
            print(f"  [Thread] Saved aggregated interaction JSON to '{interaction_json_path}'")
        except IOError as e:
            print(f"  [Thread] Error saving aggregated interaction JSON to '{interaction_json_path}': {e}")
            problem_processed_successfully = False


        # Save AI Response TXT
        response_txt_filename = f"{current_prompt_name}-response.txt"
        response_txt_path = os.path.join(problem_output_subdir, response_txt_filename)
        try:
            txt_content_to_save = ai_response_content if ai_response_content is not None else ""
            with open(response_txt_path, 'w', encoding='utf-8') as f:
                f.write(txt_content_to_save)
            print(f"  [Thread] Saved AI response TXT to '{response_txt_path}'")
        except IOError as e:
            print(f"  [Thread] Error saving AI response TXT to '{response_txt_path}': {e}")
            problem_processed_successfully = False

        # Extract and Save Java Code
        java_code = extract_java_code(ai_response_content if ai_response_content is not None else "")
        java_filename = f"{current_prompt_name}-{problem_id}.java"
        java_path = os.path.join(problem_output_subdir, java_filename)
        try:
            with open(java_path, 'w', encoding='utf-8') as f:
                f.write(java_code)
            if java_code:
                print(f"  [Thread] Extracted and saved Java code to '{java_path}'")
            else:
                print(f"  [Thread] No Java code block found, created empty Java file at '{java_path}'.")
        except IOError as e:
            print(f"  [Thread] Error saving Java code to '{java_path}': {e}")
            problem_processed_successfully = False

        time.sleep(0.1)

    return problem_id, problem_processed_successfully


def format_eta(seconds):
    """Formats seconds into HH:MM:SS."""
    m, s = divmod(int(seconds), 60)
    h, m = divmod(m, 60)
    return f"{h:02d}:{m:02d}:{s:02d}"

# --- Problem Scraper / Organizer Functions ---

# Constants for the Codeforces API endpoints
CODEFORCES_PROBLEMS_API_URL = "https://codeforces.com/api/problemset.problems"
CODEFORCES_CONTESTS_API_URL = "https://codeforces.com/api/contest.list"


def fetch_codeforces_data():
    """
    Fetches all problems and contest list from the Codeforces API.
    """
    if not requests:
        print("Requests library not available. Cannot fetch data from Codeforces API.")
        return None, None

    print("Fetching problems from Codeforces API...")
    problems = []
    try:
        response_problems = requests.get(CODEFORCES_PROBLEMS_API_URL)
        response_problems.raise_for_status()
        data_problems = response_problems.json()
        if data_problems and data_problems.get("status") == "OK":
            problems = data_problems.get("result", {}).get("problems", [])
            print("Successfully fetched problems.")
        else:
            print(f"Error fetching problems: API status not OK. Response: {data_problems}")
            return None, None
    except requests.exceptions.RequestException as e:
        print(f"Error fetching problems from Codeforces API: {e}")
        return None, None
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON response for problems: {e}")
        return None, None

    print("Fetching contest list from Codeforces API...")
    contest_times = {}
    try:
        response_contests = requests.get(CODEFORCES_CONTESTS_API_URL)
        response_contests.raise_for_status()
        data_contests = response_contests.json()
        if data_contests and data_contests.get("status") == "OK":
            for contest in data_contests.get("result", []):
                if 'id' in contest and 'startTimeSeconds' in contest:
                    contest_times[contest['id']] = contest['startTimeSeconds']
            print("Successfully fetched contest list.")
        else:
            print(f"Error fetching contests: API status not OK. Response: {data_contests}")
    except requests.exceptions.RequestException as e:
        print(f"Error fetching contest list from Codeforces API: {e}")
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON response for contests: {e}")

    return problems, contest_times


def filter_and_extract_problem_details(problems, contest_times, min_rating, max_rating,
                                       earliest_year, earliest_month, earliest_day,
                                       latest_year, latest_month, latest_day):
    """
    Filters problems by rating and contest date, and extracts relevant details.
    Also filters out problems with a '*special' tag.
    """
    filtered_problems = []
    min_date_filter = datetime(earliest_year, earliest_month, earliest_day)
    max_date_filter = datetime(latest_year, latest_month, latest_day, 23, 59, 59) # End of the day

    for problem in problems:
        problem_rating = problem.get('rating')
        contest_id = problem.get('contestId')
        index = problem.get('index')
        problem_tags = problem.get('tags', [])

        is_special_problem = False
        for tag in problem_tags:
            if tag.startswith('*special'):
                is_special_problem = True
                break

        if is_special_problem:
            continue

        if problem_rating is not None and min_rating <= problem_rating <= max_rating:
            current_problem_datetime = None

            if contest_id is not None and contest_id in contest_times:
                start_time_seconds = contest_times[contest_id]
                try:
                    current_problem_datetime = datetime.fromtimestamp(start_time_seconds)
                except (TypeError, ValueError) as e:
                    print(
                        f"Warning: Could not convert timestamp {start_time_seconds} for problem {contest_id}{index}: {e}")

            if current_problem_datetime and min_date_filter <= current_problem_datetime <= max_date_filter:
                problem_link = f"https://codeforces.com/problemset/problem/{contest_id}/{index}"
                problem_id = f"{contest_id}{index}"

                filtered_problems.append({
                    "id": problem_id,
                    "name": problem.get("name", "N/A"),
                    "link": problem_link,
                    "rating": problem_rating,
                    "tags": problem_tags,
                    "contestId": contest_id,
                    "index": index,
                    "contestDate": current_problem_datetime.strftime('%Y-%m-%d %H:%M:%S')
                })
    return filtered_problems


def scrape_problem_statement(problem_data):
    """
    Uses Selenium to scrape a single problem statement.
    """
    if not webdriver or not CHROMEDRIVER_PATH:
        print(f"Skipping scraping for {problem_data['id']}: Selenium or ChromeDriver not configured.")
        return False

    driver = None
    try:
        url = problem_data.get('link')
        prob_id = problem_data.get('id')
        rating = problem_data.get('rating')

        if not url or url == "N/A":
            print(f"Skipping problem {prob_id} due to missing or invalid link.")
            return False
        if not prob_id or prob_id == "N/A":
            print(f"Skipping problem with link '{url}' due to missing or invalid ID.")
            return False

        # Define the problem-specific directory for scraped statements
        # This is a temporary directory before final organization
        problem_output_directory = os.path.join(SCRAPED_PROBLEM_STATEMENTS_DIR, str(rating), str(prob_id))
        os.makedirs(problem_output_directory, exist_ok=True)

        output_filepath = os.path.join(problem_output_directory, f'{prob_id}.txt')

        if os.path.exists(output_filepath) and os.path.getsize(output_filepath) > 0:
            print(f"  Problem statement for {prob_id} already exists. Skipping scraping.")
            return True # Assume it's already scraped if file exists and is not empty

        print(f"  Attempting to scrape problem ID: {prob_id} from {url}")

        chrome_options = Options()
        # Non-headless mode as requested
        chrome_options.add_argument("--disable-blink-features=AutomationControlled")
        chrome_options.add_argument("--no-sandbox")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("--window-size=1920,1080")

        service = Service(CHROMEDRIVER_PATH)
        driver = webdriver.Chrome(service=service, options=chrome_options)
        driver.get(url)
        time.sleep(3) # Give page time to load

        problem_div = driver.find_element(By.CLASS_NAME, "problem-statement")

        driver.execute_script("""
            var range = document.createRange();
            var sel = window.getSelection();
            var node = arguments[0];
            range.selectNodeContents(node);
            sel.removeAllRanges();
            sel.addRange(range);
        """, problem_div)

        actions = ActionChains(driver)
        actions.key_down(Keys.CONTROL).send_keys('c').key_up(Keys.CONTROL).perform()
        time.sleep(0.3)

        text = pyperclip.paste()

        with open(output_filepath, 'w', encoding='utf-8') as out:
            out.write(text)
        print(f"  Saved scraped statement to {output_filepath}")
        time.sleep(1)
        return True
    except Exception as e:
        print(f"Failed to scrape problem ID {prob_id} from {url}: {e}")
        return False
    finally:
        if driver:
            driver.quit()


def organize_scraped_problems(scraped_problems_dir, target_organized_dir):
    """
    Moves scraped problem statements into an organized directory structure.
    """
    print(f"Organizing scraped problems from '{scraped_problems_dir}' to '{target_organized_dir}'...")
    all_organized_problems_info = []

    if not os.path.exists(scraped_problems_dir):
        print(f"Scraped problems directory '{scraped_problems_dir}' does not exist. Skipping organization.")
        return []

    for rating_dir_name in os.listdir(scraped_problems_dir):
        source_rating_path = os.path.join(scraped_problems_dir, rating_dir_name)
        if not os.path.isdir(source_rating_path):
            continue

        target_rating_path = os.path.join(target_organized_dir, rating_dir_name)
        os.makedirs(target_rating_path, exist_ok=True)

        for problem_id_dir_name in os.listdir(source_rating_path):
            source_problem_path = os.path.join(source_rating_path, problem_id_dir_name)
            if not os.path.isdir(source_problem_path):
                continue

            target_problem_path = os.path.join(target_rating_path, problem_id_dir_name)
            os.makedirs(target_problem_path, exist_ok=True) # Ensure target problem dir exists

            # Find the .txt file within the source_problem_path
            problem_txt_file = None
            for f_name in os.listdir(source_problem_path):
                if f_name.endswith('.txt') and f_name.startswith(problem_id_dir_name):
                    problem_txt_file = os.path.join(source_problem_path, f_name)
                    break

            if problem_txt_file:
                # Copy the .txt file to the new organized structure
                new_file_path = os.path.join(target_problem_path, os.path.basename(problem_txt_file))
                try:
                    shutil.copy2(problem_txt_file, new_file_path)
                    all_organized_problems_info.append({
                        'rating': int(rating_dir_name),
                        'problem_id': problem_id_dir_name,
                        'file_path': new_file_path
                    })
                except Exception as e:
                    print(f"Error copying problem file {problem_txt_file} to {new_file_path}: {e}")
            else:
                print(f"Warning: No problem statement .txt found in {source_problem_path}. Skipping.")
    print("Finished organizing problems.")
    return all_organized_problems_info

def get_problems_for_redo():
    """
    Scans existing AI output directories to determine which problems/prompts need re-processing.
    Asks the user for redo criteria (missing code/response).
    """
    print("\n--- Redo Mode Selected ---")
    print("Scanning existing AI output directories to identify problems for re-processing...")

    all_found_problems = {} # { (rating, problem_id): { 'file_path': ..., 'prompts_status': { prompt_name: { 'has_code': bool, 'has_response': bool } } } }

    if not os.path.exists(OUTPUT_AI_RESULTS_BASE_DIR):
        print(f"No existing AI results directory found at '{OUTPUT_AI_RESULTS_BASE_DIR}'. Cannot run in redo mode.")
        return []

    for rating_dir_name in os.listdir(OUTPUT_AI_RESULTS_BASE_DIR):
        rating_path = os.path.join(OUTPUT_AI_RESULTS_BASE_DIR, rating_dir_name)
        if not os.path.isdir(rating_path):
            continue

        for problem_id_dir_name in os.listdir(rating_path):
            problem_path = os.path.join(rating_path, problem_id_dir_name)
            if not os.path.isdir(problem_path):
                continue

            problem_key = (int(rating_dir_name), problem_id_dir_name)
            problem_data = {
                'rating': int(rating_dir_name),
                'problem_id': problem_id_dir_name,
                'file_path': None, # This will be filled by scanning organized problems later
                'prompts_status': {}
            }

            # Find the original problem statement in the ORGANIZED_PROBLEMS_BY_RATING_DIR
            original_problem_statement_path = os.path.join(ORGANIZED_PROBLEMS_BY_RATING_DIR, rating_dir_name, problem_id_dir_name, f"{problem_id_dir_name}.txt")
            if os.path.exists(original_problem_statement_path):
                problem_data['file_path'] = original_problem_statement_path
            else:
                print(f"Warning: Original problem statement not found for {problem_id_dir_name} at {original_problem_statement_path}. This problem cannot be re-processed.")
                continue # Skip problems for which the original statement is missing

            for prompt_name_dir in os.listdir(problem_path):
                prompt_output_path = os.path.join(problem_path, prompt_name_dir)
                if not os.path.isdir(prompt_output_path):
                    continue

                java_file_path = os.path.join(prompt_output_path, f"{prompt_name_dir}-{problem_id_dir_name}.java")
                response_txt_path = os.path.join(prompt_output_path, f"{prompt_name_dir}-response.txt")

                has_code = os.path.exists(java_file_path) and os.path.getsize(java_file_path) > 0
                has_response = os.path.exists(response_txt_path) and os.path.getsize(response_txt_path) > 0

                problem_data['prompts_status'][prompt_name_dir] = {
                    'has_code': has_code,
                    'has_response': has_response
                }
            all_found_problems[problem_key] = problem_data

    if not all_found_problems:
        print("No problems with existing AI outputs found. Redo mode cannot proceed.")
        return []

    problems_to_redo = []

    while True:
        redo_missing_code_str = input("Redo problems where Java code is missing/empty for any prompt? (yes/no): ").lower().strip()
        if redo_missing_code_str in ['yes', 'no']:
            redo_missing_code = (redo_missing_code_str == 'yes')
            break
        else:
            print("Invalid input. Please enter 'yes' or 'no'.")

    while True:
        redo_missing_response_str = input("Redo problems where AI response text is missing/empty for any prompt? (yes/no): ").lower().strip()
        if redo_missing_response_str in ['yes', 'no']:
            redo_missing_response = (redo_missing_response_str == 'yes')
            break
        else:
            print("Invalid input. Please enter 'yes' or 'no'.")

    if not redo_missing_code and not redo_missing_response:
        print("No redo criteria selected. Exiting redo mode.")
        return []

    for problem_key, problem_data in all_found_problems.items():
        prompts_for_this_problem_run = []
        for prompt_name, status in problem_data['prompts_status'].items():
            needs_redo = False
            if redo_missing_code and not status['has_code']:
                needs_redo = True
            if redo_missing_response and not status['has_response']:
                needs_redo = True

            # Ensure the prompt name actually exists in the global configured prompts
            if prompt_name in CONFIGURED_PROMPTS['prompts'] and needs_redo:
                prompts_for_this_problem_run.append(prompt_name)

        if prompts_for_this_problem_run:
            # We must provide the file_path to process_single_problem_with_ai
            if problem_data['file_path']:
                problem_data['prompts_to_run'] = prompts_for_this_problem_run
                problems_to_redo.append(problem_data)
            else:
                print(f"Skipping problem {problem_data['problem_id']} in redo mode as its original statement file is missing.")

    print(f"Found {len(problems_to_redo)} problems with specific prompts selected for re-processing.")
    return problems_to_redo

def check_for_unparseable_java():
    """
    Scans all AI output directories for response.txt files that might contain unparseable Java code.
    Specifically checks if a response.txt exists but no corresponding Java file was extracted,
    and the response.txt contains "public class".
    """
    print("\n--- Checking for potentially unparseable Java in AI responses ---")
    found_issues = []

    if not os.path.exists(OUTPUT_AI_RESULTS_BASE_DIR):
        print(f"No AI results directory found at '{OUTPUT_AI_RESULTS_BASE_DIR}'. Skipping check.")
        return

    for rating_dir_name in os.listdir(OUTPUT_AI_RESULTS_BASE_DIR):
        rating_path = os.path.join(OUTPUT_AI_RESULTS_BASE_DIR, rating_dir_name)
        if not os.path.isdir(rating_path):
            continue

        for problem_id_dir_name in os.listdir(rating_path):
            problem_path = os.path.join(rating_path, problem_id_dir_name)
            if not os.path.isdir(problem_path):
                continue

            for prompt_name_dir in os.listdir(problem_path):
                prompt_output_path = os.path.join(problem_path, prompt_name_dir)
                if not os.path.isdir(prompt_output_path):
                    continue

                java_file_path = os.path.join(prompt_output_path, f"{prompt_name_dir}-{problem_id_dir_name}.java")
                response_txt_path = os.path.join(prompt_output_path, f"{prompt_name_dir}-response.txt")

                java_file_exists_and_not_empty = os.path.exists(java_file_path) and os.path.getsize(java_file_path) > 0
                response_txt_exists_and_not_empty = os.path.exists(response_txt_path) and os.path.getsize(response_txt_path) > 0

                if response_txt_exists_and_not_empty and not java_file_exists_and_not_empty:
                    try:
                        with open(response_txt_path, 'r', encoding='utf-8') as f:
                            response_content = f.read()
                            if "public class" in response_content:
                                found_issues.append({
                                    'rating': rating_dir_name,
                                    'problem_id': problem_id_dir_name,
                                    'prompt_name': prompt_name_dir,
                                    'response_file': response_txt_path
                                })
                    except IOError as e:
                        print(f"Error reading response file '{response_txt_path}': {e}")
                    except Exception as e:
                        print(f"An unexpected error occurred while checking '{response_txt_path}': {e}")

    if found_issues:
        print("\n--- Potential unparseable Java code found in the following responses: ---")
        for issue in found_issues:
            print(f"\n  Problem ID: {issue['problem_id']} (Rating: {issue['rating']})")
            print(f"  Prompt Name: {issue['prompt_name']}")
            print(f"  Response File: {issue['response_file']}")
            print("  Action: Please check the 'response.txt' file for unparseable Java code (e.g., incomplete code blocks, syntax errors).")
    else:
        print("No potential unparseable Java code found in responses (where Java file was not extracted).")


def format_eta(seconds):
    """Formats seconds into HH:MM:SS."""
    m, s = divmod(int(seconds), 60)
    h, m = divmod(m, 60)
    return f"{h:02d}:{m:02d}:{s:02d}"

# --- Problem Scraper / Organizer Functions ---

# Constants for the Codeforces API endpoints
CODEFORCES_PROBLEMS_API_URL = "https://codeforces.com/api/problemset.problems"
CODEFORCES_CONTESTS_API_URL = "https://codeforces.com/api/contest.list"


def fetch_codeforces_data():
    """
    Fetches all problems and contest list from the Codeforces API.
    """
    if not requests:
        print("Requests library not available. Cannot fetch data from Codeforces API.")
        return None, None

    print("Fetching problems from Codeforces API...")
    problems = []
    try:
        response_problems = requests.get(CODEFORCES_PROBLEMS_API_URL)
        response_problems.raise_for_status()
        data_problems = response_problems.json()
        if data_problems and data_problems.get("status") == "OK":
            problems = data_problems.get("result", {}).get("problems", [])
            print("Successfully fetched problems.")
        else:
            print(f"Error fetching problems: API status not OK. Response: {data_problems}")
            return None, None
    except requests.exceptions.RequestException as e:
        print(f"Error fetching problems from Codeforces API: {e}")
        return None, None
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON response for problems: {e}")
        return None, None

    print("Fetching contest list from Codeforces API...")
    contest_times = {}
    try:
        response_contests = requests.get(CODEFORCES_CONTESTS_API_URL)
        response_contests.raise_for_status()
        data_contests = response_contests.json()
        if data_contests and data_contests.get("status") == "OK":
            for contest in data_contests.get("result", []):
                if 'id' in contest and 'startTimeSeconds' in contest:
                    contest_times[contest['id']] = contest['startTimeSeconds']
            print("Successfully fetched contest list.")
        else:
            print(f"Error fetching contests: API status not OK. Response: {data_contests}")
    except requests.exceptions.RequestException as e:
        print(f"Error fetching contest list from Codeforces API: {e}")
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON response for contests: {e}")

    return problems, contest_times


def filter_and_extract_problem_details(problems, contest_times, min_rating, max_rating,
                                       earliest_year, earliest_month, earliest_day,
                                       latest_year, latest_month, latest_day):
    """
    Filters problems by rating and contest date, and extracts relevant details.
    Also filters out problems with a '*special' tag.
    """
    filtered_problems = []
    min_date_filter = datetime(earliest_year, earliest_month, earliest_day)
    max_date_filter = datetime(latest_year, latest_month, latest_day, 23, 59, 59) # End of the day

    for problem in problems:
        problem_rating = problem.get('rating')
        contest_id = problem.get('contestId')
        index = problem.get('index')
        problem_tags = problem.get('tags', [])

        is_special_problem = False
        for tag in problem_tags:
            if tag.startswith('*special'):
                is_special_problem = True
                break

        if is_special_problem:
            continue

        if problem_rating is not None and min_rating <= problem_rating <= max_rating:
            current_problem_datetime = None

            if contest_id is not None and contest_id in contest_times:
                start_time_seconds = contest_times[contest_id]
                try:
                    current_problem_datetime = datetime.fromtimestamp(start_time_seconds)
                except (TypeError, ValueError) as e:
                    print(
                        f"Warning: Could not convert timestamp {start_time_seconds} for problem {contest_id}{index}: {e}")

            if current_problem_datetime and min_date_filter <= current_problem_datetime <= max_date_filter:
                problem_link = f"https://codeforces.com/problemset/problem/{contest_id}/{index}"
                problem_id = f"{contest_id}{index}"

                filtered_problems.append({
                    "id": problem_id,
                    "name": problem.get("name", "N/A"),
                    "link": problem_link,
                    "rating": problem_rating,
                    "tags": problem_tags,
                    "contestId": contest_id,
                    "index": index,
                    "contestDate": current_problem_datetime.strftime('%Y-%m-%d %H:%M:%S')
                })
    return filtered_problems


def main_script():
    global NUM_WORKERS, CHROMEDRIVER_PATH, CONFIGURED_PROMPTS

    print("--- Consolidated Codeforces Problem Processor ---")

    # Ask for redo mode at the beginning
    redo_mode_input = input("Do you want to run in redo mode (re-process existing AI outputs)? (yes/no): ").lower().strip()
    is_redo_mode = (redo_mode_input == 'yes')

    if is_redo_mode:
        # Load prompts first in redo mode as well, to know which prompts exist
        while True:
            prompts_file_name = input("\nEnter the name of the JSON file storing prompts (e.g., prompts.json): ")
            try:
                with open(prompts_file_name, 'r', encoding='utf-8') as f:
                    CONFIGURED_PROMPTS = json.load(f)
                if "prompts" not in CONFIGURED_PROMPTS or "text" not in CONFIGURED_PROMPTS:
                    print("Invalid prompts file format. It must contain 'prompts' (list of names) and 'text' (list of prompt strings/arrays).")
                    continue
                if len(CONFIGURED_PROMPTS['prompts']) != len(CONFIGURED_PROMPTS['text']):
                    print("Error: The 'prompts' array and 'text' array in your prompts file must have the same number of elements.")
                    continue
                print(f"Successfully loaded prompts from '{prompts_file_name}'.")
                break
            except FileNotFoundError:
                print(f"Error: Prompts file '{prompts_file_name}' not found.")
            except json.JSONDecodeError as e:
                print(f"Error decoding JSON from '{prompts_file_name}': {e}")
            except Exception as e:
                print(f"An unexpected error occurred loading prompts file: {e}")

        problems_to_process_with_ai = get_problems_for_redo()
        if not problems_to_process_with_ai:
            print("No problems selected for redo. Exiting.")
            sys.exit(0)
    else:
        # Normal mode: Proceed with fetching, filtering, scraping
        # Ask to delete all data
        delete_all_data_input = input("Do you want to delete all existing data in output directories (scraped, organized, AI results)? This cannot be undone. (yes/no): ").lower().strip()
        if delete_all_data_input == 'yes':
            print("Deleting existing data...")
            for directory in [SCRAPED_PROBLEM_STATEMENTS_DIR, ORGANIZED_PROBLEMS_BY_RATING_DIR, OUTPUT_AI_RESULTS_BASE_DIR]:
                if os.path.exists(directory):
                    try:
                        shutil.rmtree(directory)
                        print(f"  Deleted: {directory}")
                    except Exception as e:
                        print(f"  Error deleting {directory}: {e}")
            print("Existing data cleared.")
        else:
            print("Skipping data deletion.")

        # Recreate base directories (important after potential deletion)
        os.makedirs(SCRAPED_PROBLEM_STATEMENTS_DIR, exist_ok=True)
        os.makedirs(ORGANIZED_PROBLEMS_BY_RATING_DIR, exist_ok=True)
        os.makedirs(OUTPUT_AI_RESULTS_BASE_DIR, exist_ok=True)
        print("Ensured all base output directories exist.")

        # 1. Ask for min/max rating
        while True:
            try:
                min_rating_str = input("Enter the minimum difficulty rating (e.g., 800): ")
                min_rating = int(min_rating_str)
                if min_rating < 0:
                    print("Minimum rating cannot be negative. Please try again.")
                    continue

                max_rating_str = input("Enter the maximum difficulty rating (e.g., 1200): ")
                max_rating = int(max_rating_str)
                if max_rating < min_rating:
                    print("Maximum rating cannot be less than minimum rating. Please try again.")
                    continue
                break
            except ValueError:
                print("Invalid input. Please enter valid integers for ratings.")

        # 2. Ask for earliest and latest dates
        print("\nEnter the earliest contest date for problems (YYYY-MM-DD):")
        while True:
            try:
                earliest_year_str = input("  Year (e.g., 2020): ")
                earliest_month_str = input("  Month (1-12, e.g., 1): ")
                earliest_day_str = input("  Day (1-31, e.g., 15): ")

                earliest_year = int(earliest_year_str)
                earliest_month = int(earliest_month_str)
                earliest_day = int(earliest_day_str)

                datetime(earliest_year, earliest_month, earliest_day) # Validate date
                break
            except ValueError as e:
                print(f"Invalid date: {e}. Please ensure year, month, and day are valid integers and form a real date.")

        print("\nEnter the latest contest date for problems (YYYY-MM-DD). Press Enter three times for today's date:")
        while True:
            try:
                latest_year_str = input("  Year (e.g., 2024): ")
                latest_month_str = input("  Month (1-12, e.g., 7): ")
                latest_day_str = input("  Day (1-31, e.g., 25): ")

                if not latest_year_str and not latest_month_str and not latest_day_str:
                    today = datetime.now()
                    latest_year = today.year
                    latest_month = today.month
                    latest_day = today.day
                    print(f"  Using today's date: {latest_year}-{latest_month:02d}-{latest_day:02d}")
                    break

                latest_year = int(latest_year_str)
                latest_month = int(latest_month_str)
                latest_day = int(latest_day_str)

                datetime(latest_year, latest_month, latest_day) # Validate date
                break
            except ValueError as e:
                print(f"Invalid date: {e}. Please ensure year, month, and day are valid integers and form a real date, or hit enter three times for today's date.")


        # Fetch problems from Codeforces API
        all_codeforces_problems, contest_times_map = fetch_codeforces_data()
        if all_codeforces_problems is None:
            print("Failed to fetch problems from Codeforces API. Exiting.")
            sys.exit(1)

        # Filter problems based on user input
        filtered_problems_by_criteria = filter_and_extract_problem_details(
            all_codeforces_problems, contest_times_map, min_rating, max_rating,
            earliest_year, earliest_month, earliest_day,
            latest_year, latest_month, latest_day
        )

        if not filtered_problems_by_criteria:
            print("No problems found matching the specified criteria. Exiting.")
            sys.exit(0)

        # Group problems by rating and count
        problems_by_rating = {}
        for problem in filtered_problems_by_criteria:
            rating = problem['rating']
            if rating not in problems_by_rating:
                problems_by_rating[rating] = []
            problems_by_rating[rating].append(problem)

        print("\n--- Problem Counts by Rating (after filtering) ---")
        for rating in sorted(problems_by_rating.keys()):
            print(f"Rating {rating}: {len(problems_by_rating[rating])} problems")

        # Determine max problems per rating
        if not problems_by_rating:
            print("No problems to process after filtering by rating. Exiting.")
            sys.exit(0)

        min_count_across_ratings = min(len(problems) for problems in problems_by_rating.values())
        print(f"\nSmallest number of problems for any rating: {min_count_across_ratings}")

        # Ask user how many problems per rating
        while True:
            try:
                num_problems_per_rating_str = input(f"How many problems do you want for each rating (max {min_count_across_ratings})? (Enter a number): ")
                num_problems_per_rating = int(num_problems_per_rating_str)
                if not (1 <= num_problems_per_rating <= min_count_across_ratings):
                    print(f"Invalid number. Please enter a number between 1 and {min_count_across_ratings}.")
                    continue
                break
            except ValueError:
                print("Invalid input. Please enter a valid integer.")

        selected_problems_for_scraping = []
        for rating in sorted(problems_by_rating.keys()):
            # Randomly select `num_problems_per_rating` problems for each rating
            random.shuffle(problems_by_rating[rating])
            selected_problems_for_scraping.extend(problems_by_rating[rating][:num_problems_per_rating])

        print(f"\nSelected {len(selected_problems_for_scraping)} problems for scraping and processing.")

        # Ask for ChromeDriver path if Selenium is available
        if webdriver:
            while True:
                driver_path_input = input("Enter the full path to your ChromeDriver executable (e.g., C:/path/to/chromedriver.exe):\n  (You can download ChromeDriver from: https://chromedriver.storage.googleapis.com/index.html?path=114.0.5735.90/)\n")
                if os.path.exists(driver_path_input) and os.path.isfile(driver_path_input):
                    CHROMEDRIVER_PATH = driver_path_input
                    break
                else:
                    print("Invalid path or file not found. Please enter a valid path to ChromeDriver.")
        else:
            print("\nSelenium is not installed, skipping problem statement scraping.")

        # Scrape problem statements
        print("\n--- Scraping Problem Statements ---")
        scraped_problem_count = 0
        for i, problem in enumerate(selected_problems_for_scraping):
            print(f"Scraping {i+1}/{len(selected_problems_for_scraping)}: {problem['id']}")
            if scrape_problem_statement(problem):
                scraped_problem_count += 1
        print(f"Successfully scraped {scraped_problem_count} problem statements.")

        # Organize scraped problems into the new structure
        organized_problems_info = organize_scraped_problems(SCRAPED_PROBLEM_STATEMENTS_DIR, ORGANIZED_PROBLEMS_BY_RATING_DIR)

        # Filter organized_problems_info to match only the selected problems
        final_problems_to_process = []
        selected_problem_ids = {p['id'] for p in selected_problems_for_scraping}

        for problem_info in organized_problems_info:
            if problem_info['problem_id'] in selected_problem_ids:
                final_problems_to_process.append(problem_info)

        if not final_problems_to_process:
            print("No problem statements were successfully scraped and organized. Exiting.")
            sys.exit(0)

        # Load prompts in normal mode
        while True:
            prompts_file_name = input("\nEnter the name of the JSON file storing prompts (e.g., prompts.json): ")
            try:
                with open(prompts_file_name, 'r', encoding='utf-8') as f:
                    CONFIGURED_PROMPTS = json.load(f)
                if "prompts" not in CONFIGURED_PROMPTS or "text" not in CONFIGURED_PROMPTS:
                    print("Invalid prompts file format. It must contain 'prompts' (list of names) and 'text' (list of prompt strings/arrays).")
                    continue
                if len(CONFIGURED_PROMPTS['prompts']) != len(CONFIGURED_PROMPTS['text']):
                    print("Error: The 'prompts' array and 'text' array in your prompts file must have the same number of elements.")
                    continue
                print(f"Successfully loaded prompts from '{prompts_file_name}'.")
                break
            except FileNotFoundError:
                print(f"Error: Prompts file '{prompts_file_name}' not found.")
            except json.JSONDecodeError as e:
                print(f"Error decoding JSON from '{prompts_file_name}': {e}")
            except Exception as e:
                print(f"An unexpected error occurred loading prompts file: {e}")

        # Prepare problems for AI processing, checking status file
        problems_to_process_with_ai = []
        solution_status_map = {}
        if os.path.exists(STATUS_FILE_PATH):
            try:
                with open(STATUS_FILE_PATH, 'r', encoding='utf-8') as f:
                    loaded_status_data = json.load(f)
                for entry in loaded_status_data:
                    key = (entry.get('rating'), entry.get('problem_id'))
                    solution_status_map[key] = entry.get('prompts_status', {})
                print(f"Loaded existing solution status from '{STATUS_FILE_PATH}'.")
            except json.JSONDecodeError as e:
                print(f"Warning: Could not decode '{STATUS_FILE_PATH}'. Processing all selected problems/prompts. Error: {e}")
                solution_status_map = {}
            except Exception as e:
                print(f"Warning: An error occurred loading '{STATUS_FILE_PATH}'. Processing all selected problems/prompts. Error: {e}")
                solution_status_map = {}
        else:
            print(f"Status file '{STATUS_FILE_PATH}' not found. All selected problems/prompts will be processed.")


        for problem_info in final_problems_to_process:
            problem_key = (problem_info['rating'], problem_info['problem_id'])

            prompts_for_this_problem_run = []
            if problem_key in solution_status_map:
                status_for_this_problem = solution_status_map[problem_key]
                for prompt_name in CONFIGURED_PROMPTS['prompts']:
                    # A prompt is considered "done" if its output directory and java file exist and are not empty
                    output_dir_path = os.path.join(
                        OUTPUT_AI_RESULTS_BASE_DIR, str(problem_info['rating']), problem_info['problem_id'], prompt_name
                    )
                    java_file_path = os.path.join(output_dir_path, f"{prompt_name}-{problem_info['problem_id']}.java")
                    response_txt_path = os.path.join(output_dir_path, f"{prompt_name}-response.txt")
                    interaction_json_path = os.path.join(output_dir_path, f"{prompt_name}-interaction.json")

                    is_prompt_processed = (
                            os.path.exists(output_dir_path) and
                            os.path.exists(java_file_path) and os.path.getsize(java_file_path) > 0 and
                            os.path.exists(response_txt_path) and os.path.getsize(response_txt_path) > 0 and
                            os.path.exists(interaction_json_path) and os.path.getsize(interaction_json_path) > 0
                    )

                    if not is_prompt_processed:
                        prompts_for_this_problem_run.append(prompt_name)
                    else:
                        print(f"  Problem {problem_info['problem_id']} already has prompt '{prompt_name}' processed. Skipping.")
            else:
                prompts_for_this_problem_run = list(CONFIGURED_PROMPTS['prompts']) # All prompts if no status found

            if prompts_for_this_problem_run:
                problem_info['prompts_to_run'] = prompts_for_this_problem_run
                problems_to_process_with_ai.append(problem_info)

    # Common AI processing section for both modes
    # Ask for number of agents (workers)
    while True:
        try:
            num_workers_str = input("How many AI agents (concurrent workers) to use? (e.g., 5): ")
            NUM_WORKERS = int(num_workers_str)
            if NUM_WORKERS <= 0:
                print("Number of agents must be at least 1.")
                continue
            break
        except ValueError:
            print("Invalid input. Please enter a valid integer.")

    # Ask for API key file name
    while True:
        api_config_file = input("Enter the name of the file containing your API endpoint and key (e.g., api_config.txt): ")
        if load_api_config(api_config_file):
            if not AZURE_OPENAI_API_KEY or not AZURE_OPENAI_ENDPOINT:
                print("API key or endpoint is empty after loading. Please check the file content.")
                continue
            break
        else:
            print("Failed to load API configuration. Please try again.")

    if not AzureOpenAI or not AZURE_OPENAI_API_KEY or not AZURE_OPENAI_ENDPOINT:
        print("\nSkipping AI prompting due to missing OpenAI library or API configuration.")
        # Proceed to checks even if AI is skipped, as there might be old output files to check
        check_for_unparseable_java()
        return

    total_problems_to_attempt_ai = len(problems_to_process_with_ai)
    total_prompts_to_attempt_ai = sum(len(p.get('prompts_to_run', [])) for p in problems_to_process_with_ai)

    if total_prompts_to_attempt_ai == 0:
        print("\nAll selected problems have already been processed for the configured prompts. Exiting AI processing.")
        check_for_unparseable_java()
        return

    print(f"\nFound {total_problems_to_attempt_ai} problems with {total_prompts_to_attempt_ai} total prompts to process with AI.")
    print(f"Starting AI processing with {NUM_WORKERS} workers...")

    completed_problems_ai = 0
    successful_problems_ai = 0
    start_time_ai = time.time()

    # Initialize solution_status_map for normal mode if not already loaded
    if not is_redo_mode and not 'solution_status_map' in locals():
        solution_status_map = {}
        if os.path.exists(STATUS_FILE_PATH):
            try:
                with open(STATUS_FILE_PATH, 'r', encoding='utf-8') as f:
                    loaded_status_data = json.load(f)
                for entry in loaded_status_data:
                    key = (entry.get('rating'), entry.get('problem_id'))
                    solution_status_map[key] = entry.get('prompts_status', {})
            except Exception as e:
                print(f"Warning: Error loading solution status for normal mode: {e}")


    with ThreadPoolExecutor(max_workers=NUM_WORKERS) as executor:
        futures = {executor.submit(process_single_problem_with_ai, problem_info): problem_info['problem_id']
                   for problem_info in problems_to_process_with_ai}

        for future in as_completed(futures):
            problem_id, success = future.result()
            completed_problems_ai += 1
            if success:
                successful_problems_ai += 1

            elapsed_time_ai = time.time() - start_time_ai
            avg_time_per_problem_ai = elapsed_time_ai / completed_problems_ai if completed_problems_ai > 0 else 0
            remaining_problems_ai = total_problems_to_attempt_ai - completed_problems_ai
            estimated_remaining_time_ai = remaining_problems_ai * avg_time_per_problem_ai

            sys.stdout.write(
                f"\rAI Processing: Completed {completed_problems_ai}/{total_problems_to_attempt_ai} | Success: {successful_problems_ai} | "
                f"ETA: {format_eta(estimated_remaining_time_ai)} | Elapsed: {format_eta(elapsed_time_ai)}"
            )
            sys.stdout.flush()

            # Update status file for each completed problem
            # For redo mode, this update is mainly for future reference, as get_problems_for_redo re-scans.
            problem_info_for_status = next((p for p in problems_to_process_with_ai if p['problem_id'] == problem_id), None)
            if problem_info_for_status:
                problem_key_for_status = (problem_info_for_status['rating'], problem_info_for_status['problem_id'])
                current_prompts_status = solution_status_map.get(problem_key_for_status, {})
                for p_name in problem_info_for_status.get('prompts_to_run', []):
                    output_dir_path = os.path.join(
                        OUTPUT_AI_RESULTS_BASE_DIR, str(problem_info_for_status['rating']), problem_info_for_status['problem_id'], p_name
                    )
                    java_file_path = os.path.join(output_dir_path, f"{p_name}-{problem_info_for_status['problem_id']}.java")
                    response_txt_path = os.path.join(output_dir_path, f"{p_name}-response.txt")
                    interaction_json_path = os.path.join(output_dir_path, f"{p_name}-interaction.json")

                    is_prompt_fully_saved = (
                            os.path.exists(output_dir_path) and
                            os.path.exists(java_file_path) and os.path.getsize(java_file_path) > 0 and
                            os.path.exists(response_txt_path) and os.path.getsize(response_txt_path) > 0 and
                            os.path.exists(interaction_json_path) and os.path.getsize(interaction_json_path) > 0
                    )

                    if is_prompt_fully_saved:
                        current_prompts_status[p_name] = {
                            "directory_exists": True,
                            "java_code_exists": True,
                            "response_txt_exists": True,
                            "interaction_json_exists": True,
                            "processed_at": datetime.now().isoformat()
                        }
                    else:
                        current_prompts_status[p_name] = {
                            "directory_exists": os.path.exists(output_dir_path),
                            "java_code_exists": os.path.exists(java_file_path) and os.path.getsize(java_file_path) > 0,
                            "response_txt_exists": os.path.exists(response_txt_path) and os.path.getsize(response_txt_path) > 0,
                            "interaction_json_exists": os.path.exists(interaction_json_path) and os.path.getsize(interaction_json_path) > 0,
                            "processed_at": datetime.now().isoformat() if not is_prompt_fully_saved else None
                        }
                solution_status_map[problem_key_for_status] = current_prompts_status

                # Save status map after each problem is completed
                temp_status_list = []
                for (rating_val, problem_id_val), prompt_status in solution_status_map.items():
                    temp_status_list.append({
                        'rating': rating_val,
                        'problem_id': problem_id_val,
                        'prompts_status': prompt_status
                    })
                try:
                    with open(STATUS_FILE_PATH, 'w', encoding='utf-8') as f:
                        json.dump(temp_status_list, f, ensure_ascii=False, indent=4)
                except IOError as e:
                    print(f"\nError saving status to '{STATUS_FILE_PATH}': {e}")

    print("\n--- AI Processing Complete! ---")
    print(f"Total problems sent to AI: {total_problems_to_attempt_ai}")
    print(f"Successfully processed by AI (all requested prompts completed): {successful_problems_ai}")
    print(f"Problems with errors/skipped prompts in AI processing: {total_problems_to_attempt_ai - successful_problems_ai}")

    # Final check for unparseable Java in all modes
    check_for_unparseable_java()


if __name__ == "__main__":
    main_script()

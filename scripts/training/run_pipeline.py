#!/usr/bin/env python3
"""
Vani-Kanoon Legal LLM Training Pipeline
========================================

Complete pipeline to:
1. Scrape legal data from India Code and Indian Kanoon
2. Generate training data in instruction format
3. Prepare dataset for Kaggle upload

Usage:
    python run_pipeline.py --full          # Run everything
    python run_pipeline.py --scrape-only   # Only scrape data
    python run_pipeline.py --generate-only # Only generate training data
    python run_pipeline.py --quick         # Quick mode (priority acts + landmarks only)
"""

import argparse
import subprocess
import sys
import os
import json
from pathlib import Path
from datetime import datetime

# Colors for terminal output
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    END = '\033[0m'
    BOLD = '\033[1m'

def print_header(msg):
    print(f"\n{Colors.HEADER}{Colors.BOLD}{'='*60}{Colors.END}")
    print(f"{Colors.HEADER}{Colors.BOLD} {msg}{Colors.END}")
    print(f"{Colors.HEADER}{Colors.BOLD}{'='*60}{Colors.END}\n")

def print_step(step, msg):
    print(f"{Colors.BLUE}[Step {step}]{Colors.END} {msg}")

def print_success(msg):
    print(f"{Colors.GREEN}✅ {msg}{Colors.END}")

def print_warning(msg):
    print(f"{Colors.YELLOW}⚠️  {msg}{Colors.END}")

def print_error(msg):
    print(f"{Colors.RED}❌ {msg}{Colors.END}")

def get_project_root():
    """Get the project root directory"""
    return Path(__file__).parent.parent.parent

def check_dependencies():
    """Check if required dependencies are installed"""
    print_step(0, "Checking dependencies...")

    required = ['requests', 'beautifulsoup4', 'lxml']
    missing = []

    for pkg in required:
        try:
            __import__(pkg.replace('-', '_').replace('4', ''))
        except ImportError:
            missing.append(pkg)

    if missing:
        print_warning(f"Missing packages: {', '.join(missing)}")
        print("Installing...")
        subprocess.run([sys.executable, '-m', 'pip', 'install'] + missing, check=True)
        print_success("Dependencies installed")
    else:
        print_success("All dependencies available")

def run_scrape_india_code(quick_mode=False):
    """Run the India Code scraper"""
    print_step(1, "Scraping India Code (bare acts)...")

    script_path = get_project_root() / 'scripts' / 'data_collection' / 'scrape_india_code.py'

    if not script_path.exists():
        print_error(f"Script not found: {script_path}")
        return False

    cmd = [sys.executable, str(script_path)]
    if quick_mode:
        cmd.append('--priority-only')

    try:
        result = subprocess.run(cmd, check=True, capture_output=False)
        print_success("India Code scraping complete")
        return True
    except subprocess.CalledProcessError as e:
        print_error(f"India Code scraping failed: {e}")
        return False

def run_scrape_indian_kanoon(quick_mode=False):
    """Run the Indian Kanoon scraper"""
    print_step(2, "Scraping Indian Kanoon (case law)...")

    script_path = get_project_root() / 'scripts' / 'data_collection' / 'scrape_indian_kanoon.py'

    if not script_path.exists():
        print_error(f"Script not found: {script_path}")
        return False

    cmd = [sys.executable, str(script_path)]
    if quick_mode:
        cmd.append('--landmarks-only')

    try:
        result = subprocess.run(cmd, check=True, capture_output=False)
        print_success("Indian Kanoon scraping complete")
        return True
    except subprocess.CalledProcessError as e:
        print_error(f"Indian Kanoon scraping failed: {e}")
        return False

def run_generate_training_data():
    """Generate training data from scraped content"""
    print_step(3, "Generating training data...")

    script_path = get_project_root() / 'scripts' / 'data_collection' / 'create_training_data.py'

    if not script_path.exists():
        print_error(f"Script not found: {script_path}")
        return False

    try:
        result = subprocess.run([sys.executable, str(script_path)], check=True, capture_output=False)
        print_success("Training data generation complete")
        return True
    except subprocess.CalledProcessError as e:
        print_error(f"Training data generation failed: {e}")
        return False

def validate_output():
    """Validate the generated training data"""
    print_step(4, "Validating output...")

    data_dir = get_project_root() / 'data'
    training_file = data_dir / 'training_data.json'

    if not training_file.exists():
        print_error("training_data.json not found!")
        return False

    try:
        with open(training_file, 'r', encoding='utf-8') as f:
            data = json.load(f)

        count = len(data)
        size_mb = training_file.stat().st_size / (1024 * 1024)

        # Check data quality
        valid = 0
        for item in data[:100]:  # Sample first 100
            if 'instruction' in item and 'output' in item:
                if len(item['instruction']) > 10 and len(item['output']) > 20:
                    valid += 1

        quality = valid / min(100, count) * 100

        print(f"\n{Colors.BOLD}📊 Training Data Statistics:{Colors.END}")
        print(f"   Total examples: {count:,}")
        print(f"   File size: {size_mb:.2f} MB")
        print(f"   Quality score: {quality:.0f}%")

        if count < 1000:
            print_warning("Dataset is small. Consider scraping more data.")
        elif count < 10000:
            print_warning("Dataset is moderate. More data = better model.")
        else:
            print_success(f"Dataset looks good with {count:,} examples!")

        return True

    except Exception as e:
        print_error(f"Validation failed: {e}")
        return False

def create_kaggle_dataset():
    """Package data for Kaggle upload"""
    print_step(5, "Packaging for Kaggle...")

    data_dir = get_project_root() / 'data'
    training_file = data_dir / 'training_data.json'
    kaggle_dir = data_dir / 'kaggle_upload'

    kaggle_dir.mkdir(exist_ok=True)

    # Copy training data
    import shutil
    shutil.copy(training_file, kaggle_dir / 'training_data.json')

    # Create dataset metadata
    metadata = {
        "title": "Vani Legal Training Data",
        "id": "YOUR_USERNAME/vani-legal-training-data",
        "licenses": [{"name": "CC0-1.0"}],
        "keywords": ["indian law", "legal", "nlp", "llm training"]
    }

    with open(kaggle_dir / 'dataset-metadata.json', 'w') as f:
        json.dump(metadata, f, indent=2)

    print_success(f"Kaggle dataset ready at: {kaggle_dir}")
    print(f"\n{Colors.BOLD}📤 To upload to Kaggle:{Colors.END}")
    print(f"   1. Edit {kaggle_dir / 'dataset-metadata.json'} with your username")
    print(f"   2. Run: kaggle datasets create -p {kaggle_dir}")

    return True

def print_next_steps():
    """Print instructions for next steps"""
    print_header("PIPELINE COMPLETE - NEXT STEPS")

    print(f"""{Colors.BOLD}1. Upload to Kaggle:{Colors.END}
   - Go to kaggle.com and create a new dataset
   - Upload data/training_data.json
   - Note your dataset path (e.g., username/vani-legal-training-data)

{Colors.BOLD}2. Create Kaggle Notebook:{Colors.END}
   - Create new notebook at kaggle.com
   - Upload ml_training/notebooks/kaggle_finetune_vani_legal.ipynb
   - Enable GPU: Settings → Accelerator → GPU T4 x2
   - Update TRAINING_DATA_PATH in the notebook
   - Run all cells (~4 hours)

{Colors.BOLD}3. Download Trained Model:{Colors.END}
   - Download vani-legal-q4_k_m.gguf (~2.3 GB)
   - Place in frontend/android/app/src/main/assets/models/

{Colors.BOLD}4. Build Mobile App:{Colors.END}
   cd frontend
   npm install @nicepkg/capacitor-llama
   npx cap sync android
   npm run mobile:build

{Colors.BOLD}📚 Documentation:{Colors.END}
   See ml_training/README.md for detailed instructions
""")

def main():
    parser = argparse.ArgumentParser(
        description='Vani-Kanoon Legal LLM Training Pipeline',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_pipeline.py --full          # Complete pipeline
  python run_pipeline.py --quick         # Quick mode (priority data only)
  python run_pipeline.py --scrape-only   # Only collect data
  python run_pipeline.py --generate-only # Only generate training data
        """
    )

    parser.add_argument('--full', action='store_true',
                        help='Run the complete pipeline')
    parser.add_argument('--quick', action='store_true',
                        help='Quick mode - only priority acts and landmark cases')
    parser.add_argument('--scrape-only', action='store_true',
                        help='Only run data scraping')
    parser.add_argument('--generate-only', action='store_true',
                        help='Only generate training data (assumes data exists)')
    parser.add_argument('--validate', action='store_true',
                        help='Only validate existing training data')
    parser.add_argument('--skip-india-code', action='store_true',
                        help='Skip India Code scraping')
    parser.add_argument('--skip-indian-kanoon', action='store_true',
                        help='Skip Indian Kanoon scraping')

    args = parser.parse_args()

    # Default to --full if no args
    if not any([args.full, args.quick, args.scrape_only, args.generate_only, args.validate]):
        args.full = True

    print_header("VANI-KANOON LEGAL LLM TRAINING PIPELINE")
    print(f"Started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Mode: {'Quick' if args.quick else 'Full'}")

    # Check dependencies
    check_dependencies()

    success = True

    if args.validate:
        # Just validate
        validate_output()
        return

    if args.full or args.quick or args.scrape_only:
        # Run scrapers
        if not args.skip_india_code:
            success = run_scrape_india_code(args.quick) and success

        if not args.skip_indian_kanoon:
            success = run_scrape_indian_kanoon(args.quick) and success

    if args.full or args.quick or args.generate_only:
        # Generate training data
        success = run_generate_training_data() and success

        # Validate
        if success:
            validate_output()
            create_kaggle_dataset()

    print(f"\nFinished: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    if success:
        print_next_steps()
    else:
        print_error("Pipeline completed with errors. Check logs above.")
        sys.exit(1)

if __name__ == '__main__':
    main()

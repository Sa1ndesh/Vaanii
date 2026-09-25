# ML Training Pipeline for Vani-Kanoon Legal LLM

## Overview

This folder contains everything needed to fine-tune a custom legal LLM for the Vani-Kanoon application.

## Directory Structure

```
ml_training/
├── notebooks/
│   └── kaggle_finetune_vani_legal.ipynb  # Kaggle notebook for training
├── configs/
│   └── training_config.yaml              # Training configuration
└── README.md                             # This file

scripts/
├── data_collection/
│   ├── scrape_india_code.py              # Scrape bare acts from India Code
│   ├── scrape_indian_kanoon.py           # Scrape case law
│   └── create_training_data.py           # Convert to training format
└── training/
    └── run_pipeline.py                   # Run complete pipeline

data/                                      # Created by scripts
├── bare_acts/
│   └── central/                          # JSON files of Indian acts
├── case_law/
│   ├── landmark/                         # Landmark cases
│   ├── criminal/                         # Criminal cases
│   └── ...                               # Other categories
└── training_data.json                    # Final training data
```

## Quick Start

### Step 1: Collect Data

```bash
# Install dependencies
pip install requests beautifulsoup4 lxml

# Scrape Indian laws (takes 2-4 hours)
cd scripts/data_collection
python scrape_india_code.py --priority-only  # Start with important acts

# Scrape case law (takes 4-8 hours)
python scrape_indian_kanoon.py --landmarks-only  # Start with landmarks

# Generate training data
python create_training_data.py
```

### Step 2: Train on Kaggle (FREE GPU)

1. Go to [kaggle.com](https://kaggle.com) and create account
2. Upload `data/training_data.json` as a dataset
3. Create new notebook and upload `notebooks/kaggle_finetune_vani_legal.ipynb`
4. Enable GPU: Settings → Accelerator → GPU T4 x2
5. Run all cells (takes ~4 hours)
6. Download `vani-legal-q4_k_m.gguf` from output

### Step 3: Deploy to Mobile

1. Copy `vani-legal-q4_k_m.gguf` to:
   ```
   frontend/android/app/src/main/assets/models/
   ```

2. Install Capacitor Llama plugin:
   ```bash
   cd frontend
   npm install @nicepkg/capacitor-llama
   npx cap sync android
   ```

3. Build Android app:
   ```bash
   npm run mobile:build
   npm run mobile:open
   ```

## Data Sources

| Source | URL | Content |
|--------|-----|---------|
| India Code | indiacode.nic.in | All Central Acts |
| Indian Kanoon | indiankanoon.org | Case Law |
| Legislative Dept | legislative.gov.in | New Criminal Laws |

## Training Data Format

```json
{
  "instruction": "What is Section 302 of IPC?",
  "input": "",
  "output": "Section 302 of IPC deals with punishment for murder..."
}
```

## Model Options

| Model | Size | Mobile RAM | Quality |
|-------|------|------------|---------|
| Phi-3.5-mini | 3.8B | 4GB | ⭐⭐⭐ |
| Llama-3.2-3B | 3B | 3GB | ⭐⭐⭐ |
| Mistral-7B | 7B | 8GB | ⭐⭐⭐⭐ |

**Recommended:** Phi-3.5-mini for mobile (best size/quality balance)

## Quantization Options

| Format | Size | Speed | Quality |
|--------|------|-------|---------|
| Q4_K_M | ~2.3GB | Fast | Good |
| Q5_K_M | ~2.8GB | Medium | Better |
| Q8_0 | ~4GB | Slow | Best |

**Recommended:** Q4_K_M for mobile

## Estimated Costs

| Resource | Cost |
|----------|------|
| Kaggle GPU | FREE |
| Google Colab Pro | $10/month |
| RunPod A100 | $1-2/hour |

## Training Time

| Platform | Time |
|----------|------|
| Kaggle T4 | 4-6 hours |
| Colab T4 | 4-6 hours |
| RunPod A100 | 1-2 hours |

## Troubleshooting

### Out of Memory on Kaggle
- Reduce `BATCH_SIZE` to 1
- Reduce `MAX_SEQ_LENGTH` to 1024
- Use smaller model (Llama-3.2-3B)

### Model Not Loading on Phone
- Check file path is correct
- Ensure model file is not compressed (`.gguf` not `.gguf.zip`)
- Check phone has enough RAM (4GB minimum)

### Poor Quality Responses
- Increase training data (aim for 50K+ examples)
- Train for more epochs (3-5)
- Use larger model if phone supports it

## License

Training data sources:
- India Code: Government of India (Public Domain)
- Indian Kanoon: Fair use for research/education
- Manual examples: Original content

Model weights:
- Base model: Check original license (Phi-3: MIT, Llama: Meta License)
- Fine-tuned weights: Same as base model

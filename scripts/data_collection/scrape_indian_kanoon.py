"""
Indian Kanoon Scraper - Scrapes landmark case law from indiankanoon.org
Run: python scrape_indian_kanoon.py

This will collect:
- Supreme Court judgments
- High Court judgments
- Landmark cases across legal categories
"""

import requests
from bs4 import BeautifulSoup
import json
import time
import re
from pathlib import Path
from urllib.parse import urljoin, quote
import logging
from datetime import datetime

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('scrape_indian_kanoon.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


class IndianKanoonScraper:
    """Scraper for Indian Kanoon - Free Indian case law database"""

    BASE_URL = "https://indiankanoon.org"

    # Legal topics and their search queries
    LEGAL_TOPICS = {
        'constitutional': [
            'fundamental rights violation',
            'article 21 right to life',
            'article 14 equality',
            'article 19 freedom speech',
            'writ petition',
            'judicial review',
            'basic structure doctrine',
        ],
        'criminal': [
            'murder section 302',
            'culpable homicide',
            'rape section 376',
            'criminal conspiracy',
            'cheating section 420',
            'criminal breach of trust',
            'dowry death section 304B',
            'POCSO',
            'acid attack',
            'kidnapping abduction',
        ],
        'civil': [
            'specific performance contract',
            'breach of contract damages',
            'injunction',
            'tort negligence',
            'defamation',
            'consumer complaint',
        ],
        'property': [
            'partition suit',
            'adverse possession',
            'easement rights',
            'landlord tenant eviction',
            'property dispute',
            'title suit',
            'will probate',
        ],
        'family': [
            'divorce cruelty',
            'maintenance wife',
            'child custody',
            'domestic violence',
            'Hindu marriage divorce',
            'Muslim divorce',
            'restitution conjugal rights',
            'dowry harassment',
        ],
        'labour': [
            'wrongful termination',
            'industrial dispute',
            'gratuity',
            'provident fund',
            'sexual harassment workplace',
            'minimum wages',
        ],
        'corporate': [
            'company winding up',
            'oppression mismanagement',
            'director liability',
            'shareholder dispute',
            'insolvency bankruptcy',
        ],
        'cyber': [
            'information technology act',
            'cyber crime',
            'data privacy',
            'intermediary liability',
        ],
        'environment': [
            'environmental pollution',
            'forest conservation',
            'wildlife protection',
        ],
        'bail': [
            'anticipatory bail',
            'regular bail',
            'bail non-bailable offence',
            'cancellation of bail',
        ],
        'procedure': [
            'limitation period',
            'res judicata',
            'stay order',
            'appeal revision',
            'review petition',
        ],
    }

    # Landmark cases to specifically fetch
    LANDMARK_CASES = [
        # Constitutional
        "Kesavananda Bharati v. State of Kerala",
        "Maneka Gandhi v. Union of India",
        "Minerva Mills v. Union of India",
        "S.R. Bommai v. Union of India",
        "Vishaka v. State of Rajasthan",
        "Navtej Singh Johar v. Union of India",
        "K.S. Puttaswamy v. Union of India",
        "Shayara Bano v. Union of India",
        # Criminal
        "Bachan Singh v. State of Punjab",
        "Machhi Singh v. State of Punjab",
        "State of Maharashtra v. Mohd. Yakub",
        "Arnesh Kumar v. State of Bihar",
        "Lalita Kumari v. Government of UP",
        "Priyanka Sharma v. State of Maharashtra",
        # Civil/Contract
        "Satyabrata Ghose v. Mugneeram Bangur",
        "Balfour v. Balfour Indian context",
        # Property
        "S.P. Chengalvaraya Naidu v. Jagannath",
        "Suraj Lamp Industries v. State of Haryana",
        # Family
        "Shilpa Sailesh v. Varun Sreenivasan",
        "V. Tulasamma v. Sesha Reddy",
        "Shamim Ara v. State of UP",
        # Bail
        "Sanjay Chandra v. CBI",
        "P. Chidambaram v. Directorate of Enforcement",
        "Arnab Goswami v. State of Maharashtra",
    ]

    def __init__(self, output_dir="../../data/case_law"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
        })
        self.collected_cases = []

    def search_cases(self, query, court=None, max_results=20):
        """Search for cases on Indian Kanoon"""
        cases = []
        encoded_query = quote(query)

        try:
            url = f"{self.BASE_URL}/search/?formInput={encoded_query}"
            if court:
                url += f"+doctypes:{court}"

            response = self.session.get(url, timeout=30)
            if response.status_code != 200:
                logger.warning(f"Search failed for '{query}': {response.status_code}")
                return cases

            soup = BeautifulSoup(response.text, 'html.parser')

            # Find result links
            for result in soup.select('.result, .result_title'):
                link = result.select_one('a[href*="/doc/"]')
                if not link:
                    continue

                case_url = urljoin(self.BASE_URL, link['href'])
                case_title = link.text.strip()

                if case_url not in [c['url'] for c in cases]:
                    cases.append({
                        'title': case_title,
                        'url': case_url,
                        'search_query': query
                    })

                if len(cases) >= max_results:
                    break

        except Exception as e:
            logger.error(f"Search error for '{query}': {e}")

        return cases

    def scrape_case(self, case_url):
        """Scrape full case content"""
        try:
            response = self.session.get(case_url, timeout=60)
            if response.status_code != 200:
                return None

            soup = BeautifulSoup(response.text, 'html.parser')

            case_data = {
                'url': case_url,
                'title': '',
                'citation': '',
                'court': '',
                'date': '',
                'bench': [],
                'judges': [],
                'petitioner': '',
                'respondent': '',
                'headnotes': '',
                'facts': '',
                'issues': [],
                'arguments_petitioner': '',
                'arguments_respondent': '',
                'held': '',
                'ratio_decidendi': '',
                'obiter_dicta': '',
                'full_text': '',
                'statutes_cited': [],
                'cases_cited': [],
                'keywords': [],
            }

            # Title
            title_elem = soup.select_one('h2.doc_title, .doc_title, h1')
            if title_elem:
                case_data['title'] = title_elem.text.strip()

            # Parse title for parties
            if ' v. ' in case_data['title'] or ' vs ' in case_data['title'].lower():
                parties = re.split(r'\s+v\.?\s+|\s+vs\.?\s+', case_data['title'], flags=re.IGNORECASE)
                if len(parties) >= 2:
                    case_data['petitioner'] = parties[0].strip()
                    case_data['respondent'] = parties[1].strip()

            # Court and date from metadata
            meta_elem = soup.select_one('.docsource_main, .doc_author')
            if meta_elem:
                meta_text = meta_elem.text.strip()
                case_data['court'] = meta_text

                # Extract date
                date_match = re.search(r'(\d{1,2}[/-]\d{1,2}[/-]\d{4}|\d{4})', meta_text)
                if date_match:
                    case_data['date'] = date_match.group(1)

            # Citation
            cite_elem = soup.select_one('.doc_citations, .citation')
            if cite_elem:
                case_data['citation'] = cite_elem.text.strip()

            # Judges/Bench
            bench_elem = soup.select_one('.doc_bench, .docsource_main')
            if bench_elem:
                bench_text = bench_elem.text
                # Extract judge names (usually after "Bench:" or "Coram:")
                judge_match = re.search(r'(?:Bench|Coram|Hon\'ble)[:\s]+(.+?)(?:\n|$)', bench_text, re.IGNORECASE)
                if judge_match:
                    judges = judge_match.group(1).split(',')
                    case_data['judges'] = [j.strip() for j in judges if j.strip()]

            # Full text (main judgment content)
            judgment_elem = soup.select_one('.judgments, #judgments, .doc_content')
            if judgment_elem:
                case_data['full_text'] = judgment_elem.get_text(separator='\n').strip()

                # Try to extract structured parts from full text
                case_data.update(self.extract_judgment_parts(case_data['full_text']))

            # Cited statutes
            for statute_link in soup.select('a[href*="/act/"]'):
                statute = statute_link.text.strip()
                if statute and statute not in case_data['statutes_cited']:
                    case_data['statutes_cited'].append(statute)

            # Cited cases
            for case_link in soup.select('a[href*="/doc/"]'):
                cited = case_link.text.strip()
                if cited and cited != case_data['title'] and cited not in case_data['cases_cited']:
                    case_data['cases_cited'].append(cited)
                if len(case_data['cases_cited']) > 20:  # Limit
                    break

            return case_data

        except Exception as e:
            logger.error(f"Error scraping {case_url}: {e}")
            return None

    def extract_judgment_parts(self, full_text):
        """Extract structured parts from judgment text"""
        parts = {
            'facts': '',
            'issues': [],
            'held': '',
            'ratio_decidendi': ''
        }

        # Facts section
        facts_match = re.search(
            r'(?:FACTS|Facts of the Case|Brief Facts)[:\s]*(.+?)(?=ISSUES|Issues|ARGUMENTS|Arguments|HELD|$)',
            full_text, re.IGNORECASE | re.DOTALL
        )
        if facts_match:
            parts['facts'] = facts_match.group(1).strip()[:3000]

        # Issues
        issues_match = re.search(
            r'(?:ISSUES?|Points? for Determination)[:\s]*(.+?)(?=ARGUMENTS|Arguments|HELD|ANALYSIS|$)',
            full_text, re.IGNORECASE | re.DOTALL
        )
        if issues_match:
            issues_text = issues_match.group(1)
            # Split by numbered points
            issue_items = re.findall(r'(?:\d+[.)]\s*|[ivx]+[.)]\s*)(.+?)(?=\d+[.)]|\n\n|$)', issues_text, re.IGNORECASE)
            parts['issues'] = [i.strip() for i in issue_items if i.strip()][:10]

        # Held/Decision
        held_match = re.search(
            r'(?:HELD|ORDER|DECISION|JUDGMENT)[:\s]*(.+?)(?=\n\n\n|$)',
            full_text, re.IGNORECASE | re.DOTALL
        )
        if held_match:
            parts['held'] = held_match.group(1).strip()[:2000]

        # Ratio decidendi (key legal principle)
        ratio_patterns = [
            r'(?:ratio|principle)[:\s]*(.+?)(?=\n\n|$)',
            r'(?:We hold that|It is held that|The law is)[:\s]*(.+?)(?=\n\n|$)',
        ]
        for pattern in ratio_patterns:
            ratio_match = re.search(pattern, full_text, re.IGNORECASE | re.DOTALL)
            if ratio_match:
                parts['ratio_decidendi'] = ratio_match.group(1).strip()[:1500]
                break

        return parts

    def save_case(self, case_data, category=None):
        """Save case as JSON"""
        # Create filename from title
        filename = re.sub(r'[^\w\s-]', '', case_data['title'].lower())
        filename = re.sub(r'[-\s]+', '_', filename)[:80]

        # Organize by category
        if category:
            category_dir = self.output_dir / category
            category_dir.mkdir(exist_ok=True)
            filepath = category_dir / f"{filename}.json"
        else:
            filepath = self.output_dir / f"{filename}.json"

        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(case_data, f, ensure_ascii=False, indent=2)

        logger.info(f"Saved: {filepath}")
        return filepath

    def scrape_by_topics(self, max_per_topic=30):
        """Scrape cases organized by legal topic"""
        for category, queries in self.LEGAL_TOPICS.items():
            logger.info(f"\n{'='*50}")
            logger.info(f"Category: {category.upper()}")
            logger.info(f"{'='*50}")

            category_cases = []

            for query in queries:
                logger.info(f"Searching: {query}")

                # Search Supreme Court
                sc_cases = self.search_cases(query, court='supremecourt', max_results=max_per_topic // 2)
                # Search High Courts
                hc_cases = self.search_cases(query, court='allahabad,bombay,delhi,karnataka,madras',
                                            max_results=max_per_topic // 2)

                all_cases = sc_cases + hc_cases

                for case_info in all_cases:
                    if case_info['url'] in [c.get('url') for c in category_cases]:
                        continue

                    logger.info(f"  Scraping: {case_info['title'][:50]}...")

                    case_data = self.scrape_case(case_info['url'])
                    if case_data:
                        case_data['category'] = category
                        case_data['search_query'] = query
                        self.save_case(case_data, category=category)
                        category_cases.append(case_data)
                        self.collected_cases.append(case_data)

                    time.sleep(2)  # Rate limiting

                time.sleep(1)

            logger.info(f"Collected {len(category_cases)} cases for {category}")

    def scrape_landmark_cases(self):
        """Scrape specific landmark cases"""
        logger.info("\n" + "="*50)
        logger.info("Scraping LANDMARK CASES")
        logger.info("="*50)

        for case_name in self.LANDMARK_CASES:
            logger.info(f"Searching: {case_name}")

            cases = self.search_cases(case_name, max_results=3)

            if cases:
                case_info = cases[0]  # Take first result
                logger.info(f"  Found: {case_info['title'][:50]}...")

                case_data = self.scrape_case(case_info['url'])
                if case_data:
                    case_data['is_landmark'] = True
                    self.save_case(case_data, category='landmark')
                    self.collected_cases.append(case_data)

            time.sleep(2)

    def create_index(self):
        """Create index of all collected cases"""
        index = {
            'total_cases': len(self.collected_cases),
            'scraped_at': datetime.now().isoformat(),
            'categories': {},
            'cases': []
        }

        for case in self.collected_cases:
            category = case.get('category', 'uncategorized')
            if category not in index['categories']:
                index['categories'][category] = 0
            index['categories'][category] += 1

            index['cases'].append({
                'title': case.get('title', ''),
                'court': case.get('court', ''),
                'category': category,
                'is_landmark': case.get('is_landmark', False),
            })

        index_path = self.output_dir / '_index.json'
        with open(index_path, 'w', encoding='utf-8') as f:
            json.dump(index, f, ensure_ascii=False, indent=2)

        logger.info(f"Index saved: {index_path}")

    def run(self, include_topics=True, include_landmarks=True, max_per_topic=30):
        """Run the scraper"""
        logger.info("Starting Indian Kanoon scraper...")
        logger.info(f"Output directory: {self.output_dir}")

        if include_landmarks:
            self.scrape_landmark_cases()

        if include_topics:
            self.scrape_by_topics(max_per_topic=max_per_topic)

        self.create_index()

        logger.info(f"\n{'='*50}")
        logger.info(f"COMPLETED! Total cases collected: {len(self.collected_cases)}")
        logger.info(f"{'='*50}")

        return self.collected_cases


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Scrape case law from Indian Kanoon')
    parser.add_argument('--landmarks-only', action='store_true', help='Only scrape landmark cases')
    parser.add_argument('--topics-only', action='store_true', help='Only scrape by topics')
    parser.add_argument('--max-per-topic', type=int, default=30, help='Max cases per topic')
    parser.add_argument('--output', default='../../data/case_law', help='Output directory')

    args = parser.parse_args()

    scraper = IndianKanoonScraper(output_dir=args.output)

    include_landmarks = not args.topics_only
    include_topics = not args.landmarks_only

    scraper.run(
        include_topics=include_topics,
        include_landmarks=include_landmarks,
        max_per_topic=args.max_per_topic
    )

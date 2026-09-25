"""
India Code Scraper - Scrapes all Central Acts from indiacode.nic.in
Run: python scrape_india_code.py

This will collect:
- All Central Acts (200+)
- Section-wise breakdown
- Act metadata (year, act number, etc.)
"""

import requests
from bs4 import BeautifulSoup
import json
import time
import os
import re
from pathlib import Path
from urllib.parse import urljoin
import logging

# Setup logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('scrape_india_code.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

class IndiaCodeScraper:
    """Scraper for India Code website - Official repository of Indian laws"""

    BASE_URL = "https://www.indiacode.nic.in"

    # Important acts to prioritize
    PRIORITY_ACTS = [
        # New Criminal Laws (2023)
        "Bharatiya Nyaya Sanhita, 2023",
        "Bharatiya Nagarik Suraksha Sanhita, 2023",
        "Bharatiya Sakshya Adhiniyam, 2023",
        # Old Criminal Laws (still referenced)
        "Indian Penal Code, 1860",
        "Code of Criminal Procedure, 1973",
        "Indian Evidence Act, 1872",
        # Civil Laws
        "Indian Contract Act, 1872",
        "Transfer of Property Act, 1882",
        "Specific Relief Act, 1963",
        "Limitation Act, 1963",
        # Family Laws
        "Hindu Marriage Act, 1955",
        "Hindu Succession Act, 1956",
        "Muslim Personal Law (Shariat) Application Act, 1937",
        "Special Marriage Act, 1954",
        "Protection of Women from Domestic Violence Act, 2005",
        # Property & Land
        "Registration Act, 1908",
        "Indian Stamp Act, 1899",
        "Right to Fair Compensation and Transparency in Land Acquisition, Rehabilitation and Resettlement Act, 2013",
        # Labour Laws
        "Industrial Disputes Act, 1947",
        "Payment of Wages Act, 1936",
        "Minimum Wages Act, 1948",
        "Code on Wages, 2019",
        "Industrial Relations Code, 2020",
        "Code on Social Security, 2020",
        "Occupational Safety, Health and Working Conditions Code, 2020",
        # Consumer & Business
        "Consumer Protection Act, 2019",
        "Companies Act, 2013",
        "Negotiable Instruments Act, 1881",
        "Arbitration and Conciliation Act, 1996",
        "Insolvency and Bankruptcy Code, 2016",
        # Constitutional & Administrative
        "Constitution of India",
        "Right to Information Act, 2005",
        "Administrative Tribunals Act, 1985",
        # Technology & Modern Laws
        "Information Technology Act, 2000",
        "Digital Personal Data Protection Act, 2023",
        # Women & Children
        "Protection of Children from Sexual Offences Act, 2012",
        "Prohibition of Child Marriage Act, 2006",
        "Maternity Benefit Act, 1961",
        "Sexual Harassment of Women at Workplace Act, 2013",
        # Motor & Transport
        "Motor Vehicles Act, 1988",
        # Environment
        "Environment Protection Act, 1986",
    ]

    def __init__(self, output_dir="../../data/bare_acts/central"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
        })
        self.collected_acts = []

    def get_act_list_from_search(self, year_start=1800, year_end=2026):
        """Get list of acts by searching year by year"""
        all_acts = []

        for year in range(year_end, year_start - 1, -1):
            logger.info(f"Fetching acts from year {year}...")

            try:
                # India Code search endpoint
                search_url = f"{self.BASE_URL}/search/actSearch.action"
                params = {
                    'actYear': year,
                    'actType': 'Central',
                }

                response = self.session.get(search_url, params=params, timeout=30)
                if response.status_code != 200:
                    logger.warning(f"Failed to fetch year {year}: {response.status_code}")
                    continue

                soup = BeautifulSoup(response.text, 'html.parser')

                # Find act links
                for link in soup.select('a[href*="handle"]'):
                    act_title = link.text.strip()
                    act_url = urljoin(self.BASE_URL, link['href'])

                    if act_title and act_url not in [a['url'] for a in all_acts]:
                        all_acts.append({
                            'title': act_title,
                            'url': act_url,
                            'year': year
                        })

                time.sleep(1)  # Rate limiting

            except Exception as e:
                logger.error(f"Error fetching year {year}: {e}")
                continue

        logger.info(f"Found {len(all_acts)} acts total")
        return all_acts

    def get_priority_acts(self):
        """Search for priority acts by name"""
        acts = []

        for act_name in self.PRIORITY_ACTS:
            logger.info(f"Searching for: {act_name}")

            try:
                search_url = f"{self.BASE_URL}/search/actSearch.action"
                params = {'actName': act_name}

                response = self.session.get(search_url, params=params, timeout=30)
                soup = BeautifulSoup(response.text, 'html.parser')

                # Find first matching result
                for link in soup.select('a[href*="handle"]'):
                    if any(word.lower() in link.text.lower() for word in act_name.split()[:3]):
                        acts.append({
                            'title': link.text.strip(),
                            'url': urljoin(self.BASE_URL, link['href']),
                            'priority': True
                        })
                        break

                time.sleep(1)

            except Exception as e:
                logger.error(f"Error searching for {act_name}: {e}")

        return acts

    def scrape_act_page(self, act_url):
        """Scrape full act content from its page"""
        try:
            response = self.session.get(act_url, timeout=30)
            soup = BeautifulSoup(response.text, 'html.parser')

            act_data = {
                'url': act_url,
                'title': '',
                'act_number': '',
                'year': '',
                'date_of_enactment': '',
                'preamble': '',
                'sections': [],
                'chapters': [],
                'schedules': [],
                'amendments': [],
                'full_text': ''
            }

            # Extract title
            title_elem = soup.select_one('h1, h2, .act-title, .actTitle')
            if title_elem:
                act_data['title'] = title_elem.text.strip()

            # Extract act number and year from title
            title_match = re.search(r'(\d+)\s+of\s+(\d{4})', act_data['title'])
            if title_match:
                act_data['act_number'] = title_match.group(1)
                act_data['year'] = title_match.group(2)

            # Extract preamble
            preamble_elem = soup.select_one('.preamble, #preamble, [class*="preamble"]')
            if preamble_elem:
                act_data['preamble'] = preamble_elem.text.strip()

            # Extract sections
            section_elems = soup.select('.section, [id*="section"], [class*="section"]')
            for section in section_elems:
                section_data = self.parse_section(section)
                if section_data:
                    act_data['sections'].append(section_data)

            # If no sections found, try alternative parsing
            if not act_data['sections']:
                act_data['sections'] = self.parse_sections_alternative(soup)

            # Get full text as backup
            main_content = soup.select_one('.main-content, #content, article, .actContent')
            if main_content:
                act_data['full_text'] = main_content.get_text(separator='\n').strip()
            else:
                act_data['full_text'] = soup.get_text(separator='\n').strip()

            return act_data

        except Exception as e:
            logger.error(f"Error scraping {act_url}: {e}")
            return None

    def parse_section(self, section_elem):
        """Parse a single section element"""
        section_data = {
            'number': '',
            'title': '',
            'text': '',
            'subsections': [],
            'explanation': '',
            'illustrations': [],
            'exceptions': []
        }

        # Get section number
        num_elem = section_elem.select_one('.section-number, .secNum, h3, h4')
        if num_elem:
            num_match = re.search(r'(\d+[A-Z]?)', num_elem.text)
            if num_match:
                section_data['number'] = num_match.group(1)

        # Get section title
        title_elem = section_elem.select_one('.section-title, .marginal-note, em, strong')
        if title_elem:
            section_data['title'] = title_elem.text.strip()

        # Get section text
        text_elem = section_elem.select_one('.section-text, .secText, p')
        if text_elem:
            section_data['text'] = text_elem.text.strip()
        else:
            section_data['text'] = section_elem.get_text(separator=' ').strip()

        # Get subsections
        for subsec in section_elem.select('.subsection, [class*="subsec"]'):
            subsec_text = subsec.get_text(separator=' ').strip()
            if subsec_text:
                section_data['subsections'].append(subsec_text)

        # Get explanation
        explanation = section_elem.select_one('.explanation, [class*="explanation"]')
        if explanation:
            section_data['explanation'] = explanation.text.strip()

        # Get illustrations
        for illus in section_elem.select('.illustration, [class*="illustration"]'):
            section_data['illustrations'].append(illus.text.strip())

        return section_data if section_data['text'] else None

    def parse_sections_alternative(self, soup):
        """Alternative section parsing for different page structures"""
        sections = []

        # Try finding sections by pattern in text
        full_text = soup.get_text()

        # Pattern: "Section 1. Title.—Text..."
        section_pattern = r'Section\s+(\d+[A-Z]?)\.?\s*([^.—]+)?[.—]\s*([^§]+?)(?=Section\s+\d|$)'

        for match in re.finditer(section_pattern, full_text, re.IGNORECASE | re.DOTALL):
            sections.append({
                'number': match.group(1),
                'title': (match.group(2) or '').strip(),
                'text': match.group(3).strip()[:5000],  # Limit text length
                'subsections': [],
                'explanation': '',
                'illustrations': [],
                'exceptions': []
            })

        return sections

    def save_act(self, act_data, filename=None):
        """Save act data as JSON"""
        if not filename:
            # Create filename from title
            filename = re.sub(r'[^\w\s-]', '', act_data['title'].lower())
            filename = re.sub(r'[-\s]+', '_', filename)[:80]

        filepath = self.output_dir / f"{filename}.json"

        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(act_data, f, ensure_ascii=False, indent=2)

        logger.info(f"Saved: {filepath}")
        return filepath

    def create_manual_acts(self):
        """Create manual entries for critical new laws"""
        # These are the new criminal laws that replaced IPC/CrPC/Evidence Act

        new_criminal_laws = [
            {
                'title': 'Bharatiya Nyaya Sanhita, 2023',
                'act_number': '45',
                'year': '2023',
                'replaces': 'Indian Penal Code, 1860',
                'effective_from': '2024-07-01',
                'description': 'New criminal code replacing IPC. Contains provisions for offenses against body, property, state, public tranquility, and more.',
                'key_changes': [
                    'Section 103 replaces Section 302 IPC (Murder)',
                    'Section 63-69 covers sexual offenses (replaces 375-376 IPC)',
                    'New offense of mob lynching (Section 103(2))',
                    'Organized crime and terrorism offenses added',
                    'Community service as punishment introduced'
                ],
                'sections': [
                    {'number': '1', 'title': 'Short title, commencement and application', 'text': 'This Act may be called the Bharatiya Nyaya Sanhita, 2023. It shall come into force on such date as the Central Government may, by notification in the Official Gazette, appoint.'},
                    {'number': '103', 'title': 'Punishment for murder', 'text': 'Whoever commits murder shall be punished with death or imprisonment for life, and shall also be liable to fine. (2) When a group of five or more persons acting in concert commits murder on the ground of race, caste or community, sex, place of birth, language, personal belief or any other similar ground, each member of such group shall be punished with death or with imprisonment for life, and shall also be liable to fine.'},
                    {'number': '63', 'title': 'Rape', 'text': 'A man is said to commit rape if he performs sexual intercourse with a woman under circumstances falling under any of the descriptions provided in this section.'},
                    # Add more key sections...
                ]
            },
            {
                'title': 'Bharatiya Nagarik Suraksha Sanhita, 2023',
                'act_number': '46',
                'year': '2023',
                'replaces': 'Code of Criminal Procedure, 1973',
                'effective_from': '2024-07-01',
                'description': 'New criminal procedure code. Covers investigation, trial, bail, arrest procedures.',
                'key_changes': [
                    'Zero FIR mandatory',
                    'Mandatory videography of crime scenes',
                    'Timeline for investigation (90 days for offenses punishable up to 3 years)',
                    'Electronic summons and proceedings',
                    'Victim to be informed of investigation progress'
                ],
                'sections': [
                    {'number': '1', 'title': 'Short title, extent and commencement', 'text': 'This Act may be called the Bharatiya Nagarik Suraksha Sanhita, 2023.'},
                    {'number': '173', 'title': 'Information in cognizable cases (FIR)', 'text': 'Every information relating to the commission of a cognizable offence, if given orally to an officer in charge of a police station, shall be reduced to writing by him or under his direction.'},
                    {'number': '480', 'title': 'Bail in bailable offenses', 'text': 'When any person other than a person accused of a non-bailable offence is arrested or detained without warrant, he shall be released on bail.'},
                    {'number': '482', 'title': 'Anticipatory bail', 'text': 'When any person has reason to believe that he may be arrested on accusation of having committed a non-bailable offence, he may apply to the High Court or the Court of Session for a direction for grant of bail.'},
                    # Add more sections...
                ]
            },
            {
                'title': 'Bharatiya Sakshya Adhiniyam, 2023',
                'act_number': '47',
                'year': '2023',
                'replaces': 'Indian Evidence Act, 1872',
                'effective_from': '2024-07-01',
                'description': 'New evidence law. Recognizes electronic evidence, DNA, modern forensics.',
                'key_changes': [
                    'Electronic and digital records as primary evidence',
                    'Admissibility of electronic evidence simplified',
                    'DNA evidence provisions',
                    'Joint trials provisions updated'
                ],
                'sections': [
                    {'number': '1', 'title': 'Short title, extent and commencement', 'text': 'This Act may be called the Bharatiya Sakshya Adhiniyam, 2023.'},
                    {'number': '57', 'title': 'Admissibility of electronic records', 'text': 'Any information contained in an electronic record which is printed on paper, stored, recorded or copied in optical or magnetic media produced by a computer shall be deemed to be a document.'},
                    # Add more sections...
                ]
            }
        ]

        for act in new_criminal_laws:
            self.save_act(act)
            self.collected_acts.append(act)

    def run(self, priority_only=False, limit=None):
        """Run the scraper"""
        logger.info("Starting India Code scraper...")

        # First, create manual entries for new laws
        logger.info("Creating manual entries for new criminal laws...")
        self.create_manual_acts()

        if priority_only:
            acts = self.get_priority_acts()
        else:
            acts = self.get_act_list_from_search()

        if limit:
            acts = acts[:limit]

        logger.info(f"Processing {len(acts)} acts...")

        for i, act in enumerate(acts):
            logger.info(f"[{i+1}/{len(acts)}] Scraping: {act['title'][:50]}...")

            try:
                act_data = self.scrape_act_page(act['url'])
                if act_data:
                    self.save_act(act_data)
                    self.collected_acts.append(act_data)
            except Exception as e:
                logger.error(f"Failed to scrape {act['title']}: {e}")

            time.sleep(2)  # Rate limiting

        # Save index
        index_path = self.output_dir / '_index.json'
        with open(index_path, 'w', encoding='utf-8') as f:
            json.dump({
                'total_acts': len(self.collected_acts),
                'acts': [{'title': a.get('title', ''), 'file': f"{a.get('title', '')[:80]}.json"}
                        for a in self.collected_acts]
            }, f, ensure_ascii=False, indent=2)

        logger.info(f"Completed! Scraped {len(self.collected_acts)} acts.")
        return self.collected_acts


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Scrape Indian laws from India Code')
    parser.add_argument('--priority-only', action='store_true', help='Only scrape priority acts')
    parser.add_argument('--limit', type=int, help='Limit number of acts to scrape')
    parser.add_argument('--output', default='../../data/bare_acts/central', help='Output directory')

    args = parser.parse_args()

    scraper = IndiaCodeScraper(output_dir=args.output)
    scraper.run(priority_only=args.priority_only, limit=args.limit)

"""
Training Data Generator - Converts scraped legal data into fine-tuning format
Run: python create_training_data.py

This creates Q&A pairs for:
- Act/Section explanations
- Case summaries
- Legal procedures
- Document drafting
- Multilingual support (Hindi, Kannada, Marathi)
"""

import json
import os
import re
import random
from pathlib import Path
from typing import List, Dict, Any
import logging
from datetime import datetime

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class LegalTrainingDataGenerator:
    """Generate training data for fine-tuning legal LLM"""

    def __init__(self, data_dir="../../data", output_file="../../data/training_data.json"):
        self.data_dir = Path(data_dir)
        self.output_file = Path(output_file)
        self.training_examples = []

    def load_json_files(self, directory: Path) -> List[Dict]:
        """Load all JSON files from a directory"""
        data = []
        if not directory.exists():
            logger.warning(f"Directory not found: {directory}")
            return data

        for json_file in directory.glob("**/*.json"):
            if json_file.name.startswith('_'):  # Skip index files
                continue
            try:
                with open(json_file, 'r', encoding='utf-8') as f:
                    data.append(json.load(f))
            except Exception as e:
                logger.error(f"Error loading {json_file}: {e}")

        return data

    def add_example(self, instruction: str, input_text: str, output: str,
                    category: str = None, language: str = "english"):
        """Add a training example"""
        if not instruction or not output:
            return

        example = {
            "instruction": instruction.strip(),
            "input": input_text.strip() if input_text else "",
            "output": output.strip(),
            "metadata": {
                "category": category,
                "language": language
            }
        }
        self.training_examples.append(example)

    # =========================================
    # SECTION Q&A GENERATION
    # =========================================

    def generate_section_qa(self, act_data: Dict):
        """Generate Q&A pairs from act sections"""
        act_name = act_data.get('title', 'Unknown Act')
        year = act_data.get('year', '')

        for section in act_data.get('sections', []):
            section_num = section.get('number', '')
            section_title = section.get('title', '')
            section_text = section.get('text', '')

            if not section_text or len(section_text) < 50:
                continue

            # Multiple question variations
            questions = [
                f"What does Section {section_num} of {act_name} state?",
                f"Explain Section {section_num} of {act_name}.",
                f"What is the provision under Section {section_num} of {act_name}?",
                f"Section {section_num} {act_name} - explain in detail.",
            ]

            if section_title:
                questions.extend([
                    f"What does {act_name} say about {section_title.lower()}?",
                    f"Explain the provision for {section_title.lower()} under {act_name}.",
                ])

            # Create comprehensive answer
            answer = f"**Section {section_num} of {act_name}**"
            if section_title:
                answer += f" - {section_title}"
            answer += f"\n\n{section_text}"

            # Add subsections if available
            subsections = section.get('subsections', [])
            if subsections:
                answer += "\n\n**Subsections:**\n"
                for i, subsec in enumerate(subsections[:5], 1):
                    answer += f"({i}) {subsec}\n"

            # Add explanation if available
            if section.get('explanation'):
                answer += f"\n\n**Explanation:** {section['explanation']}"

            # Add illustrations if available
            illustrations = section.get('illustrations', [])
            if illustrations:
                answer += "\n\n**Illustrations:**\n"
                for illus in illustrations[:3]:
                    answer += f"- {illus}\n"

            # Add to training data with random question variation
            for q in random.sample(questions, min(2, len(questions))):
                self.add_example(q, "", answer, category="statute", language="english")

    def generate_act_overview_qa(self, act_data: Dict):
        """Generate overview Q&A for entire acts"""
        act_name = act_data.get('title', '')
        if not act_name:
            return

        # Count sections
        num_sections = len(act_data.get('sections', []))
        year = act_data.get('year', '')
        preamble = act_data.get('preamble', '')
        description = act_data.get('description', '')

        questions = [
            f"What is {act_name}?",
            f"Give an overview of {act_name}.",
            f"Explain the purpose and scope of {act_name}.",
        ]

        answer = f"**{act_name}**\n\n"

        if year:
            answer += f"**Year of Enactment:** {year}\n\n"

        if description:
            answer += f"**Purpose:** {description}\n\n"
        elif preamble:
            answer += f"**Preamble:** {preamble[:500]}...\n\n"

        if num_sections > 0:
            answer += f"**Structure:** This Act contains {num_sections} sections.\n\n"

        # Key sections summary
        key_sections = act_data.get('sections', [])[:5]
        if key_sections:
            answer += "**Key Provisions:**\n"
            for sec in key_sections:
                if sec.get('number') and sec.get('title'):
                    answer += f"- Section {sec['number']}: {sec['title']}\n"

        # Key changes (for new laws)
        if act_data.get('key_changes'):
            answer += "\n**Key Changes:**\n"
            for change in act_data['key_changes']:
                answer += f"- {change}\n"

        # Replaces info
        if act_data.get('replaces'):
            answer += f"\n**Replaces:** {act_data['replaces']}"

        for q in questions:
            self.add_example(q, "", answer, category="statute_overview", language="english")

    # =========================================
    # CASE LAW Q&A GENERATION
    # =========================================

    def generate_case_qa(self, case_data: Dict):
        """Generate Q&A pairs from case law"""
        title = case_data.get('title', '')
        if not title:
            return

        court = case_data.get('court', '')
        date = case_data.get('date', '')
        held = case_data.get('held', '')
        facts = case_data.get('facts', '')
        ratio = case_data.get('ratio_decidendi', '')
        full_text = case_data.get('full_text', '')

        # Case summary question
        if held or ratio or full_text:
            questions = [
                f"What was the decision in {title}?",
                f"Summarize the case {title}.",
                f"What did the court hold in {title}?",
            ]

            answer = f"**Case:** {title}\n"
            if court:
                answer += f"**Court:** {court}\n"
            if date:
                answer += f"**Date:** {date}\n"
            answer += "\n"

            if facts:
                answer += f"**Facts:** {facts[:1000]}\n\n"

            if held:
                answer += f"**Held:** {held[:1500]}\n\n"
            elif full_text:
                # Extract holding from full text
                answer += f"**Summary:** {full_text[:2000]}...\n\n"

            if ratio:
                answer += f"**Ratio Decidendi (Legal Principle):** {ratio}\n"

            # Cited statutes
            statutes = case_data.get('statutes_cited', [])
            if statutes:
                answer += f"\n**Statutes Cited:** {', '.join(statutes[:5])}"

            for q in questions[:2]:
                self.add_example(q, "", answer, category="case_law", language="english")

        # Case issues question
        issues = case_data.get('issues', [])
        if issues:
            q = f"What were the legal issues in {title}?"
            answer = f"**Legal Issues in {title}:**\n\n"
            for i, issue in enumerate(issues, 1):
                answer += f"{i}. {issue}\n"

            self.add_example(q, "", answer, category="case_law", language="english")

    # =========================================
    # LEGAL PROCEDURES
    # =========================================

    def generate_procedure_qa(self):
        """Generate Q&A for common legal procedures"""
        procedures = [
            # FIR Filing
            {
                "questions": [
                    "How do I file an FIR in India?",
                    "What is the procedure to lodge an FIR?",
                    "Steps to file a First Information Report",
                    "FIR kaise darj karein?",  # Hindi
                ],
                "answer": """**How to File an FIR (First Information Report) in India**

**Step 1: Determine Jurisdiction**
Go to the police station that has jurisdiction over the area where the offense occurred.

**Step 2: Who Can File**
- The victim
- Any person who has knowledge of the offense
- Police officer who learns about it

**Step 3: Filing Process**
1. Visit the police station
2. Give an oral or written complaint to the Station House Officer (SHO)
3. The officer must record the information in writing
4. Sign the written FIR after verifying contents
5. Obtain a free copy of the FIR (your legal right under Section 173 BNSS)

**Step 4: Important Points**
- FIR registration is mandatory for cognizable offenses
- Police cannot refuse to register FIR (Lalita Kumari v. State of UP)
- You can file Zero FIR at any police station
- Online FIR facility available in many states

**If Police Refuses:**
1. Send written complaint to Superintendent of Police
2. File complaint before Judicial Magistrate (Section 200 BNSS)
3. File writ petition in High Court

**Legal Basis:** Section 173 of Bharatiya Nagarik Suraksha Sanhita (BNSS), 2023 [formerly Section 154 CrPC]""",
                "category": "procedure"
            },
            # Bail
            {
                "questions": [
                    "How to apply for bail in India?",
                    "What are the types of bail?",
                    "Bail procedure in criminal cases",
                    "Anticipatory bail kaise milti hai?",
                ],
                "answer": """**Bail in Indian Criminal Law**

**Types of Bail:**

1. **Regular Bail** (Section 480-481 BNSS)
   - Applied after arrest
   - Filed before Sessions Court or High Court
   - For both bailable and non-bailable offenses

2. **Anticipatory Bail** (Section 482 BNSS)
   - Applied BEFORE arrest
   - When there's apprehension of arrest
   - Filed in Sessions Court or High Court

3. **Interim Bail**
   - Temporary bail pending regular bail hearing
   - Short duration (days to weeks)

**Factors Courts Consider:**
- Nature and gravity of offense
- Character of evidence
- Severity of punishment
- Criminal antecedents
- Flight risk
- Likelihood of tampering with evidence/witnesses
- Health and age of accused

**Procedure:**
1. File bail application with relevant court
2. Attach supporting documents (ID, address proof, surety details)
3. Court issues notice to prosecution
4. Hearing conducted
5. If granted, furnish bail bond and surety

**Key Judgments:**
- Arnesh Kumar v. State of Bihar (2014) - Arrest guidelines
- Sanjay Chandra v. CBI (2012) - Bail is rule, jail is exception
- P. Chidambaram v. CBI (2019) - Economic offenses bail""",
                "category": "procedure"
            },
            # Divorce
            {
                "questions": [
                    "How to file for divorce in India?",
                    "Divorce procedure under Hindu law",
                    "What are grounds for divorce in India?",
                    "Mutual consent divorce kitne time mein hota hai?",
                ],
                "answer": """**Divorce Procedure in India**

**Types of Divorce:**

1. **Mutual Consent Divorce** (Section 13B Hindu Marriage Act)
   - Both parties agree to separate
   - 6 months waiting period (can be waived)
   - Fastest method

2. **Contested Divorce** (Section 13 Hindu Marriage Act)
   - One party files against the other
   - Based on specific grounds
   - Can take 2-5 years

**Grounds for Divorce (Section 13):**
- Adultery
- Cruelty (physical or mental)
- Desertion for 2+ years
- Conversion to another religion
- Mental disorder
- Leprosy
- Venereal disease
- Renunciation of world
- Not heard alive for 7 years

**Additional Grounds for Wife:**
- Husband guilty of rape/sodomy/bestiality
- Non-resumption of cohabitation after maintenance decree

**Procedure:**
1. File petition in Family Court (where parties last resided together or where marriage was solemnized)
2. Court issues notice to other party
3. Attempts reconciliation (mandatory)
4. Evidence and arguments
5. Decree of divorce

**Documents Required:**
- Marriage certificate
- Address proof
- Photos
- Evidence of grounds (if contested)
- Income proof (for maintenance)

**Key Cases:**
- Shilpa Sailesh v. Varun Sreenivasan (2023) - Supreme Court can grant divorce directly
- Naveen Kohli v. Neelu Kohli (2006) - Irretrievable breakdown""",
                "category": "procedure"
            },
            # Consumer Complaint
            {
                "questions": [
                    "How to file a consumer complaint in India?",
                    "Consumer court mein case kaise kare?",
                    "Consumer Protection Act complaint procedure",
                ],
                "answer": """**Filing a Consumer Complaint in India**

**Under Consumer Protection Act, 2019**

**Who Can File:**
- Consumer who bought goods/services
- Recognized consumer association
- Central/State Government
- Legal heirs (if consumer deceased)

**Jurisdiction Based on Value:**
- Up to ₹1 Crore → District Commission
- ₹1 Crore to ₹10 Crore → State Commission
- Above ₹10 Crore → National Commission

**Grounds for Complaint:**
- Defective goods
- Deficient services
- Unfair trade practices
- Overcharging
- Misleading advertisements

**Procedure:**
1. Send legal notice to opposite party
2. File complaint (online at edaakhil.nic.in or offline)
3. Pay nominal fee (₹100 to ₹5000 based on value)
4. Attach documents (bill, warranty, correspondence)
5. Hearing conducted
6. Order passed within 3-5 months (ideally)

**Documents Required:**
- Purchase receipt/invoice
- Warranty card
- Photos of defective product
- Communication with seller
- Any expert report

**Remedies Available:**
- Replacement of goods
- Refund
- Compensation for loss
- Discontinuation of unfair practice
- Costs of litigation

**Time Limit:** 2 years from cause of action""",
                "category": "procedure"
            },
            # Property Registration
            {
                "questions": [
                    "How to register a property in India?",
                    "Property registration procedure",
                    "What is stamp duty and registration charges?",
                ],
                "answer": """**Property Registration in India**

**Legal Requirement:**
Under Section 17 of Registration Act, 1908, registration is compulsory for:
- Sale deed
- Gift deed
- Lease deed (1 year+)
- Exchange deed

**Step-by-Step Procedure:**

**Step 1: Document Preparation**
- Draft sale deed/conveyance deed
- Include property description, consideration, terms
- Get it vetted by lawyer

**Step 2: Stamp Duty Payment**
- Pay stamp duty (varies by state: 5-8% typically)
- Purchase stamp paper or pay online
- Karnataka: 5.6% | Maharashtra: 5-6% | Delhi: 4-6%

**Step 3: Visit Sub-Registrar Office**
- Both buyer and seller must be present
- Bring two witnesses
- Submit documents

**Step 4: Verification**
- Officer verifies identities
- Checks property documents
- Verifies stamp duty payment

**Step 5: Registration**
- Pay registration fee (1% typically)
- Biometric/photo capture
- Document scanned and registered

**Step 6: Collect Registered Document**
- Collect after 1-15 days
- Verify all entries

**Documents Required:**
- Original title deed of seller
- Encumbrance certificate
- Property tax receipts
- ID proof of parties
- Photos
- NOC (if applicable)

**Important:**
- Registration within 4 months of execution
- Late fee if delayed
- Document invalid if unregistered (for compulsory registration documents)""",
                "category": "procedure"
            },
            # RTI Application
            {
                "questions": [
                    "How to file an RTI application?",
                    "RTI application procedure in India",
                    "Right to Information Act - how to apply?",
                ],
                "answer": """**Filing an RTI Application in India**

**Under Right to Information Act, 2005**

**Who Can Apply:**
Any Indian citizen (not available to corporations, foreigners, or persons who ceased to be citizens)

**What Information Can Be Sought:**
- Any information held by public authority
- Records, documents, files
- Details of decisions and reasons
- Contracts, reports, data

**Exemptions (Section 8):**
- National security matters
- Cabinet papers
- Trade secrets
- Personal information (privacy)

**Procedure:**

**Step 1: Identify Public Authority**
Determine which government department holds the information

**Step 2: Write Application**
- Address to Public Information Officer (PIO)
- Include your name and address
- Specify information required clearly
- No need to give reason for seeking info

**Step 3: Pay Fee**
- ₹10 for Central Government
- Varies for State (₹10-50)
- BPL applicants exempted (attach proof)

**Step 4: Submit**
- Online: rtionline.gov.in (Central)
- State portals for state matters
- Offline: By post or in person

**Step 5: Response Timeline**
- 30 days for normal information
- 48 hours if life/liberty involved
- If transferred: 35 days

**First Appeal:**
- To First Appellate Authority within 30 days
- If not satisfied

**Second Appeal:**
- To Central/State Information Commission
- Within 90 days of first appeal decision

**Sample Application Format:**
```
To,
The Public Information Officer
[Department Name]
[Address]

Subject: Application under RTI Act, 2005

Sir/Madam,
I request the following information under RTI Act:
1. [Specific question]
2. [Specific question]

Fee of ₹10 paid via [postal order/DD/online]

Yours faithfully,
[Name]
[Address]
[Phone]
```""",
                "category": "procedure"
            },
        ]

        for proc in procedures:
            for q in proc["questions"]:
                lang = "hindi" if any(hindi_word in q.lower() for hindi_word in ["kaise", "kya", "kitne", "mein"]) else "english"
                self.add_example(q, "", proc["answer"], category=proc["category"], language=lang)

    # =========================================
    # DOCUMENT DRAFTING
    # =========================================

    def generate_document_drafting_qa(self):
        """Generate Q&A for legal document drafting"""
        documents = [
            # Legal Notice
            {
                "instruction": "Draft a legal notice for recovery of money",
                "input": "Creditor: Suresh Kumar, Debtor: Mahesh Gupta, Amount: Rs 5,00,000, Loan Date: 01-Jan-2024, Due Date: 01-Jul-2024, Creditor Address: Delhi",
                "output": """**LEGAL NOTICE FOR RECOVERY OF MONEY**

REGD. A.D.

To,
Shri Mahesh Gupta
[Address]

**Subject: Legal Notice for Recovery of Rs. 5,00,000/-**

Under instructions from and on behalf of my client, Shri Suresh Kumar, resident of Delhi, I do hereby serve upon you the following Legal Notice:

1. That my client had advanced a sum of Rs. 5,00,000/- (Rupees Five Lakhs Only) to you on 01-Jan-2024 as a friendly loan upon your request and assurance of repayment.

2. That you had agreed and undertaken to repay the said amount along with interest by 01-Jul-2024.

3. That the due date for repayment has long passed, and despite repeated requests and reminders, you have failed, neglected, and avoided repaying the said amount to my client.

4. That the said non-payment amounts to breach of your contractual obligation and causes wrongful loss to my client and wrongful gain to yourself.

5. That my client is entitled to recover the principal sum of Rs. 5,00,000/- along with interest @ 18% per annum from the date of default till realization.

6. I, therefore, on behalf of my client, call upon you to pay the sum of Rs. 5,00,000/- along with accrued interest within 15 days from the receipt of this Notice.

7. In case of non-compliance, my client shall be constrained to initiate appropriate civil and criminal proceedings against you for recovery of the said amount along with interest, damages, and costs, entirely at your risk and consequences.

Please note that this Notice is issued without prejudice to any other rights and remedies available to my client under law.

Place: Delhi
Date: [Date]

[Advocate Name]
Advocate
[Enrollment Number]
[Address]

Copy to Client for information.""",
                "category": "document_drafting"
            },
            # Rent Agreement
            {
                "instruction": "Draft a rent agreement",
                "input": "Landlord: Ramesh Sharma, Tenant: Amit Kumar, Property: Flat No. 101, ABC Apartments, Mumbai, Rent: Rs 25,000/month, Security: Rs 1,00,000, Duration: 11 months",
                "output": """**RENT AGREEMENT**

This Rent Agreement is made and executed on this _____ day of _______, 2024

**BETWEEN**

**Shri Ramesh Sharma**, residing at _________________ (hereinafter called the "LANDLORD/LESSOR", which expression shall include his heirs, successors, and assigns) of the FIRST PART;

**AND**

**Shri Amit Kumar**, residing at _________________ (hereinafter called the "TENANT/LESSEE", which expression shall include his heirs, successors, and assigns) of the SECOND PART.

**WHEREAS** the Landlord is the absolute owner of Flat No. 101, ABC Apartments, Mumbai, and the Tenant has approached the Landlord for taking the said premises on rent for residential purposes.

**NOW THIS AGREEMENT WITNESSETH AS FOLLOWS:**

**1. PREMISES:** The Landlord hereby lets out and the Tenant hereby takes on rent Flat No. 101, ABC Apartments, Mumbai, measuring approximately ____ sq. ft. (hereinafter referred to as "Demised Premises").

**2. PERIOD:** The tenancy shall commence from _______ and shall be for a period of 11 (Eleven) months, unless terminated earlier.

**3. RENT:** The monthly rent shall be Rs. 25,000/- (Rupees Twenty-Five Thousand Only), payable on or before the 5th day of each English calendar month.

**4. SECURITY DEPOSIT:** The Tenant has paid a sum of Rs. 1,00,000/- (Rupees One Lakh Only) as interest-free refundable security deposit. The same shall be refunded after deducting any dues/damages at the time of vacating.

**5. MAINTENANCE:** Electricity, water, and maintenance charges shall be borne by the Tenant separately.

**6. USE OF PREMISES:** The premises shall be used solely for residential purposes by the Tenant and his immediate family members.

**7. SUBLETTING:** The Tenant shall not sublet, assign, or part with the possession of the premises.

**8. REPAIRS:** Minor repairs shall be done by the Tenant. Major structural repairs shall be the responsibility of the Landlord.

**9. TERMINATION:** Either party may terminate this agreement by giving one month's prior written notice.

**10. INSPECTION:** The Landlord or his authorized agent may inspect the premises with prior appointment.

**11. RETURN OF PREMISES:** Upon termination, the Tenant shall hand over vacant and peaceful possession in good condition (subject to normal wear and tear).

**12. JURISDICTION:** Courts in Mumbai shall have exclusive jurisdiction.

IN WITNESS WHEREOF, the parties have signed this agreement on the date first written above.

**LANDLORD**                           **TENANT**
Signature: ____________               Signature: ____________
Name: Ramesh Sharma                   Name: Amit Kumar

**WITNESSES:**
1. Name: ____________  Signature: ____________
2. Name: ____________  Signature: ____________""",
                "category": "document_drafting"
            },
            # FIR Draft
            {
                "instruction": "Draft an FIR for theft",
                "input": "Complainant: Priya Verma, Incident: Gold chain snatched while walking, Location: MG Road, Bangalore, Date: 15-Sep-2024, Time: 8:30 PM, Description: Two men on bike snatched gold chain worth Rs 80,000",
                "output": """**FIRST INFORMATION REPORT (FIR)**

**To,**
The Station House Officer
[Concerned Police Station]
MG Road, Bangalore

**Subject: FIR for Snatching/Theft**

Respected Sir/Madam,

I, Priya Verma, D/o [Father's Name], Age: ___ years, R/o [Complete Address], do hereby lodge this complaint as follows:

**DATE & TIME OF INCIDENT:** 15-Sep-2024 at approximately 8:30 PM

**PLACE OF INCIDENT:** MG Road, near [landmark], Bangalore

**DETAILS OF INCIDENT:**

On the above-mentioned date and time, I was walking on MG Road towards [destination]. At that time, two unknown persons came on a motorcycle from behind. The pillion rider snatched my gold chain from my neck while the bike was in motion.

**DESCRIPTION OF STOLEN PROPERTY:**
- One gold chain
- Approximate weight: ___ grams
- Approximate value: Rs. 80,000/- (Rupees Eighty Thousand Only)
- Description: [Color, design, any unique features]

**DESCRIPTION OF ACCUSED PERSONS:**
1. Rider: Male, approximately ___ years old, wearing [clothes description]
2. Pillion: Male, approximately ___ years old, wearing [clothes description]
- Motorcycle: [Color], could not note registration number

**WITNESSES (if any):**
[Names and contact details of witnesses]

**INJURIES (if any):**
[Description of any injuries sustained]

I request you to kindly:
1. Register this FIR
2. Investigate the matter
3. Arrest the culprits
4. Recover my stolen property

I undertake that the above information is true to the best of my knowledge and belief.

Place: Bangalore
Date: ___________

**Complainant:**
Name: Priya Verma
Signature: ____________
Contact: ____________
Address: ____________

---
*Note: This is a draft. Actual FIR will be written by the police officer based on your statement.*""",
                "category": "document_drafting"
            },
            # Affidavit
            {
                "instruction": "Draft a general affidavit",
                "input": "Deponent: Rajesh Patel, Purpose: Name correction in documents, Father's name in some documents shows 'Mohan' instead of 'Mohan Lal'",
                "output": """**AFFIDAVIT**

I, **Rajesh Patel**, S/o Shri Mohan Lal Patel, aged about ___ years, R/o _________________ [Complete Address], do hereby solemnly affirm and declare as under:

1. That I am the deponent above named and am a citizen of India.

2. That I am competent to swear this affidavit and am well conversant with the facts stated herein.

3. That my father's full name is "**MOHAN LAL PATEL**".

4. That in some of my documents, my father's name has been inadvertently mentioned as "**MOHAN**" instead of "**MOHAN LAL**".

5. That I declare that "**MOHAN**" and "**MOHAN LAL PATEL**" refer to one and the same person, i.e., my father.

6. That there is no change in the identity of my father, and the variation is only due to clerical/typographical error.

7. That I am filing this affidavit to clarify the discrepancy and for all official purposes.

8. That the contents of this affidavit are true and correct to the best of my knowledge and belief, and nothing material has been concealed therefrom.

**DEPONENT**

**VERIFICATION**

I, the above-named deponent, do hereby verify that the contents of this affidavit are true and correct to my knowledge and belief, no part of it is false, and nothing material has been concealed therefrom.

Verified at _________ on this _____ day of _________, 2024.

**DEPONENT**

---
*Sworn before me on this _____ day of _________, 2024*

**Notary Public / Oath Commissioner**
[Seal]""",
                "category": "document_drafting"
            },
        ]

        for doc in documents:
            self.add_example(
                doc["instruction"],
                doc.get("input", ""),
                doc["output"],
                category=doc["category"]
            )

    # =========================================
    # MULTILINGUAL Q&A
    # =========================================

    def generate_multilingual_qa(self):
        """Generate Q&A in Hindi, Kannada, and Marathi"""

        multilingual_qa = [
            # Hindi
            {
                "instruction": "धारा 302 आईपीसी में क्या प्रावधान है?",
                "input": "",
                "output": """**धारा 302 भारतीय दंड संहिता (आईपीसी) / धारा 103 भारतीय न्याय संहिता (बीएनएस)**

यह धारा हत्या के लिए सजा का प्रावधान करती है।

**प्रावधान:**
जो कोई हत्या करेगा, उसे मृत्युदंड या आजीवन कारावास की सजा दी जाएगी और वह जुर्माने के लिए भी उत्तरदायी होगा।

**आवश्यक तत्व:**
1. किसी मनुष्य की मृत्यु हुई हो
2. ऐसी मृत्यु आरोपी के कृत्य से हुई हो
3. आरोपी का इरादा मृत्यु कारित करना था, या ऐसी शारीरिक चोट पहुंचाना था जिससे मृत्यु होने की संभावना हो

**दंड:**
- मृत्युदंड (फांसी), या
- आजीवन कारावास, और
- जुर्माना

**नोट:** 2024 से भारतीय न्याय संहिता (BNS) लागू हो गई है जिसमें धारा 103 यही प्रावधान करती है।

**महत्वपूर्ण निर्णय:**
- बचन सिंह बनाम पंजाब राज्य (1980) - मृत्युदंड "दुर्लभतम में दुर्लभ" मामलों में
- माछी सिंह बनाम पंजाब राज्य (1983) - दुर्लभतम मामलों के मानदंड""",
                "language": "hindi"
            },
            {
                "instruction": "जमानत कैसे मिलती है?",
                "input": "",
                "output": """**जमानत (Bail) कैसे प्राप्त करें**

**जमानत के प्रकार:**

1. **नियमित जमानत (Regular Bail)**
   - गिरफ्तारी के बाद आवेदन
   - सत्र न्यायालय या उच्च न्यायालय में

2. **अग्रिम जमानत (Anticipatory Bail)**
   - गिरफ्तारी से पहले
   - जब गिरफ्तारी की आशंका हो
   - धारा 482 बीएनएसएस के तहत

**प्रक्रिया:**
1. वकील के माध्यम से आवेदन दायर करें
2. आवश्यक दस्तावेज संलग्न करें
3. न्यायालय सुनवाई करेगा
4. जमानत मिलने पर जमानती बांड भरें

**न्यायालय किन बातों पर विचार करता है:**
- अपराध की प्रकृति
- सबूतों का स्वरूप
- भागने की संभावना
- गवाहों को प्रभावित करने की संभावना
- आरोपी का पूर्व आपराधिक रिकॉर्ड

**महत्वपूर्ण:**
- जमानतीय अपराधों में जमानत अधिकार है
- गैर-जमानतीय अपराधों में न्यायालय का विवेक""",
                "language": "hindi"
            },
            # Kannada
            {
                "instruction": "ಭಾರತದಲ್ಲಿ FIR ಹೇಗೆ ದಾಖಲಿಸುವುದು?",
                "input": "",
                "output": """**FIR (ಪ್ರಥಮ ಮಾಹಿತಿ ವರದಿ) ದಾಖಲಿಸುವ ವಿಧಾನ**

**ಹಂತ 1: ಪೊಲೀಸ್ ಠಾಣೆಗೆ ಹೋಗಿ**
- ಅಪರಾಧ ನಡೆದ ಪ್ರದೇಶದ ಅಧಿಕಾರ ವ್ಯಾಪ್ತಿಯ ಪೊಲೀಸ್ ಠಾಣೆ
- ಅಥವಾ ಯಾವುದೇ ಪೊಲೀಸ್ ಠಾಣೆಯಲ್ಲಿ Zero FIR ದಾಖಲಿಸಬಹುದು

**ಹಂತ 2: ದೂರು ನೀಡಿ**
- ಬರವಣಿಗೆಯಲ್ಲಿ ಅಥವಾ ಮೌಖಿಕವಾಗಿ
- ಘಟನೆಯ ಸಂಪೂರ್ಣ ವಿವರಗಳನ್ನು ತಿಳಿಸಿ
- ದಿನಾಂಕ, ಸಮಯ, ಸ್ಥಳ, ಆರೋಪಿಗಳ ವಿವರ

**ಹಂತ 3: FIR ಪ್ರತಿ ಪಡೆಯಿರಿ**
- ಇದು ನಿಮ್ಮ ಕಾನೂನು ಹಕ್ಕು
- ಉಚಿತವಾಗಿ ನೀಡಬೇಕು

**ಪೊಲೀಸರು ನಿರಾಕರಿಸಿದರೆ:**
1. SP ಗೆ ಲಿಖಿತ ದೂರು ಕಳುಹಿಸಿ
2. ನ್ಯಾಯಾಧೀಶರ ಮುಂದೆ ದೂರು ದಾಖಲಿಸಿ
3. ಹೈಕೋರ್ಟ್‌ನಲ್ಲಿ ರಿಟ್ ಅರ್ಜಿ ಸಲ್ಲಿಸಿ

**ಕಾನೂನು ಆಧಾರ:** ಭಾರತೀಯ ನಾಗರಿಕ ಸುರಕ್ಷಾ ಸಂಹಿತೆ ಸೆಕ್ಷನ್ 173""",
                "language": "kannada"
            },
            {
                "instruction": "ಜಾಮೀನು ಎಂದರೇನು?",
                "input": "",
                "output": """**ಜಾಮೀನು (Bail) ಬಗ್ಗೆ ಮಾಹಿತಿ**

**ಜಾಮೀನು ಎಂದರೆ:**
ಕ್ರಿಮಿನಲ್ ಪ್ರಕರಣದಲ್ಲಿ ಆರೋಪಿಯನ್ನು ವಿಚಾರಣೆ ಮುಗಿಯುವವರೆಗೆ ಜೈಲಿನಿಂದ ತಾತ್ಕಾಲಿಕವಾಗಿ ಬಿಡುಗಡೆ ಮಾಡುವುದು.

**ಜಾಮೀನಿನ ವಿಧಗಳು:**

1. **ನಿಯಮಿತ ಜಾಮೀನು**
   - ಬಂಧನದ ನಂತರ
   - ಸೆಷನ್ಸ್ ಕೋರ್ಟ್ ಅಥವಾ ಹೈಕೋರ್ಟ್‌ನಲ್ಲಿ

2. **ಮುಂಚಿತ ಜಾಮೀನು**
   - ಬಂಧನಕ್ಕೆ ಮುನ್ನ
   - ಬಂಧನದ ಭಯವಿದ್ದಾಗ

**ಅರ್ಜಿ ಹೇಗೆ:**
- ವಕೀಲರ ಮೂಲಕ ಅರ್ಜಿ ಸಲ್ಲಿಸಿ
- ಗುರುತಿನ ದಾಖಲೆಗಳು, ವಿಳಾಸ ಪ್ರಮಾಣ ಲಗತ್ತಿಸಿ
- ಜಾಮೀನು ಸಿಕ್ಕರೆ ಜಾಮೀನು ಬಾಂಡ್ ಸಲ್ಲಿಸಿ

**ನ್ಯಾಯಾಲಯ ಪರಿಗಣಿಸುವ ಅಂಶಗಳು:**
- ಅಪರಾಧದ ಸ್ವರೂಪ
- ಪುರಾವೆಗಳ ಬಲ
- ಓಡಿಹೋಗುವ ಸಾಧ್ಯತೆ
- ಸಾಕ್ಷಿಗಳ ಮೇಲೆ ಪ್ರಭಾವ""",
                "language": "kannada"
            },
            # Marathi
            {
                "instruction": "भारतात FIR कसा नोंदवावा?",
                "input": "",
                "output": """**FIR (प्रथम माहिती अहवाल) नोंदणी प्रक्रिया**

**पायरी 1: पोलीस स्टेशनला जा**
- गुन्हा घडलेल्या ठिकाणच्या अधिकार क्षेत्रातील पोलीस स्टेशन
- किंवा कोणत्याही पोलीस स्टेशनमध्ये Zero FIR नोंदवता येते

**पायरी 2: तक्रार द्या**
- लेखी किंवा तोंडी
- घटनेची संपूर्ण माहिती द्या
- तारीख, वेळ, ठिकाण, आरोपींचे वर्णन

**पायरी 3: FIR ची प्रत घ्या**
- हा तुमचा कायदेशीर हक्क आहे
- विनामूल्य मिळणे आवश्यक

**पोलिसांनी नकार दिल्यास:**
1. SP ला लेखी तक्रार पाठवा
2. न्यायदंडाधिकारी समोर तक्रार दाखल करा
3. उच्च न्यायालयात याचिका दाखल करा

**कायदेशीर आधार:** भारतीय नागरिक सुरक्षा संहिता कलम 173

**महत्त्वाचे:**
- FIR नोंदणी पोलिसांची कर्तव्य आहे
- नकार दिल्यास कायदेशीर कारवाई होऊ शकते""",
                "language": "marathi"
            },
        ]

        for qa in multilingual_qa:
            self.add_example(
                qa["instruction"],
                qa.get("input", ""),
                qa["output"],
                category="multilingual",
                language=qa["language"]
            )

    # =========================================
    # COMPARISON Q&A (Old vs New Laws)
    # =========================================

    def generate_comparison_qa(self):
        """Generate Q&A comparing old and new criminal laws"""
        comparisons = [
            {
                "instruction": "What is the difference between IPC Section 302 and BNS Section 103?",
                "output": """**Comparison: IPC Section 302 vs BNS Section 103 (Murder)**

| Aspect | IPC Section 302 (Old) | BNS Section 103 (New) |
|--------|----------------------|----------------------|
| **Offense** | Murder | Murder |
| **Punishment** | Death or Life imprisonment + Fine | Death or Life imprisonment + Fine |
| **New Addition** | - | Mob Lynching (Section 103(2)) |
| **Mob Lynching Punishment** | Not specifically covered | Death or Life imprisonment (minimum 7 years) |

**Key Change in BNS:**
Section 103(2) specifically addresses mob lynching: "When a group of five or more persons acting in concert commits murder on the ground of race, caste, community, sex, place of birth, language, personal belief or any other similar ground, each member of such group shall be punished with death or imprisonment for life (minimum 7 years), and fine."

**Why the Change:**
The BNS addresses mob lynching explicitly following incidents like the Palghar lynching and other hate crimes. IPC had no specific provision for group killings based on identity.

**Effective Date:** BNS came into force on 1st July 2024.""",
                "category": "comparison"
            },
            {
                "instruction": "Compare CrPC and BNSS - what are the major changes?",
                "output": """**Major Changes: CrPC 1973 vs BNSS 2023**

**1. Zero FIR (Section 173 BNSS)**
- Old: Not mandatory
- New: Mandatory provision for Zero FIR at any police station

**2. Investigation Timeline**
- Old: No fixed timeline
- New: 90 days for offenses up to 3 years, 180 days for serious offenses

**3. Electronic/Digital**
- Old: Limited provisions
- New: Electronic summons, video conferencing for trials, digital evidence handling

**4. Videography**
- Old: Not mandatory
- New: Mandatory videography for search and seizure

**5. Forensic Investigation**
- Old: Optional
- New: Mandatory visit of forensic experts for offenses punishable with 7+ years

**6. Victim Rights**
- Old: Limited
- New: Victim to be informed of progress within 90 days

**7. Bail (Section 480-482)**
- Old: Complex provisions
- New: Streamlined, specific timelines for bail hearings

**8. Mercy Petition**
- Old: No timeline
- New: President must decide within 60 days

**9. Proclamation (Absconder)**
- Old: 30 days publication
- New: Attachment of property permitted after proclamation

**Effective Date:** 1st July 2024""",
                "category": "comparison"
            },
        ]

        for comp in comparisons:
            self.add_example(
                comp["instruction"],
                comp.get("input", ""),
                comp["output"],
                category=comp["category"]
            )

    # =========================================
    # MAIN PROCESSING
    # =========================================

    def process_all_data(self):
        """Process all collected data and generate training examples"""
        logger.info("Starting training data generation...")

        # 1. Process bare acts
        logger.info("Processing bare acts...")
        acts_dir = self.data_dir / "bare_acts" / "central"
        acts = self.load_json_files(acts_dir)
        logger.info(f"Loaded {len(acts)} acts")

        for act in acts:
            self.generate_section_qa(act)
            self.generate_act_overview_qa(act)

        # 2. Process case law
        logger.info("Processing case law...")
        cases_dir = self.data_dir / "case_law"
        cases = self.load_json_files(cases_dir)
        logger.info(f"Loaded {len(cases)} cases")

        for case in cases:
            self.generate_case_qa(case)

        # 3. Add manual examples
        logger.info("Adding manual training examples...")
        self.generate_procedure_qa()
        self.generate_document_drafting_qa()
        self.generate_multilingual_qa()
        self.generate_comparison_qa()

        # 4. Load comprehensive Q&A if available
        comprehensive_file = self.data_dir / "training" / "comprehensive_legal_qa.json"
        if comprehensive_file.exists():
            logger.info("Loading comprehensive legal Q&A...")
            try:
                with open(comprehensive_file, 'r', encoding='utf-8') as f:
                    comprehensive_data = json.load(f)
                for item in comprehensive_data:
                    self.add_example(
                        item.get("instruction", ""),
                        item.get("input", ""),
                        item.get("output", ""),
                        category=item.get("category", "general"),
                        language=item.get("language", "english")
                    )
                logger.info(f"Added {len(comprehensive_data)} comprehensive Q&A examples")
            except Exception as e:
                logger.error(f"Error loading comprehensive Q&A: {e}")

        # 4. Shuffle
        random.shuffle(self.training_examples)

        # 5. Save
        logger.info(f"Saving {len(self.training_examples)} training examples...")
        self.output_file.parent.mkdir(parents=True, exist_ok=True)

        with open(self.output_file, 'w', encoding='utf-8') as f:
            json.dump(self.training_examples, f, ensure_ascii=False, indent=2)

        # 6. Create summary
        summary = {
            "total_examples": len(self.training_examples),
            "generated_at": datetime.now().isoformat(),
            "categories": {},
            "languages": {}
        }

        for ex in self.training_examples:
            cat = ex.get("metadata", {}).get("category", "unknown")
            lang = ex.get("metadata", {}).get("language", "english")
            summary["categories"][cat] = summary["categories"].get(cat, 0) + 1
            summary["languages"][lang] = summary["languages"].get(lang, 0) + 1

        summary_path = self.output_file.parent / "training_data_summary.json"
        with open(summary_path, 'w', encoding='utf-8') as f:
            json.dump(summary, f, ensure_ascii=False, indent=2)

        logger.info(f"Training data saved to: {self.output_file}")
        logger.info(f"Summary saved to: {summary_path}")
        logger.info(f"\nSummary:\n{json.dumps(summary, indent=2)}")

        return self.training_examples


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description='Generate training data for legal LLM')
    parser.add_argument('--data-dir', default='../../data', help='Data directory')
    parser.add_argument('--output', default='../../data/training_data.json', help='Output file')

    args = parser.parse_args()

    generator = LegalTrainingDataGenerator(
        data_dir=args.data_dir,
        output_file=args.output
    )
    generator.process_all_data()
